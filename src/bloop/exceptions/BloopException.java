package bloop.exceptions;

public abstract class BloopException extends RuntimeException {

    private final int line;

    protected BloopException(String message, int line) {
        super(formatMessage(message, line));
        this.line = line;
    }

    protected BloopException(String message) {
        super(message);
        this.line = -1; // Unknown line
    }

    public int getLine() {
        return line;
    }

    private static String formatMessage(String message, int line) {
        return "[line " + line + "] " + message;
    }
}