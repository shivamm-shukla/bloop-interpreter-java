package bloop.lexer;


import bloop.token.TokenType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

//  LexerCursor
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › LexerCursor")
class LexCursorTest {

    @Test
    @DisplayName("currentChar() returns first character initially")
    void currentCharInitial() {
        assertEquals('h', new LexerCursor("hello").currentChar());
    }

    @Test
    @DisplayName("currentChar() returns '\\0' on empty source")
    void currentCharEmpty() {
        assertEquals('\0', new LexerCursor("").currentChar());
    }

    @Test
    @DisplayName("isExhausted() is true for empty source")
    void isExhaustedEmpty() {
        assertTrue(new LexerCursor("").isExhausted());
    }

    @Test
    @DisplayName("isExhausted() is false for non-empty source")
    void isExhaustedNonEmpty() {
        assertFalse(new LexerCursor("x").isExhausted());
    }

    @Test
    @DisplayName("isExhausted() becomes true after consuming all characters")
    void isExhaustedAfterConsuming() {
        LexerCursor c = new LexerCursor("ab");
        c.advance(); c.advance();
        assertTrue(c.isExhausted());
    }

    @Test
    @DisplayName("advance() moves to next character and returns current")
    void advanceMovesForward() {
        LexerCursor c = new LexerCursor("ab");
        assertEquals('a', c.advance());
        assertEquals('b', c.currentChar());
    }

    @Test
    @DisplayName("peekNextChar() returns the character after current without consuming")
    void peekNextChar() {
        LexerCursor c = new LexerCursor("ab");
        assertEquals('b', c.peekNextChar());
        assertEquals('a', c.currentChar()); // not consumed
    }

    @Test
    @DisplayName("peekNextChar() returns '\\0' when at last character")
    void peekNextCharAtEnd() {
        LexerCursor c = new LexerCursor("x");
        assertEquals('\0', c.peekNextChar());
    }

    @Test
    @DisplayName("advanceIf() consumes and returns true when character matches")
    void advanceIfMatch() {
        LexerCursor c = new LexerCursor("=x");
        assertTrue(c.advanceIf('='));
        assertEquals('x', c.currentChar());
    }

    @Test
    @DisplayName("advanceIf() does not consume and returns false when no match")
    void advanceIfNoMatch() {
        LexerCursor c = new LexerCursor("=x");
        assertFalse(c.advanceIf('!'));
        assertEquals('=', c.currentChar()); // unchanged
    }

    @Test
    @DisplayName("advanceIf() returns false on exhausted cursor")
    void advanceIfExhausted() {
        LexerCursor c = new LexerCursor("");
        assertFalse(c.advanceIf('a'));
    }

    @Test
    @DisplayName("sliceFrom() returns substring from given position to current")
    void sliceFrom() {
        LexerCursor c = new LexerCursor("hello");
        c.advance(); c.advance(); c.advance(); // position = 3
        assertEquals("hel", c.sliceFrom(0));
    }

    @Test
    @DisplayName("getCurrentLine() starts at 1")
    void initialLineIsOne() {
        assertEquals(1, new LexerCursor("code").getCurrentLine());
    }

    @Test
    @DisplayName("incrementLine() increments line counter")
    void incrementLine() {
        LexerCursor c = new LexerCursor("a\nb");
        c.incrementLine();
        assertEquals(2, c.getCurrentLine());
    }

    @Test
    @DisplayName("getCurrentPosition() starts at 0")
    void initialPosition() {
        assertEquals(0, new LexerCursor("abc").getCurrentPosition());
    }
}

// ────────────────────────────────────────────────────────────
//  KeywordRegistry
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › KeywordRegistry")
class KeywordRegistryTest {

    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "put,    PUT",
            "into,   INTO",
            "print,  PRINT",
            "if,     IF",
            "else,   ELSE",
            "then,   THEN",
            "repeat, REPEAT",
            "times,  TIMES"
    })
    @DisplayName("Known keywords resolve to their TokenType")
    void knownKeywords(String word, String expectedType) {
        assertEquals(TokenType.valueOf(expectedType.trim()),
                KeywordRegistry.resolve(word.trim()));
    }

    @ParameterizedTest(name = "\"{0}\" → IDENTIFIER")
    @ValueSource(strings = {"x", "total", "PUT", "Print", "IF", "foo123", "_var", "notAKeyword"})
    @DisplayName("Unknown words resolve to IDENTIFIER")
    void unknownWords(String word) {
        assertEquals(TokenType.IDENTIFIER, KeywordRegistry.resolve(word));
    }

    @Test
    @DisplayName("Empty string resolves to IDENTIFIER")
    void emptyStringIsIdentifier() {
        assertEquals(TokenType.IDENTIFIER, KeywordRegistry.resolve(""));
    }

    @Test
    @DisplayName("Keywords are case-sensitive — 'IF' is IDENTIFIER, not IF")
    void caseSensitive() {
        assertEquals(TokenType.IDENTIFIER, KeywordRegistry.resolve("IF"));
        assertEquals(TokenType.IF,         KeywordRegistry.resolve("if"));
    }
}

// ────────────────────────────────────────────────────────────
//  OperatorRegistry
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › OperatorRegistry")
class OperatorRegistryTest {

    @ParameterizedTest(name = "'{0}' → {1}")
    @CsvSource({
            "+, PLUS",
            "-, MINUS",
            "*, STAR",
            "/, SLASH",
            "(, LEFT_PAREN",
            "), RIGHT_PAREN",
            "',', COMMA",
            ":, COLON"
    })
    @DisplayName("Single-char operators resolve to correct TokenType")
    void singleCharOperators(char ch, String expectedType) {
        assertEquals(TokenType.valueOf(expectedType.trim()),
                OperatorRegistry.findSingle(ch));
    }

    @Test
    @DisplayName("findSingle() returns null for unknown character")
    void findSingleUnknown() {
        assertNull(OperatorRegistry.findSingle('@'));
        assertNull(OperatorRegistry.findSingle('?'));
    }

    @Test
    @DisplayName("findCompound('>'): singleType=GREATER, compoundType=GREATER_EQUAL")
    void compoundGreater() {
        OperatorRegistry.CompoundEntry e = OperatorRegistry.findCompound('>');
        assertNotNull(e);
        assertEquals(TokenType.GREATER,       e.singleType());
        assertEquals(TokenType.GREATER_EQUAL, e.compoundType());
    }

    @Test
    @DisplayName("findCompound('<'): singleType=LESS, compoundType=LESS_EQUAL")
    void compoundLess() {
        OperatorRegistry.CompoundEntry e = OperatorRegistry.findCompound('<');
        assertNotNull(e);
        assertEquals(TokenType.LESS,       e.singleType());
        assertEquals(TokenType.LESS_EQUAL, e.compoundType());
    }

    @Test
    @DisplayName("findCompound('='): singleType=null (bare '=' is invalid), compoundType=EQUAL_EQUAL")
    void compoundEquals() {
        OperatorRegistry.CompoundEntry e = OperatorRegistry.findCompound('=');
        assertNotNull(e);
        assertNull(e.singleType());
        assertEquals(TokenType.EQUAL_EQUAL, e.compoundType());
    }

    @Test
    @DisplayName("findCompound('!'): singleType=null, compoundType=NOT_EQUAL")
    void compoundNot() {
        OperatorRegistry.CompoundEntry e = OperatorRegistry.findCompound('!');
        assertNotNull(e);
        assertNull(e.singleType());
        assertEquals(TokenType.NOT_EQUAL, e.compoundType());
    }

    @Test
    @DisplayName("findCompound() returns null for unknown character")
    void findCompoundUnknown() {
        assertNull(OperatorRegistry.findCompound('x'));
        assertNull(OperatorRegistry.findCompound('+'));
    }
}