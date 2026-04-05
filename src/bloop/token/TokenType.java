package bloop.token;

/**
 * Every kind of token the BLOOP lexer can produce.
 *
 * Grouped by category so the enum is self-documenting.
 * Adding a new language keyword = one new entry here +
 * one entry in KeywordRegistry. Nothing else changes.
 */
public enum TokenType {

    // ── Literals ──────────────────────────────────────────────────────────
    NUMBER,       // e.g.  42   3.14
    STRING,       // e.g.  "hello"
    IDENTIFIER,   // e.g.  x   total   score

    // ── BLOOP Keywords ────────────────────────────────────────────────────
    PUT,          // put <expr> into <var>
    INTO,         // put … INTO …
    PRINT,        // print <expr>
    IF,           // if <condition> then:
    THEN,         // if … THEN:
    REPEAT,       // repeat <n> times:
    TIMES,        // repeat … TIMES:

    // ── Arithmetic Operators ──────────────────────────────────────────────
    PLUS,         // +
    MINUS,        // -
    STAR,         // *
    SLASH,        // /

    // ── Comparison Operators ──────────────────────────────────────────────
    EQUAL_EQUAL,  // ==
    NOT_EQUAL,    // !=
    GREATER,      // >
    GREATER_EQUAL,// >=
    LESS,         // <
    LESS_EQUAL,   // <=

    // ── Symbols ───────────────────────────────────────────────────────────
    LEFT_PAREN,   // (
    RIGHT_PAREN,  // )
    COMMA,        // ,
    COLON,        // :

    // ── Layout tokens (indentation-aware grammar) ─────────────────────────
    NEWLINE,      // end of a logical line
    INDENT,       // increase in indentation level
    DEDENT,       // return to previous indentation level

    // ── Sentinel ──────────────────────────────────────────────────────────
    EOF           // end of source — always the last token
}
