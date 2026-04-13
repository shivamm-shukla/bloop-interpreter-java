package bloop.token;


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


//  Token record
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › Token")
class TokenTest {

    @Test
    @DisplayName("Accessor methods return construction values")
    void accessors() {
        Token t = new Token(TokenType.NUMBER, "42", 3);
        assertEquals(TokenType.NUMBER, t.type());
        assertEquals("42", t.value());
        assertEquals(3, t.line());
    }

    @Test
    @DisplayName("Two tokens with identical fields are equal (record semantics)")
    void recordEquality() {
        Token a = new Token(TokenType.PLUS, "+", 1);
        Token b = new Token(TokenType.PLUS, "+", 1);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Tokens with different types are not equal")
    void inequalityByType() {
        assertNotEquals(
                new Token(TokenType.PLUS,  "+", 1),
                new Token(TokenType.MINUS, "+", 1));
    }

    @Test
    @DisplayName("Tokens with different values are not equal")
    void inequalityByValue() {
        assertNotEquals(
                new Token(TokenType.NUMBER, "1", 1),
                new Token(TokenType.NUMBER, "2", 1));
    }

    @Test
    @DisplayName("Tokens with different lines are not equal")
    void inequalityByLine() {
        assertNotEquals(
                new Token(TokenType.NUMBER, "1", 1),
                new Token(TokenType.NUMBER, "1", 2));
    }

    @Test
    @DisplayName("toString() contains type, value, and line information")
    void toStringContainsFields() {
        String s = new Token(TokenType.IDENTIFIER, "myVar", 7).toString();
        assertTrue(s.contains("IDENTIFIER"));
        assertTrue(s.contains("myVar"));
        assertTrue(s.contains("7"));
    }
}

// ────────────────────────────────────────────────────────────
//  TokenType enum
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › TokenType enum")
class TokenTypeTest {

    @Test
    @DisplayName("All expected token types exist")
    void allExpectedTypesExist() {
        // Just referencing each variant forces a compile error if any are missing
        TokenType[] expected = {
                TokenType.NUMBER, TokenType.STRING, TokenType.IDENTIFIER,
                TokenType.PUT, TokenType.INTO, TokenType.PRINT,
                TokenType.IF, TokenType.THEN, TokenType.ELSE,
                TokenType.REPEAT, TokenType.TIMES,
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
                TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL,
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL,
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN,
                TokenType.COMMA, TokenType.COLON,
                TokenType.NEWLINE, TokenType.INDENT, TokenType.DEDENT, TokenType.EOF
        };
        assertEquals(29, expected.length);
    }

    @Test
    @DisplayName("EOF is the last sentinel value")
    void eofIsLast() {
        TokenType[] vals = TokenType.values();
        assertEquals(TokenType.EOF, vals[vals.length - 1]);
    }
}