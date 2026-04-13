package bloop.exceptions;



public class BloopException extends RuntimeException {

    private final int sourceLine;

    public BloopException(String message, int sourceLine) {
        super(buildMessage(message, sourceLine));
        this.sourceLine = sourceLine;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    // ── private helpers ───────────────────────────────────────────────────

    private static String buildMessage(String message, int line) {
        return line >= 0
                ? "[Line " + line + "] " + message
                : message;
    }
}
