package bloop.exceptions;


public class BloopParseException extends BloopException {

    public BloopParseException(String message, int sourceLine) {
        super("Parse error — " + message, sourceLine);
    }
}
