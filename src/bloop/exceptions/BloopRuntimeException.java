package bloop.exceptions;


public class BloopRuntimeException extends BloopException {

    public BloopRuntimeException(String message, int sourceLine) {
        super("Runtime error — " + message, sourceLine);
    }

    public BloopRuntimeException(String message) {
        this(message, -1);
    }
}
