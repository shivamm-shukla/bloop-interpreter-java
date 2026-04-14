package bloop.lexer;

import bloop.exceptions.BloopLexerException;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class IndentationHandlerTest {

    // =========================================================================
    // Helpers
    // =========================================================================

    private LexerCursor cursorFor(String source) {
        return new LexerCursor(source);
    }

    private long countOf(List<Token> tokens, TokenType type) {
        return tokens.stream().filter(t -> t.type() == type).count();
    }

    // =========================================================================
    // Construction — 4 tests
    // =========================================================================

    @Test
    void constructor_freshHandler_zeroIndentEmitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("print"), tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void constructor_freshHandler_lineIsOne() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("x");
        handler.processLineStart(cursor, tokens);
        assertEquals(0, tokens.size());
        assertEquals(1, cursor.getCurrentLine());
    }

    @Test
    void constructor_freshHandler_closeAllOpenBlocks_emitsNothing() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.closeAllOpenBlocks(1, tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void constructor_twoIndependentHandlers_doNotShareState() {
        IndentationHandler h1 = new IndentationHandler();
        IndentationHandler h2 = new IndentationHandler();
        List<Token> t1 = new ArrayList<>();
        List<Token> t2 = new ArrayList<>();

        h1.processLineStart(cursorFor("    x"), t1);
        h2.processLineStart(cursorFor("y"), t2);

        assertEquals(1, countOf(t1, TokenType.INDENT));
        assertTrue(t2.isEmpty());
    }

    // =========================================================================
    // processLineStart — no change (same level) — 6 tests
    // =========================================================================

    @Test
    void processLineStart_zeroIndentOnce_emitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("print"), tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void processLineStart_zeroIndentTwice_emitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("a"), tokens);
        handler.processLineStart(cursorFor("b"), tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void processLineStart_sameIndentLevelTwice_emitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("    y"), tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void processLineStart_sameIndentLevelThrice_emitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("    y"), tokens);
        handler.processLineStart(cursorFor("    z"), tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void processLineStart_zeroIndent_doesNotConsumePrintChar() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("print");
        handler.processLineStart(cursor, tokens);
        assertEquals('p', cursor.currentChar());
    }

    @Test
    void processLineStart_sameIndent_cursorSitsOnFirstNonSpace() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        LexerCursor cursor = cursorFor("    y");
        handler.processLineStart(cursor, tokens);
        assertEquals('y', cursor.currentChar());
    }

    // =========================================================================
    // processLineStart — INDENT — 18 tests
    // =========================================================================

    @Test
    void processLineStart_fourSpaces_emitsExactlyOneINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_fourSpaces_emitsOnlyOneToken() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertEquals(1, tokens.size());
    }

    @Test
    void processLineStart_eightSpaces_emitsOneINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("        x"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_oneTab_countsAsFourSpaces_emitsINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("\tx"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_twoTabs_emitsOneINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("\t\tx"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_twoSpacesThenTab_emitsINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("  \tx"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_indent_tokenTypeIsINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertEquals(TokenType.INDENT, tokens.get(0).type());
    }

    @Test
    void processLineStart_indent_tokenValueIsEmpty() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertEquals("", tokens.get(0).value());
    }

    @Test
    void processLineStart_indent_tokenLineIsLine1ByDefault() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertEquals(1, tokens.get(0).line());
    }

    @Test
    void processLineStart_indent_tokenLineReflectsCursorLine() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("    x");
        cursor.incrementLine();
        cursor.incrementLine(); // line 3
        handler.processLineStart(cursor, tokens);
        assertEquals(3, tokens.get(0).line());
    }

    @Test
    void processLineStart_indent_consumesAllLeadingSpaces() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("    hello");
        handler.processLineStart(cursor, tokens);
        assertEquals('h', cursor.currentChar());
    }

    @Test
    void processLineStart_indent_consumesTab_cursorOnContent() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("\thello");
        handler.processLineStart(cursor, tokens);
        assertEquals('h', cursor.currentChar());
    }

    @Test
    void processLineStart_twoLevelsOfIndent_emitsTwoINDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_twoLevelsOfIndent_emitsNoDEDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        assertEquals(0, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_threeNestedLevels_emitsThreeINDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        handler.processLineStart(cursorFor("            c"), tokens);
        assertEquals(3, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_indentAfterDedent_emitsINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        handler.processLineStart(cursorFor("y"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("    z"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
        assertEquals(0, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_noLeadingWhitespace_cursorUnchanged() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("hello");
        handler.processLineStart(cursor, tokens);
        assertEquals(0, cursor.getCurrentPosition());
    }

    @Test
    void processLineStart_onlySpaces_exhaustsCursor() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("    ");
        handler.processLineStart(cursor, tokens);
        assertTrue(cursor.isExhausted());
    }

    // =========================================================================
    // processLineStart — DEDENT — 16 tests
    // =========================================================================

    @Test
    void processLineStart_dedentToZero_emitsOneDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_dedentToZero_emitsOnlyOneDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals(1, tokens.size());
    }

    @Test
    void processLineStart_dedentToZero_emitsNoINDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals(0, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_dedent_tokenTypeIsDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals(TokenType.DEDENT, tokens.get(0).type());
    }

    @Test
    void processLineStart_dedent_tokenValueIsEmpty() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals("", tokens.get(0).value());
    }

    @Test
    void processLineStart_dedent_tokenLineIsCorrect() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        LexerCursor cursor = cursorFor("y");
        cursor.incrementLine();
        handler.processLineStart(cursor, tokens);
        assertEquals(2, tokens.get(0).line());
    }

    @Test
    void processLineStart_twoLevelDedentToZero_emitsTwoDEDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("c"), tokens);
        assertEquals(2, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_twoLevelDedentToZero_emitsNoINDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("c"), tokens);
        assertEquals(0, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_dedentToMidLevel_emitsOneDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("    c"), tokens);
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_dedentToMidLevel_emitsNoINDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("    c"), tokens);
        assertEquals(0, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void processLineStart_threeLevelDedentToZero_emitsThreeDEDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        handler.processLineStart(cursorFor("            c"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("d"), tokens);
        assertEquals(3, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_dedentAndReindent_correctSequence() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        handler.processLineStart(cursorFor("y"), tokens);
        handler.processLineStart(cursorFor("    z"), tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_ifElse_correctINDENTDEDENTBalance() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if x then:"), tokens);
        handler.processLineStart(cursorFor("    print x"), tokens);
        handler.processLineStart(cursorFor("else:"), tokens);
        handler.processLineStart(cursorFor("    print y"), tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void processLineStart_dedent_allTokensHaveEmptyValue() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("c"), tokens);
        tokens.forEach(t -> assertEquals("", t.value()));
    }

    @Test
    void processLineStart_dedent_cursorSitsOnContent() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        LexerCursor cursor = cursorFor("print");
        handler.processLineStart(cursor, tokens);
        assertEquals('p', cursor.currentChar());
    }

    @Test
    void processLineStart_dedentToZeroAfterTabIndent_emitsOneDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("\tx"), tokens);
        tokens.clear();
        handler.processLineStart(cursorFor("y"), tokens);
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    // =========================================================================
    // processLineStart — invalid dedent (BloopLexerException) — 10 tests
    // =========================================================================

    @Test
    void processLineStart_invalidDedent_throwsBloopLexerException() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("  y"), tokens));
    }

    @Test
    void processLineStart_invalidDedent_notPlainRuntimeException() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        Exception ex = assertThrows(Exception.class,
                () -> handler.processLineStart(cursorFor("  y"), tokens));
        assertInstanceOf(BloopLexerException.class, ex);
    }

    @Test
    void processLineStart_invalidDedent_messageContainsIndentationError() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        BloopLexerException ex = assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("  y"), tokens));
        assertTrue(ex.getMessage().contains("IndentationError"));
    }

    @Test
    void processLineStart_invalidDedent_messageContainsLexerError() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        BloopLexerException ex = assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("  y"), tokens));
        assertTrue(ex.getMessage().contains("Lexer error"));
    }

    @Test
    void processLineStart_invalidDedent_messageContainsLineNumber() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        LexerCursor cursor = cursorFor("  y");
        cursor.incrementLine(); // line 2
        BloopLexerException ex = assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursor, tokens));
        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    void processLineStart_invalidDedent_sourceLineIsCorrect() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        LexerCursor cursor = cursorFor("  y");
        cursor.incrementLine();
        cursor.incrementLine(); // line 3
        BloopLexerException ex = assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursor, tokens));
        assertEquals(3, ex.getSourceLine());
    }

    @Test
    void processLineStart_invalidDedent_isRuntimeException() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        BloopLexerException ex = assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("  y"), tokens));
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void processLineStart_invalidDedent_threeLevel_midLevelNotOpened() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("      c"), tokens));
    }

    @Test
    void processLineStart_invalidDedent_levelNeverOpened() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("        x"), tokens);
        assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("   y"), tokens));
    }

    @Test
    void processLineStart_validDedentDoesNotThrow() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        assertDoesNotThrow(
                () -> handler.processLineStart(cursorFor("y"), tokens));
    }

    // =========================================================================
    // closeAllOpenBlocks() — 12 tests
    // =========================================================================

    @Test
    void closeAllOpenBlocks_noOpenBlocks_emitsNoTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.closeAllOpenBlocks(1, tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void closeAllOpenBlocks_oneOpenBlock_emitsExactlyOneDEDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(5, tokens);
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void closeAllOpenBlocks_twoOpenBlocks_emitsTwoDEDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(10, tokens);
        assertEquals(2, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void closeAllOpenBlocks_threeOpenBlocks_emitsThreeDEDENTs() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        handler.processLineStart(cursorFor("            c"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(20, tokens);
        assertEquals(3, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void closeAllOpenBlocks_usesProvidedFinalLine() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(42, tokens);
        assertEquals(42, tokens.get(0).line());
    }

    @Test
    void closeAllOpenBlocks_allTokensHaveEmptyValue() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(5, tokens);
        tokens.forEach(t -> assertEquals("", t.value()));
    }

    @Test
    void closeAllOpenBlocks_emitsOnlyDEDENTTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(10, tokens);
        assertTrue(tokens.stream().allMatch(t -> t.type() == TokenType.DEDENT));
    }

    @Test
    void closeAllOpenBlocks_calledTwice_secondCallEmitsNothing() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        handler.closeAllOpenBlocks(5, tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(6, tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void closeAllOpenBlocks_allDEDENTsHaveSameFinalLine() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(99, tokens);
        tokens.forEach(t -> assertEquals(99, t.line()));
    }

    @Test
    void closeAllOpenBlocks_afterPartialManualDedent_closesRemaining() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    a"), tokens);
        handler.processLineStart(cursorFor("        b"), tokens);
        handler.processLineStart(cursorFor("    c"), tokens); // DEDENT to 4
        tokens.clear();
        handler.closeAllOpenBlocks(10, tokens);
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void closeAllOpenBlocks_lineZero_isValidFinalLine() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("    x"), tokens);
        tokens.clear();
        handler.closeAllOpenBlocks(0, tokens);
        assertEquals(0, tokens.get(0).line());
    }

    @Test
    void closeAllOpenBlocks_doesNotThrow_withNoOpenBlocks() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        assertDoesNotThrow(() -> handler.closeAllOpenBlocks(1, tokens));
    }

    // =========================================================================
    // consumeLeadingSpaces — via processLineStart (indirect) — 8 tests
    // =========================================================================

    @Test
    void consumeLeadingSpaces_singleSpace_emitsINDENT() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor(" x"), tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
    }

    @Test
    void consumeLeadingSpaces_oneTabEqualsFourSpaces_bothEmitINDENT() {
        IndentationHandler h1 = new IndentationHandler();
        IndentationHandler h2 = new IndentationHandler();
        List<Token> t1 = new ArrayList<>();
        List<Token> t2 = new ArrayList<>();
        h1.processLineStart(cursorFor("\tx"), t1);
        h2.processLineStart(cursorFor("    x"), t2);
        assertEquals(1, countOf(t1, TokenType.INDENT));
        assertEquals(1, countOf(t2, TokenType.INDENT));
    }

    @Test
    void consumeLeadingSpaces_mixedSpaceTab_advancesCursorPastAllWhitespace() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("  \tx");
        handler.processLineStart(cursor, tokens);
        assertEquals('x', cursor.currentChar());
    }

    @Test
    void consumeLeadingSpaces_onlySpaces_exhaustsCursor() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("    ");
        handler.processLineStart(cursor, tokens);
        assertTrue(cursor.isExhausted());
    }

    @Test
    void consumeLeadingSpaces_noLeadingWhitespace_positionStaysAtZero() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("abc");
        handler.processLineStart(cursor, tokens);
        assertEquals(0, cursor.getCurrentPosition());
    }

    @Test
    void consumeLeadingSpaces_stopAtNonWhitespace_cursorOnFirstNonSpace() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("    +");
        handler.processLineStart(cursor, tokens);
        assertEquals('+', cursor.currentChar());
    }

    @Test
    void consumeLeadingSpaces_onlyTab_exhaustsCursor() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        LexerCursor cursor = cursorFor("\t");
        handler.processLineStart(cursor, tokens);
        assertTrue(cursor.isExhausted());
    }

    @Test
    void consumeLeadingSpaces_emptyCursor_noTokensEmitted() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor(""), tokens);
        assertTrue(tokens.isEmpty());
    }

    // =========================================================================
    // Realistic full-program scenarios — 8 tests
    // =========================================================================

    @Test
    void scenario_singleBlock_openAndClose() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if x then:"), tokens);
        handler.processLineStart(cursorFor("    print x"), tokens);
        handler.closeAllOpenBlocks(2, tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void scenario_ifElseBlock_correctBalance() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if age >= 18 then:"), tokens);
        handler.processLineStart(cursorFor("    print \"adult\""), tokens);
        handler.processLineStart(cursorFor("else:"), tokens);
        handler.processLineStart(cursorFor("    print \"minor\""), tokens);
        handler.closeAllOpenBlocks(4, tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
        assertEquals(2, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void scenario_nestedRepeat_correctBalance() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("repeat 3 times:"), tokens);
        handler.processLineStart(cursorFor("    repeat 2 times:"), tokens);
        handler.processLineStart(cursorFor("        print \"inner\""), tokens);
        handler.processLineStart(cursorFor("    print \"outer\""), tokens);
        handler.closeAllOpenBlocks(4, tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
        assertEquals(2, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void scenario_emptyProgram_noTokens() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.closeAllOpenBlocks(1, tokens);
        assertTrue(tokens.isEmpty());
    }

    @Test
    void scenario_tabIndentedBlock_sameAsSpaces() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if x then:"), tokens);
        handler.processLineStart(cursorFor("\tprint x"), tokens);
        handler.closeAllOpenBlocks(2, tokens);
        assertEquals(1, countOf(tokens, TokenType.INDENT));
        assertEquals(1, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void scenario_gradedMarkBlock_threeLevel() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if marks >= 90 then:"), tokens);
        handler.processLineStart(cursorFor("    print \"A\""), tokens);
        handler.processLineStart(cursorFor("else:"), tokens);
        handler.processLineStart(cursorFor("    if marks >= 75 then:"), tokens);
        handler.processLineStart(cursorFor("        print \"B\""), tokens);
        handler.processLineStart(cursorFor("    else:"), tokens);
        handler.processLineStart(cursorFor("        print \"C\""), tokens);
        handler.closeAllOpenBlocks(7, tokens);
        assertEquals(4, countOf(tokens, TokenType.INDENT));
        assertEquals(4, countOf(tokens, TokenType.DEDENT));
    }

    @Test
    void scenario_invalidDedentInProgram_throwsMidway() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("if x then:"), tokens);
        handler.processLineStart(cursorFor("    print x"), tokens);
        assertThrows(BloopLexerException.class,
                () -> handler.processLineStart(cursorFor("  oops"), tokens));
    }

    @Test
    void scenario_multipleSequentialBlocks_correctBalance() {
        IndentationHandler handler = new IndentationHandler();
        List<Token> tokens = new ArrayList<>();
        handler.processLineStart(cursorFor("repeat 2 times:"), tokens);
        handler.processLineStart(cursorFor("    print \"a\""), tokens);
        handler.processLineStart(cursorFor("repeat 3 times:"), tokens);
        handler.processLineStart(cursorFor("    print \"b\""), tokens);
        handler.closeAllOpenBlocks(4, tokens);
        assertEquals(2, countOf(tokens, TokenType.INDENT));
        assertEquals(2, countOf(tokens, TokenType.DEDENT));
    }
}