package bloop.parser.cursor;

import bloop.exceptions.BloopParseException;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenCursor — exhaustive")
class TokenCursorTest {

    private static Token tok(TokenType t, String v)          { return new Token(t, v, 1); }
    private static Token tok(TokenType t, String v, int line){ return new Token(t, v, line); }
    private static Token eof()                               { return tok(TokenType.EOF, ""); }
    private static Token nl()                                { return tok(TokenType.NEWLINE, ""); }

    private TokenCursor cursor(Token... tokens) { return new TokenCursor(List.of(tokens)); }

    // ══════════════════════════════════════════════════════════════════════
    // current()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("current()")
    class Current {
        @Test void returnsFirstToken()        { var c = cursor(tok(TokenType.NUMBER,"1"), eof()); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void returnsEOFAtEnd()          { var c = cursor(eof()); assertEquals(TokenType.EOF, c.current().type()); }
        @Test void syntheticEOFWhenExhausted(){ var c = cursor(eof()); c.consume(); assertEquals(TokenType.EOF, c.current().type()); }
        @Test void emptyListGivesEOF()        { var c = new TokenCursor(List.of()); assertEquals(TokenType.EOF, c.current().type()); }
        @Test void currentValueCorrect()      { var c = cursor(tok(TokenType.NUMBER,"42"), eof()); assertEquals("42", c.current().value()); }
        @Test void currentLineCorrect()       { var c = cursor(tok(TokenType.NUMBER,"1",5), eof()); assertEquals(5, c.current().line()); }
        @Test void currentDoesNotAdvance()    {
            var c = cursor(tok(TokenType.NUMBER,"1"), eof());
            c.current(); c.current(); // call twice
            assertEquals(TokenType.NUMBER, c.current().type());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // check()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("check()")
    class Check {
        @Test void trueForMatchingType()      { var c = cursor(tok(TokenType.PLUS,"+"), eof()); assertTrue(c.check(TokenType.PLUS)); }
        @Test void falseForDifferentType()    { var c = cursor(tok(TokenType.PLUS,"+"), eof()); assertFalse(c.check(TokenType.MINUS)); }
        @Test void checkEOF()                 { var c = cursor(eof()); assertTrue(c.check(TokenType.EOF)); }
        @Test void checkDoesNotAdvance()      { var c = cursor(tok(TokenType.PLUS,"+"), eof()); c.check(TokenType.PLUS); assertEquals(TokenType.PLUS, c.current().type()); }
        @Test void checkAfterConsume()        {
            var c = cursor(tok(TokenType.PLUS,"+"), tok(TokenType.MINUS,"-"), eof());
            c.consume();
            assertTrue(c.check(TokenType.MINUS));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // isAtEnd()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("isAtEnd()")
    class IsAtEnd {
        @Test void falseWhenNotAtEOF()        { var c = cursor(tok(TokenType.NUMBER,"1"), eof()); assertFalse(c.isAtEnd()); }
        @Test void trueWhenAtEOF()            { var c = cursor(eof()); assertTrue(c.isAtEnd()); }
        @Test void trueAfterConsumeAll()      { var c = cursor(tok(TokenType.NUMBER,"1"), eof()); c.consume(); assertTrue(c.isAtEnd()); }
        @Test void emptyListIsAtEnd()         { assertTrue(new TokenCursor(List.of()).isAtEnd()); }
        @Test void trueAfterExhausted()       { var c = cursor(eof()); c.consume(); c.consume(); assertTrue(c.isAtEnd()); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // consume()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("consume()")
    class Consume {
        @Test void returnsCurrentAndAdvances() {
            var c = cursor(tok(TokenType.NUMBER,"1"), tok(TokenType.PLUS,"+"), eof());
            Token t = c.consume();
            assertEquals(TokenType.NUMBER, t.type());
            assertEquals(TokenType.PLUS, c.current().type());
        }
        @Test void consumeAtEOFStaysAtEOF() {
            var c = cursor(eof());
            c.consume();
            assertEquals(TokenType.EOF, c.current().type());
        }
        @Test void consumeSequentially() {
            var c = cursor(tok(TokenType.NUMBER,"1"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"2"), eof());
            assertEquals(TokenType.NUMBER, c.consume().type());
            assertEquals(TokenType.PLUS,   c.consume().type());
            assertEquals(TokenType.NUMBER, c.consume().type());
            assertTrue(c.isAtEnd());
        }
        @Test void consumeReturnsCorrectValue() {
            var c = cursor(tok(TokenType.STRING,"hello"), eof());
            assertEquals("hello", c.consume().value());
        }
        @Test void consumeReturnsCorrectLine() {
            var c = cursor(tok(TokenType.NUMBER,"1",7), eof());
            assertEquals(7, c.consume().line());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // tryConsume()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("tryConsume()")
    class TryConsume {
        @Test void matchFirstType()           { var c = cursor(tok(TokenType.PLUS,"+"), eof()); assertNotNull(c.tryConsume(TokenType.PLUS, TokenType.MINUS)); }
        @Test void matchSecondType()          { var c = cursor(tok(TokenType.MINUS,"-"), eof()); assertNotNull(c.tryConsume(TokenType.PLUS, TokenType.MINUS)); }
        @Test void noMatchReturnsNull()       { var c = cursor(tok(TokenType.STAR,"*"), eof()); assertNull(c.tryConsume(TokenType.PLUS, TokenType.MINUS)); }
        @Test void noMatchDoesNotAdvance()    { var c = cursor(tok(TokenType.STAR,"*"), eof()); c.tryConsume(TokenType.PLUS); assertEquals(TokenType.STAR, c.current().type()); }
        @Test void matchAdvancesCursor()      { var c = cursor(tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"2"), eof()); c.tryConsume(TokenType.PLUS); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void returnsMatchedToken()      { var c = cursor(tok(TokenType.PLUS,"+"), eof()); assertEquals(TokenType.PLUS, c.tryConsume(TokenType.PLUS).type()); }
        @Test void singleTypeMatch()          { var c = cursor(tok(TokenType.STAR,"*"), eof()); assertNotNull(c.tryConsume(TokenType.STAR)); }
        @Test void noTypesGivenReturnsNull()  { var c = cursor(tok(TokenType.PLUS,"+"), eof()); assertNull(c.tryConsume()); }
        @Test void tryConsumeAtEOFNoMatch()   { var c = cursor(eof()); assertNull(c.tryConsume(TokenType.PLUS)); }
        @Test void tryConsumeEOFMatches()     { var c = cursor(eof()); assertNotNull(c.tryConsume(TokenType.EOF)); }
        @Test void consumesCorrectValueInSeq(){
            var c = cursor(tok(TokenType.PLUS,"+"), tok(TokenType.MINUS,"-"), eof());
            Token first = c.tryConsume(TokenType.PLUS, TokenType.MINUS);
            assertEquals(TokenType.PLUS, first.type());
            Token second = c.tryConsume(TokenType.PLUS, TokenType.MINUS);
            assertEquals(TokenType.MINUS, second.type());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // expect()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("expect()")
    class Expect {
        @Test void matchConsumesAndReturns()  {
            var c = cursor(tok(TokenType.COLON,":"), eof());
            Token t = c.expect(TokenType.COLON, "Expected ':'");
            assertEquals(TokenType.COLON, t.type());
            assertTrue(c.isAtEnd());
        }
        @Test void mismatchThrows()           {
            var c = cursor(tok(TokenType.PLUS,"+",5), eof());
            assertThrows(BloopParseException.class, () -> c.expect(TokenType.COLON, "Expected ':'"));
        }
        @Test void errorMessageInException()  {
            var c = cursor(tok(TokenType.PLUS,"+",5), eof());
            BloopParseException ex = assertThrows(BloopParseException.class, () -> c.expect(TokenType.COLON, "Expected ':'"));
            assertTrue(ex.getMessage().contains("Expected ':'"));
        }
        @Test void lineNumberInException()    {
            var c = cursor(tok(TokenType.PLUS,"+",7), eof());
            BloopParseException ex = assertThrows(BloopParseException.class, () -> c.expect(TokenType.MINUS, "oops"));
            assertTrue(ex.getMessage().contains("7"));
        }
        @Test void expectEOFWhenAtEOF()       {
            var c = cursor(eof());
            assertDoesNotThrow(() -> c.expect(TokenType.EOF, "Expected EOF"));
        }
        @Test void expectAfterConsumingPrevious() {
            var c = cursor(tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"1"), eof());
            c.consume();
            assertDoesNotThrow(() -> c.expect(TokenType.NUMBER, "Expected number"));
        }
        @Test void doesNotAdvanceOnMismatch() {
            var c = cursor(tok(TokenType.STAR,"*"), eof());
            try { c.expect(TokenType.PLUS, "x"); } catch (BloopParseException ignored) {}
            assertEquals(TokenType.STAR, c.current().type()); // still on STAR
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // skipNewlines()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("skipNewlines()")
    class SkipNewlines {
        @Test void skipsOne()              { var c = cursor(nl(), tok(TokenType.NUMBER,"1"), eof()); c.skipNewlines(); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void skipsMany()             { var c = cursor(nl(), nl(), nl(), tok(TokenType.NUMBER,"1"), eof()); c.skipNewlines(); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void noOpWhenNoNewline()     { var c = cursor(tok(TokenType.NUMBER,"1"), eof()); c.skipNewlines(); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void stopsAtNonNewline()     { var c = cursor(nl(), tok(TokenType.PLUS,"+"), nl(), eof()); c.skipNewlines(); assertEquals(TokenType.PLUS, c.current().type()); }
        @Test void stopsAtEOF()            { var c = cursor(nl(), eof()); c.skipNewlines(); assertTrue(c.isAtEnd()); }
        @Test void skipsNewlinesThenEOF()  { var c = cursor(nl(), nl(), eof()); c.skipNewlines(); assertTrue(c.isAtEnd()); }
        @Test void noOpAtEOF()             { var c = cursor(eof()); assertDoesNotThrow(c::skipNewlines); assertTrue(c.isAtEnd()); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // consumeNewlineIfPresent()
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("consumeNewlineIfPresent()")
    class ConsumeNewlineIfPresent {
        @Test void consumesSingleNewline()  { var c = cursor(nl(), tok(TokenType.NUMBER,"1"), eof()); c.consumeNewlineIfPresent(); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void consumesOnlyOne()        { var c = cursor(nl(), nl(), eof()); c.consumeNewlineIfPresent(); assertEquals(TokenType.NEWLINE, c.current().type()); }
        @Test void noOpWhenNotNewline()     { var c = cursor(tok(TokenType.NUMBER,"1"), eof()); c.consumeNewlineIfPresent(); assertEquals(TokenType.NUMBER, c.current().type()); }
        @Test void noOpAtEOF()             { var c = cursor(eof()); assertDoesNotThrow(c::consumeNewlineIfPresent); assertTrue(c.isAtEnd()); }
        @Test void canConsumeMultipleByCallingTwice() {
            var c = cursor(nl(), nl(), eof());
            c.consumeNewlineIfPresent();
            c.consumeNewlineIfPresent();
            assertTrue(c.isAtEnd());
        }
    }
}