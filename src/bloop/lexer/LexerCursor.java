package bloop.lexer;

/**
 * Owns all character-level navigation over the raw BLOOP source string.
 *
 * SRP: Every method that reads or advances through individual characters
 * lives here. Nothing outside this class should hold or mutate a position index.
 *
 * Package-private — only the lexer layer uses it directly.
 */
final class LexerCursor {

    private final String source;
    private int position = 0;
    private int currentLine = 1;

    LexerCursor(String source) {
        this.source = source;
    }

    // ── Inspection ────────────────────────────────────────────────────────

    /** The character at the current position, or '\0' when exhausted. */
    char currentChar() {
        return isExhausted() ? '\0' : source.charAt(position);
    }

    /** One character ahead of the current position, or '\0' at end. */
    char peekNextChar() {
        int next = position + 1;
        return (next >= source.length()) ? '\0' : source.charAt(next);
    }

    /** True when all characters have been consumed. */
    boolean isExhausted() {
        return position >= source.length();
    }

    int getCurrentLine() { return currentLine; }

    /** Current absolute position — used by scanners to mark where a token starts. */
    int getCurrentPosition() { return position; }

    // ── Advancement ───────────────────────────────────────────────────────

    /** Advance one character and return it. */
    char advance() {
        return source.charAt(position++);
    }

    /**
     * Advance only if the current character equals {@code expected}.
     *
     * @return true if the character matched and was consumed.
     */
    boolean advanceIf(char expected) {
        if (isExhausted() || source.charAt(position) != expected) return false;
        position++;
        return true;
    }

    /** Extract the substring from {@code startPosition} up to (not including) the current position. */
    String sliceFrom(int startPosition) {
        return source.substring(startPosition, position);
    }

    void incrementLine() { currentLine++; }
}
