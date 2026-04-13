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

    /** Lex the entire source and return an immutable, ordered token list. */
    public List<Token> tokenize() {
        List<Token>          tokens      = new ArrayList<>();
        LexerCursor          cursor      = new LexerCursor(source);
        IndentationHandler   indentation = new IndentationHandler();

        while (!cursor.isExhausted()) {
            char ch = cursor.currentChar();

            if (isInlineWhitespace(ch)) {
                cursor.advance();

            } else if (ch == '#') {
                skipLineComment(cursor);

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

    // ── Private scanners — each has exactly one scanning job ─────────────

    /** Skips every character until end-of-line (the '\n' is left for the main loop). */
    private void skipLineComment(LexerCursor cursor) {
        while (!cursor.isExhausted() && cursor.currentChar() != '\n') {
            cursor.advance();
        }
    }

    /** Emits a NEWLINE token, increments the line counter, then handles indentation. */
    private void emitNewlineAndHandleIndent(LexerCursor cursor,
                                            IndentationHandler indentation,
                                            List<Token> tokens) {
        cursor.advance(); // consume '\n'
        tokens.add(new Token(TokenType.NEWLINE, "\\n", cursor.getCurrentLine()));
        cursor.incrementLine();
        indentation.processLineStart(cursor, tokens);
    }

    /**
     * Scans an integer or floating-point number.
     *
     * Bug-fix from original: validates that at most one '.' appears,
     * and that it is followed by at least one digit (so "3." is rejected).
     */
    private void scanNumberLiteral(LexerCursor cursor, List<Token> tokens) {
        int     startPosition = cursor.getCurrentPosition();
        boolean hasDecimalPoint = false;

        while (!cursor.isExhausted()) {
            char ch = cursor.currentChar();

            if (ch == '.') {
                if (hasDecimalPoint) {
                    throw new BloopLexerException(
                            "Malformed number — multiple decimal points found",
                            cursor.getCurrentLine());
                }
                if (!Character.isDigit(cursor.peekNextChar())) break; // trailing dot — stop here
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

    /**
     * Scans a word and resolves it to a keyword or identifier.
     * Identifiers may contain letters, digits, and underscores.
     */
    private void scanWordOrKeyword(LexerCursor cursor, List<Token> tokens) {
        int startPosition = cursor.getCurrentPosition();

        while (!cursor.isExhausted() &&
               (Character.isLetterOrDigit(cursor.currentChar()) || cursor.currentChar() == '_')) {
            cursor.advance();
        }

        String    word      = cursor.sliceFrom(startPosition);
        TokenType tokenType = KeywordRegistry.resolve(word); // OCP — no switch needed
        tokens.add(new Token(tokenType, word, cursor.getCurrentLine()));
    }

    /**
     * Scans a double-quoted string literal.
     * Supports escape sequences: \n  \t  \\  \"
     * Throws if the string is not closed before end-of-line or end-of-file.
     */
    private void scanStringLiteral(LexerCursor cursor, List<Token> tokens) {
        cursor.advance(); // consume opening '"'
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
                    "Unterminated string — reached end of file without closing '\"'",
                    cursor.getCurrentLine());
        }

        cursor.advance(); // consume closing '"'
        tokens.add(new Token(TokenType.STRING, content.toString(), cursor.getCurrentLine()));
    }

    /**
     * Scans one operator or symbol character, handling compound operators
     * (>=, <=, ==, !=) through the OperatorRegistry.
     *
     * OCP fix: dispatch is data-driven, not a hardcoded switch.
     * DRY fix: all compound operator logic shares one code path.
     */
    private void scanOperatorOrSymbol(LexerCursor cursor, List<Token> tokens) {
        char ch   = cursor.currentChar();
        int  line = cursor.getCurrentLine();

        // Try compound operators first: >, <, =, !
        var compoundEntry = OperatorRegistry.findCompound(ch);
        if (compoundEntry.isPresent()) {
            cursor.advance();
            var entry = compoundEntry.get();

            if (cursor.advanceIf('=')) {
                // Two-character form: >=, <=, ==, !=
                tokens.add(new Token(entry.compoundType(), ch + "=", line));
            } else {
                // Single-character form: bare '!' or bare '=' is illegal in BLOOP
                if (entry.singleType() == null) {
                    throw new BloopLexerException(
                            "Unexpected character '" + ch + "' — did you mean '" + ch + "='?", line);
                }
                tokens.add(new Token(entry.singleType(), String.valueOf(ch), line));
            }
            return;
        }

        // Try simple single-character operators: +, -, *, /, (, ), ,, :
        var singleType = OperatorRegistry.findSingle(ch);
        if (singleType.isPresent()) {
            cursor.advance();
            tokens.add(new Token(singleType.get(), String.valueOf(ch), line));
            return;
        }

        // Unrecognised character
        cursor.advance(); // consume so we don't spin forever
        throw new BloopLexerException(
                "Unexpected character '" + ch + "' (Unicode: U+" +
                Integer.toHexString(ch).toUpperCase() + ")", line);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private boolean isInlineWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
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
                    "Unknown escape sequence '\\" + escapedChar + "'",
                    cursor.getCurrentLine());
        };
    }
}
