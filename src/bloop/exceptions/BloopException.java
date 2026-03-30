package bloop.exceptions;

public abstract class BloopException extends RuntimeException {

    private final int line;

    protected BloopException(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}