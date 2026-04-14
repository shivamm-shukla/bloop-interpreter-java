package bloop.exceptions;


public class BloopLexerException extends BloopException {

    public BloopLexerException(String message, int sourceLine) {
        super("Lexer error — " + message, sourceLine);
    }
}
