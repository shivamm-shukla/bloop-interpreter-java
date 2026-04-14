package bloop.lexer;

import bloop.exceptions.BloopLexerException;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;


public final class IndentationHandler {

    private final Deque<Integer> indentLevelStack = new ArrayDeque<>();

    public IndentationHandler() {
        indentLevelStack.push(0);
    }

    public void processLineStart(LexerCursor cursor, List<Token> tokens) {
        int spacesOnThisLine = consumeLeadingSpaces(cursor);
        int previousLevel = indentLevelStack.isEmpty() ? 0 : indentLevelStack.peek();

        if (spacesOnThisLine > previousLevel) {
            indentLevelStack.push(spacesOnThisLine);
            tokens.add(new Token(TokenType.INDENT, "", cursor.getCurrentLine()));
        } else if (spacesOnThisLine < previousLevel) {
            emitDedentsUntil(spacesOnThisLine, cursor.getCurrentLine(), tokens);
        }
    }

    public void closeAllOpenBlocks(int finalLine, List<Token> tokens) {
        while (indentLevelStack.size() > 1) {
            indentLevelStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", finalLine));
        }
    }


    private int consumeLeadingSpaces(LexerCursor cursor) {
        int count = 0;

        while (!cursor.isExhausted()) {
            if (cursor.currentChar() == ' ') {
                count++;
            } else if (cursor.currentChar() == '\t') {
                count += 4;
            } else {
                break;
            }
            cursor.advance();
        }
        return count;
    }


    private void emitDedentsUntil(int targetLevel, int line, List<Token> tokens) {
        while (!indentLevelStack.isEmpty() && indentLevelStack.peek() > targetLevel) {
            indentLevelStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", line));
        }
        if (indentLevelStack.isEmpty() || indentLevelStack.peek() != targetLevel) {
            throw new BloopLexerException(
                    "IndentationError: unindent does not match any outer indentation level", line);
        }
    }
}