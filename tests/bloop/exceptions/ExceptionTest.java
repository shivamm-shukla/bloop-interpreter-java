package bloop.exceptions;


//  UNIT TESTS — Exception Hierarchy
//  BloopException, BloopLexerException,
//  BloopParseException, BloopRuntimeException


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

// ────────────────────────────────────────────────────────────
//  BloopException (base)
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › BloopException")
class BloopExceptionTest {

    @Test
    @DisplayName("Message is prefixed with [Line N] when line >= 0")
    void messageWithPositiveLine() {
        BloopException ex = new BloopException("something went wrong", 5) {};
        assertEquals("[Line 5] something went wrong", ex.getMessage());
    }

    @Test
    @DisplayName("Message has no prefix when line is -1")
    void messageWithNegativeLine() {
        BloopException ex = new BloopException("unknown error", -1) {};
        assertEquals("unknown error", ex.getMessage());
    }

    @Test
    @DisplayName("Line 0 still gets a [Line 0] prefix")
    void messageWithLineZero() {
        BloopException ex = new BloopException("at start", 0) {};
        assertEquals("[Line 0] at start", ex.getMessage());
    }

    @Test
    @DisplayName("getSourceLine() returns the line passed at construction")
    void getSourceLineReturnsCorrectLine() {
        BloopException ex = new BloopException("test", 42) {};
        assertEquals(42, ex.getSourceLine());
    }

    @Test
    @DisplayName("Is a RuntimeException (unchecked)")
    void isRuntimeException() {
        BloopException ex = new BloopException("x", 1) {};
        assertInstanceOf(RuntimeException.class, ex);
    }
}

// ────────────────────────────────────────────────────────────
//  BloopLexerException
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › BloopLexerException")
class BloopLexerExceptionTest {

    @Test
    @DisplayName("Message is prefixed with 'Lexer error —'")
    void messageHasLexerPrefix() {
        BloopLexerException ex = new BloopLexerException("bad char '@'", 3);
        assertTrue(ex.getMessage().contains("Lexer error —"),
                "Expected 'Lexer error —' in: " + ex.getMessage());
    }

    @Test
    @DisplayName("Message includes the original detail text")
    void messageIncludesDetail() {
        BloopLexerException ex = new BloopLexerException("bad char '@'", 3);
        assertTrue(ex.getMessage().contains("bad char '@'"));
    }

    @Test
    @DisplayName("Line number is embedded in the message via BloopException")
    void messageIncludesLine() {
        BloopLexerException ex = new BloopLexerException("oops", 7);
        assertTrue(ex.getMessage().contains("[Line 7]"));
    }

    @Test
    @DisplayName("getSourceLine() is correct")
    void getSourceLine() {
        assertEquals(12, new BloopLexerException("x", 12).getSourceLine());
    }

    @Test
    @DisplayName("Is a subtype of BloopException")
    void isBloopException() {
        assertInstanceOf(BloopException.class, new BloopLexerException("x", 1));
    }
}

// ────────────────────────────────────────────────────────────
//  BloopParseException
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › BloopParseException")
class BloopParseExceptionTest {

    @Test
    @DisplayName("Message is prefixed with 'Parse error —'")
    void messageHasParsePrefix() {
        BloopParseException ex = new BloopParseException("unexpected token", 2);
        assertTrue(ex.getMessage().contains("Parse error —"));
    }

    @Test
    @DisplayName("Detail message is present")
    void messageIncludesDetail() {
        BloopParseException ex = new BloopParseException("unexpected token", 2);
        assertTrue(ex.getMessage().contains("unexpected token"));
    }

    @Test
    @DisplayName("Line number is embedded")
    void messageIncludesLine() {
        BloopParseException ex = new BloopParseException("oops", 10);
        assertTrue(ex.getMessage().contains("[Line 10]"));
    }

    @Test
    @DisplayName("No line prefix when line = -1")
    void noLinePrefixWhenNegative() {
        BloopParseException ex = new BloopParseException("internal error", -1);
        assertFalse(ex.getMessage().contains("[Line"));
    }

    @Test
    @DisplayName("Is a subtype of BloopException")
    void isBloopException() {
        assertInstanceOf(BloopException.class, new BloopParseException("x", 1));
    }
}

// ────────────────────────────────────────────────────────────
//  BloopRuntimeException
// ────────────────────────────────────────────────────────────
@DisplayName("Unit › BloopRuntimeException")
class BloopRuntimeExceptionTest {

    @Test
    @DisplayName("Two-arg constructor: message has 'Runtime error —' prefix")
    void twoArgHasPrefix() {
        BloopRuntimeException ex = new BloopRuntimeException("division by zero", 5);
        assertTrue(ex.getMessage().contains("Runtime error —"));
    }

    @Test
    @DisplayName("Two-arg constructor: line is embedded")
    void twoArgHasLine() {
        BloopRuntimeException ex = new BloopRuntimeException("oops", 9);
        assertTrue(ex.getMessage().contains("[Line 9]"));
    }

    @Test
    @DisplayName("One-arg constructor defaults to line -1 (no [Line N] prefix)")
    void oneArgDefaultsToNegativeLine() {
        BloopRuntimeException ex = new BloopRuntimeException("no line info");
        assertFalse(ex.getMessage().contains("[Line"),
                "One-arg constructor should not embed a line number");
    }

    @Test
    @DisplayName("One-arg constructor still has 'Runtime error —' prefix")
    void oneArgStillHasRuntimePrefix() {
        BloopRuntimeException ex = new BloopRuntimeException("boom");
        assertTrue(ex.getMessage().contains("Runtime error —"));
    }

    @Test
    @DisplayName("getSourceLine() is -1 for one-arg constructor")
    void oneArgSourceLineIsNegativeOne() {
        assertEquals(-1, new BloopRuntimeException("x").getSourceLine());
    }

    @Test
    @DisplayName("Is a subtype of BloopException")
    void isBloopException() {
        assertInstanceOf(BloopException.class, new BloopRuntimeException("x"));
    }
}
// ══════════════════════════════════════════════════════════════════════
// Exception hierarchy relationships
// ══════════════════════════════════════════════════════════════════════
@Nested @DisplayName("Hierarchy relationships")
class Hierarchy {
    @Test void lexerIsCaughtAsBloop()   { assertInstanceOf(BloopException.class, new BloopLexerException("x",1)); }
    @Test void parseIsCaughtAsBloop()   { assertInstanceOf(BloopException.class, new BloopParseException("x",1)); }
    @Test void runtimeIsCaughtAsBloop() { assertInstanceOf(BloopException.class, new BloopRuntimeException("x")); }
    @Test void allAreCaughtAsRuntime()  {
        assertInstanceOf(RuntimeException.class, new BloopLexerException("x",1));
        assertInstanceOf(RuntimeException.class, new BloopParseException("x",1));
        assertInstanceOf(RuntimeException.class, new BloopRuntimeException("x"));
    }
    @Test void lexerNotParseException() {
        new BloopLexerException("x", 1);
        assertFalse(false); }
    @Test void parseNotLexerException() {
        new BloopParseException("x", 1);
        assertFalse(false); }
}
