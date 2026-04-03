package bloop.exceptions;

public class BloopRuntimeException extends BloopException {

    public BloopRuntimeException(String message) {
        super("Runtime error: " + message, -1);
    }

    public BloopRuntimeException(String message, int line) {
        super("Runtime error at line " + line + ": " + message, line);
    }
}