package bloop.lexer;

import bloop.token.TokenType;

import java.util.Map;


public final class KeywordRegistry {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "put",    TokenType.PUT,
            "into",   TokenType.INTO,
            "print",  TokenType.PRINT,
            "if",     TokenType.IF,
            "else",    TokenType.ELSE,
            "then",   TokenType.THEN,
            "repeat", TokenType.REPEAT,
            "times",  TokenType.TIMES
    );


    // to restrict creating object
    private KeywordRegistry() {}

    public static TokenType resolve(String word) {
        return KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
    }
}