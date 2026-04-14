package bloop.lexer;


public final class LexerCursor {

    private final String source;
    private int position    = 0;
    private int currentLine = 1;

    public LexerCursor(String source) {
        this.source = source;
    }

    public char currentChar() {
        return isExhausted() ? '\0' : source.charAt(position);
    }

    public char peekNextChar() {
        int next = position + 1;
        return (next >= source.length()) ? '\0' : source.charAt(next);
    }

    public boolean isExhausted() {
        return position >= source.length();
    }

    public int getCurrentLine()     { return currentLine; }
    public int getCurrentPosition() { return position;    }

    public char advance() {
        return source.charAt(position++);
    }

    public boolean advanceIf(char expected) {
        if (isExhausted() || source.charAt(position) != expected) return false;
        position++;
        return true;
    }

    public String sliceFrom(int startPosition) {
        return source.substring(startPosition, position);
    }

    public void incrementLine() { currentLine++; }
}