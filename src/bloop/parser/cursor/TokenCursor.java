package bloop.parser.cursor;

import bloop.exceptions.BloopParseException;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.List;


public final class TokenCursor {

    private final List<Token> tokens;
    private int currentIndex = 0;

    public TokenCursor(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Inspection

    public Token current() {
        if (currentIndex >= tokens.size()) {
            return new Token(TokenType.EOF, "EOF", -1);
        }
        return tokens.get(currentIndex);
    }

    // True if the current token matches the given type
    public boolean check(TokenType type) {
        return current().type() == type;
    }

    // True if we are positioned at the EOF token
    public boolean isAtEnd() {
        return current().type() == TokenType.EOF;
    }

    //  Advancement

    public Token consume() {
        Token token = current();
        if (!isAtEnd()) currentIndex++;
        return token;
    }


    public Token tryConsume(TokenType... acceptableTypes) {
        for (TokenType type : acceptableTypes) {
            if (check(type)) return consume();
        }
        return null;
    }


    public Token expect(TokenType expectedType, String errorMessage) {
        if (check(expectedType)) return consume();
        throw new BloopParseException(errorMessage, current().line());
    }

    // Skip past any NEWLINE tokens at the current position
    public void skipNewlines() {
        while (check(TokenType.NEWLINE)) consume();
    }

    // Consume a NEWLINE if one is present; silently do nothing if not
    public void consumeNewlineIfPresent() {
        if (check(TokenType.NEWLINE)) consume();
    }
}
