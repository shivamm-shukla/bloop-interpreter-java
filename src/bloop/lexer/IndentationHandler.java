package bloop.lexer;

import bloop.exceptions.BloopLexerException;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Tracks indentation depth and emits INDENT / DEDENT tokens.
 *
 * SRP: Every decision about when to push or pop indentation levels
 * lives exclusively here. The Tokenizer delegates all indentation
 * concerns to this class and never touches the stack directly.
 *
 * Algorithm:
 *   • After each NEWLINE, count leading spaces on the next line.
 *   • More spaces than the current level → emit one INDENT, push new level.
 *   • Fewer spaces                        → emit one DEDENT per closed level.
 *   • Same spaces                         → no structural token emitted.
 *   • A dedent that does not land on a known level → lexer error.
 */
final class IndentationHandler {

    private final Deque<Integer> indentLevelStack = new ArrayDeque<>();

    IndentationHandler() {
        indentLevelStack.push(0); // base level
    }

    /**
     * Called immediately after a NEWLINE token is emitted.
     * Consumes leading spaces from the cursor and emits structural tokens.
     */
    void processLineStart(LexerCursor cursor, List<Token> tokens) {
        int spacesOnThisLine = consumeLeadingSpaces(cursor);
        int previousLevel    = indentLevelStack.peek();

        if (spacesOnThisLine > previousLevel) {
            indentLevelStack.push(spacesOnThisLine);
            tokens.add(new Token(TokenType.INDENT, "", cursor.getCurrentLine()));

        } else if (spacesOnThisLine < previousLevel) {
            emitDedentsUntil(spacesOnThisLine, cursor.getCurrentLine(), tokens);
        }
        // equal → same level, no structural token needed
    }

    /**
     * Called at EOF to close any remaining open indentation blocks.
     */
    void closeAllOpenBlocks(int finalLine, List<Token> tokens) {
        while (indentLevelStack.size() > 1) {
            indentLevelStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", finalLine));
        }
    }

    // ── private helpers ───────────────────────────────────────────────────

    private int consumeLeadingSpaces(LexerCursor cursor) {
        int count = 0;
        while (!cursor.isExhausted() && cursor.currentChar() == ' ') {
            cursor.advance();
            count++;
        }
        return count;
    }

    private void emitDedentsUntil(int targetLevel, int line, List<Token> tokens) {
        while (indentLevelStack.peek() > targetLevel) {
            indentLevelStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", line));
        }

        if (indentLevelStack.peek() != targetLevel) {
            throw new BloopLexerException(
                    "Dedent does not match any enclosing indentation level", line);
        }
    }
}
