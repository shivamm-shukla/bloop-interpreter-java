package bloop.token;

public class Token {

    private final TokenType type;
    private final String value;
    private final int line;

    public Token(TokenType type, String value, int line) {
        if (type == null)
            throw new IllegalArgumentException("Token: type cannot be null");
        if (value == null)
            throw new IllegalArgumentException("Token: value cannot be null");
        if (line < 1)
            throw new IllegalArgumentException("Token: line must be >= 1");

        this.type = type;
        this.value = value;
        this.line = line;
    }

    public TokenType getType()  { return type; }
    public String getValue()    { return value; }
    public int getLine()        { return line; }

    @Override
    public String toString() {
        return "Token(" + type + ", '" + value + "', line=" + line + ")";
    }
}