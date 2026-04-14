package bloop.token;

public record Token(TokenType type, String value, int line) {

    @Override
    public String toString() {
        return String.format("Token[%-15s | %-12s | line %d]", type, "\"" + value + "\"", line);
    }
}