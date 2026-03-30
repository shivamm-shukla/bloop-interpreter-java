package bloop.exceptions;

public class BloopParseException extends BloopException {

    public BloopParseException(String message) {
        super(message);
    }

    public BloopParseException(String message, int line) {
        super(message, line);
    }
}