package bloop.lexer;

import bloop.token.TokenType;

import java.util.Map;
import java.util.Optional;

/**
 * Maps operator characters to their TokenTypes.
 *
 * OCP: Adding a new operator = one new entry in a map.
 * LexerCore never needs a new branch.
 *
 * Two tables:
 *   SINGLE_CHAR — operators that are always one character  (+, -, *, …)
 *   COMPOUND    — operators that may be one or two chars   (>, >=  |  =, ==  …)
 */
final class OperatorRegistry {

    /** Operators that are always exactly one character and need no lookahead. */
    private static final Map<Character, TokenType> SINGLE_CHAR = Map.of(
            '+', TokenType.PLUS,
            '-', TokenType.MINUS,
            '*', TokenType.STAR,
            '/', TokenType.SLASH,
            '(', TokenType.LEFT_PAREN,
            ')', TokenType.RIGHT_PAREN,
            ',', TokenType.COMMA,
            ':', TokenType.COLON
    );

    /**
     * Operators that may expand to a two-character form when followed by '='.
     *
     * singleType  — the type when NOT followed by '='  (null = standalone char is illegal)
     * compoundType — the type when followed by '='
     */
    record CompoundEntry(TokenType singleType, TokenType compoundType) {}

    private static final Map<Character, CompoundEntry> COMPOUND = Map.of(
            '>', new CompoundEntry(TokenType.GREATER,   TokenType.GREATER_EQUAL),
            '<', new CompoundEntry(TokenType.LESS,      TokenType.LESS_EQUAL),
            '=', new CompoundEntry(null,                TokenType.EQUAL_EQUAL),   // bare '=' is not used in BLOOP
            '!', new CompoundEntry(null,                TokenType.NOT_EQUAL)      // bare '!' is illegal
    );

    private OperatorRegistry() {}

    static Optional<TokenType>      findSingle(char c)   { return Optional.ofNullable(SINGLE_CHAR.get(c)); }
    static Optional<CompoundEntry>  findCompound(char c)  { return Optional.ofNullable(COMPOUND.get(c));   }
}
