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

    // Literals & identifiers
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

    // Structure
       
    COLON,        // :
    NEWLINE,      // \n
    INDENT,       // indentation increase
    DEDENT,       // indentation decrease

    // Special
    EOF
}
