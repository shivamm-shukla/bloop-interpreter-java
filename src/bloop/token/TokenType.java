package bloop.token;

public enum TokenType {

    // Keywords
    PUT,
    INTO,
    PRINT,
    IF,
    THEN,
    REPEAT,
    TIMES,

    // Identifiers & literals
    IDENTIFIER,
    NUMBER,
    STRING,

    // Operators
    PLUS,      // +
    MINUS,     // -
    STAR,      // *
    SLASH,     // /
    GREATER,   // >
    LESS,      // <
    EQUAL_EQUAL, // ==

    // Special
    NEWLINE,
    EOF
}