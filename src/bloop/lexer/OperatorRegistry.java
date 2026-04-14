package bloop.lexer;

import bloop.token.TokenType;

import java.util.Map;


public final class OperatorRegistry {

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

    public record CompoundEntry(TokenType singleType, TokenType compoundType) {}

    private static final Map<Character, CompoundEntry> COMPOUND = Map.of(
            '>', new CompoundEntry(TokenType.GREATER,   TokenType.GREATER_EQUAL),
            '<', new CompoundEntry(TokenType.LESS,      TokenType.LESS_EQUAL),
            '=', new CompoundEntry(null,                TokenType.EQUAL_EQUAL),
            '!', new CompoundEntry(null,                TokenType.NOT_EQUAL)
    );


    // to restrict instaniation
    private OperatorRegistry() {}

    public static TokenType findSingle(char c) {
        return SINGLE_CHAR.get(c); // null if not found
    }

    public static CompoundEntry findCompound(char c) {
        return COMPOUND.get(c); // null if not found
    }
}