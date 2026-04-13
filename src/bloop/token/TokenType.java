package bloop.token;

public enum TokenType {

    // Literals
    NUMBER,       // e.g.  42   3.14
    STRING,       // e.g.  "hello"
    IDENTIFIER,   // e.g.  x   total   score

    // BLOOP Keywords
    PUT,          // put <expr> into <var>
    INTO,         // put … INTO …
    PRINT,        // print <expr>
    IF,           // if <condition> then:
    THEN,         // if … THEN:
    ELSE,         // else:
    REPEAT,       // repeat <n> times:
    TIMES,        // repeat … TIMES:

    // Arithmetic Operators
    PLUS,         // +
    MINUS,        // -
    STAR,         // *
    SLASH,        // /

    // Comparison Operators
    EQUAL_EQUAL,  // ==
    NOT_EQUAL,    // !=
    GREATER,      // >
    GREATER_EQUAL,// >=
    LESS,         //
    LESS_EQUAL,   // <=

    // Symbols
    LEFT_PAREN,   // (
    RIGHT_PAREN,  // )
    COMMA,        // ,
    COLON,        // :

    // Layout tokens (indentation-aware grammar)
    NEWLINE,      // end of a logical line
    INDENT,       // increase in indentation level
    DEDENT,       // return to previous indentation level

    // Sentinel
    EOF           // end of source — always the last
}