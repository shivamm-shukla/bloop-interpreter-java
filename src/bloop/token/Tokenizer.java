package bloop.token;

import bloop.exceptions.BloopLexerException;
import bloop.lexer.*;

import java.util.ArrayList;
import java.util.List;


public final class Tokenizer {

    private final String source;

    public Tokenizer(String source) {
        if (source == null) throw new IllegalArgumentException("Source code must not be null");
        this.source = source;
    }

    public List<Token> tokenize() {
        List<Token> tokens  = new ArrayList<>();
        LexerCursor cursor  = new LexerCursor(source);
        IndentationHandler indentation = new IndentationHandler();

        while (!cursor.isExhausted()) {
            char ch = cursor.currentChar();

            if (isInlineWhitespace(ch)) {
                cursor.advance();

            } else if (ch == '\n') {
                emitNewlineAndHandleIndent(cursor, indentation, tokens);

            } else if (Character.isDigit(ch)) {
                scanNumberLiteral(cursor, tokens);

            } else if (Character.isLetter(ch) || ch == '_') {
                scanWordOrKeyword(cursor, tokens);

            } else if (ch == '"') {
                scanStringLiteral(cursor, tokens);

            } else {
                scanOperatorOrSymbol(cursor, tokens);
            }
        }

        indentation.closeAllOpenBlocks(cursor.getCurrentLine(), tokens);
        tokens.add(new Token(TokenType.EOF, "", cursor.getCurrentLine()));
        return List.copyOf(tokens);
    }

    private void emitNewlineAndHandleIndent(LexerCursor cursor,
                                            IndentationHandler indentation,
                                            List<Token> tokens) {
        int lineBeforeAdvance = cursor.getCurrentLine();
        cursor.advance();
        cursor.incrementLine();

        //  check full line
        boolean isBlankLine = true;
        int pos = cursor.getCurrentPosition();

        while (pos < source.length()) {
            char ch = source.charAt(pos);

            if (ch == '\n') break;
            if (ch != ' ' && ch != '\t') {
                isBlankLine = false;
                break;
            }
            pos++;
        }

        if (!isBlankLine && !cursor.isExhausted()) {
            tokens.add(new Token(TokenType.NEWLINE, "", lineBeforeAdvance));
            indentation.processLineStart(cursor, tokens);
        }
    }

    private void scanNumberLiteral(LexerCursor cursor, List<Token> tokens) {
        int startPosition = cursor.getCurrentPosition();
        boolean hasDecimalPoint = false;

        while (!cursor.isExhausted()) {
            char ch = cursor.currentChar();

            if (ch == '.') {
                if (hasDecimalPoint) {
                    throw new BloopLexerException(
                            "Malformed number — multiple decimal points found",
                            cursor.getCurrentLine());
                }
                if (!Character.isDigit(cursor.peekNextChar())) break;
                hasDecimalPoint = true;
                cursor.advance();

            } else if (Character.isDigit(ch)) {
                cursor.advance();

            } else {
                break;
            }
        }

        tokens.add(new Token(TokenType.NUMBER, cursor.sliceFrom(startPosition), cursor.getCurrentLine()));
    }

    private void scanWordOrKeyword(LexerCursor cursor, List<Token> tokens) {
        int startPosition = cursor.getCurrentPosition();

        while (!cursor.isExhausted() &&
                (Character.isLetterOrDigit(cursor.currentChar()) || cursor.currentChar() == '_')) {
            cursor.advance();
        }

        String word = cursor.sliceFrom(startPosition);
        TokenType tokenType = KeywordRegistry.resolve(word);
        tokens.add(new Token(tokenType, word, cursor.getCurrentLine()));
    }

    private void scanStringLiteral(LexerCursor cursor, List<Token> tokens) {
        cursor.advance(); // consume opening quote
        StringBuilder content = new StringBuilder();

        while (!cursor.isExhausted() && cursor.currentChar() != '"') {
            char ch = cursor.advance();

            if (ch == '\n') {
                throw new BloopLexerException(
                        "Unterminated string — newline inside string literal",
                        cursor.getCurrentLine());
            }
            if (ch == '\\') {
                content.append(resolveEscapeSequence(cursor));
            } else {
                content.append(ch);
            }
        }

        if (cursor.isExhausted()) {
            throw new BloopLexerException(
                    "Unterminated string — reached end of file without closing quote",
                    cursor.getCurrentLine());
        }

        cursor.advance(); // consume closing quote
        tokens.add(new Token(TokenType.STRING, content.toString(), cursor.getCurrentLine()));
    }

    private void scanOperatorOrSymbol(LexerCursor cursor, List<Token> tokens) {
        char ch   = cursor.currentChar();
        int  line = cursor.getCurrentLine();

        OperatorRegistry.CompoundEntry compoundEntry = OperatorRegistry.findCompound(ch);
        if (compoundEntry != null) {
            cursor.advance();

            if (cursor.advanceIf('=')) {
                tokens.add(new Token(compoundEntry.compoundType(), ch + "=", line));
            } else {
                if (compoundEntry.singleType() == null) {
                    throw new BloopLexerException(
                            "Unexpected character '" + ch + "' — did you mean '" + ch + "='?", line);
                }
                tokens.add(new Token(compoundEntry.singleType(), String.valueOf(ch), line));
            }
            return;
        }

        TokenType singleType = OperatorRegistry.findSingle(ch);
        if (singleType != null) {
            cursor.advance();
            tokens.add(new Token(singleType, String.valueOf(ch), line));
            return;
        }

        cursor.advance();
        throw new BloopLexerException(
                "Unexpected character '" + ch + "' (Unicode: U+" +
                        Integer.toHexString(ch).toUpperCase() + ")", line);
    }

    private boolean isInlineWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r';
    }

    private char resolveEscapeSequence(LexerCursor cursor) {
        if (cursor.isExhausted()) {
            throw new BloopLexerException("Incomplete escape sequence at end of file",
                    cursor.getCurrentLine());
        }
        char escapedChar = cursor.advance();
        return switch (escapedChar) {
            case 'n'  -> '\n';
            case 't'  -> '\t';
            case '\\' -> '\\';
            case '"'  -> '"';
            default   -> throw new BloopLexerException(
                    "Unknown escape sequence: " + escapedChar,
                    cursor.getCurrentLine());
        };
    }
}