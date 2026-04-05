package bloop.lexer;

import bloop.token.TokenType;

import java.util.Map;

/**
 * Maps every BLOOP reserved word to its TokenType.
 *
 * OCP: Adding a new keyword = one new entry in this map.
 * The Tokenizer never needs to change.
 *
 * The map is package-private; only LexerCore uses it.
 */
final class KeywordRegistry {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "put",    TokenType.PUT,
            "into",   TokenType.INTO,
            "print",  TokenType.PRINT,
            "if",     TokenType.IF,
            "then",   TokenType.THEN,
            "repeat", TokenType.REPEAT,
            "times",  TokenType.TIMES
    );

    private KeywordRegistry() {}

    /**
     * Resolve a scanned word to its TokenType.
     *
     * @return the keyword's type, or IDENTIFIER if the word is not reserved.
     */
    static TokenType resolve(String word) {
        return KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
    }
}
