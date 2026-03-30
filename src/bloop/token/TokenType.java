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
    // Comparison Operators
    GREATER,          // >
    LESS,             // <
    GREATER_EQUAL,    // >=
    LESS_EQUAL,       // <=
    EQUAL_EQUAL,      // ==
    NOT_EQUAL,        // !=

    // Structure
       
    COLON,        // :
    NEWLINE,      // \n
    INDENT,       /
    DEDENT,      

   
    EOF
}
