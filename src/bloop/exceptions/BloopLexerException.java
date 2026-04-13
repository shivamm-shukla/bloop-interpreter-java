package bloop.exceptions;

public class BloopLexerException extends BloopException {

    public BloopLexerException(String message) {
        super(message);
    }

    public BloopLexerException(String message, int line) {
        super(message, line);
    }
}