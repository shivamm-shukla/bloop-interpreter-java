package bloop.exceptions;

public class BloopParseException extends RuntimeException {
    private final int line;

    public BloopParseException(String message, int line) {
        super("Parse Error - Line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}