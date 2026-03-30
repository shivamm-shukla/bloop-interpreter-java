package bloop.exceptions;

public class BloopParseException extends BloopException {

    public BloopParseException(String message, int line) {
        super("Parse error at line " + line + ": " + message, line);
    }
}