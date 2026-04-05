package bloop.token;

import bloop.exceptions.BloopLexerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Tokenizer / lexer layer.
 *
 * Each test covers exactly one behaviour so failures point
 * directly to the broken rule.
 */
@DisplayName("Tokenizer")
class TokenizerTest {

    // ── helpers ───────────────────────────────────────────────────────────

    private List<Token> lex(String source) {
        return new Tokenizer(source).tokenize();
    }

    /** Returns every token except the trailing EOF. */
    private List<Token> lexWithoutEof(String source) {
        List<Token> all = lex(source);
        return all.subList(0, all.size() - 1);
    }

    private void assertType(Token token, TokenType expected) {
        assertEquals(expected, token.getType(),
                "Expected token type " + expected + " but got " + token.getType()
                + " (value=\"" + token.getValue() + "\")");
    }

    private void assertToken(Token token, TokenType type, String value) {
        assertType(token, type);
        assertEquals(value, token.getValue());
    }

    // ── EOF guarantee ─────────────────────────────────────────────────────

    @Test
    @DisplayName("empty source produces only EOF")
    void emptySource_producesOnlyEof() {
        List<Token> tokens = lex("");
        assertEquals(1, tokens.size());
        assertType(tokens.get(0), TokenType.EOF);
    }

    @Test
    @DisplayName("last token is always EOF")
    void lastToken_isAlwaysEof() {
        List<Token> tokens = lex("print x");
        assertType(tokens.get(tokens.size() - 1), TokenType.EOF);
    }

    // ── Keywords ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("recognises all BLOOP keywords")
    void recognisesAllKeywords() {
        String source = "put into print if then repeat times";
        List<Token> tokens = lexWithoutEof(source);

        TokenType[] expected = {
            TokenType.PUT, TokenType.INTO, TokenType.PRINT,
            TokenType.IF,  TokenType.THEN, TokenType.REPEAT, TokenType.TIMES
        };

        assertEquals(expected.length, tokens.size());
        for (int i = 0; i < expected.length; i++) {
            assertType(tokens.get(i), expected[i]);
        }
    }

    @Test
    @DisplayName("unknown word becomes IDENTIFIER")
    void unknownWord_becomesIdentifier() {
        List<Token> tokens = lexWithoutEof("myVariable");
        assertEquals(1, tokens.size());
        assertToken(tokens.get(0), TokenType.IDENTIFIER, "myVariable");
    }

    @Test
    @DisplayName("identifier may contain underscore and digits")
    void identifier_allowsUnderscoreAndDigits() {
        List<Token> tokens = lexWithoutEof("total_2");
        assertEquals(1, tokens.size());
        assertType(tokens.get(0), TokenType.IDENTIFIER);
    }

    // ── Number literals ───────────────────────────────────────────────────

    @Test
    @DisplayName("scans integer literal")
    void scansIntegerLiteral() {
        List<Token> tokens = lexWithoutEof("42");
        assertToken(tokens.get(0), TokenType.NUMBER, "42");
    }

    @Test
    @DisplayName("scans decimal literal")
    void scansDecimalLiteral() {
        List<Token> tokens = lexWithoutEof("3.14");
        assertToken(tokens.get(0), TokenType.NUMBER, "3.14");
    }

    @Test
    @DisplayName("rejects number with two decimal points")
    void rejectsNumberWithTwoDecimalPoints() {
        assertThrows(BloopLexerException.class, () -> lex("3.1.4"));
    }

    // ── String literals ───────────────────────────────────────────────────

    @Test
    @DisplayName("scans string literal content without quotes")
    void scansStringLiteralContent() {
        List<Token> tokens = lexWithoutEof("\"Hello from BLOOP\"");
        assertToken(tokens.get(0), TokenType.STRING, "Hello from BLOOP");
    }

    @Test
    @DisplayName("scans empty string literal")
    void scansEmptyStringLiteral() {
        List<Token> tokens = lexWithoutEof("\"\"");
        assertToken(tokens.get(0), TokenType.STRING, "");
    }

    @Test
    @DisplayName("resolves \\n escape sequence inside string")
    void resolvesNewlineEscapeInString() {
        List<Token> tokens = lexWithoutEof("\"line1\\nline2\"");
        assertEquals("line1\nline2", tokens.get(0).getValue());
    }

    @Test
    @DisplayName("throws on unterminated string at end of file")
    void throwsOnUnterminatedStringAtEof() {
        assertThrows(BloopLexerException.class, () -> lex("\"unclosed"));
    }

    @Test
    @DisplayName("throws on string containing a raw newline")
    void throwsOnStringContainingRawNewline() {
        assertThrows(BloopLexerException.class, () -> lex("\"line1\nline2\""));
    }

    // ── Arithmetic operators ──────────────────────────────────────────────

    @Test
    @DisplayName("scans all single-character arithmetic operators")
    void scansSingleCharArithmeticOperators() {
        List<Token> tokens = lexWithoutEof("+ - * /");
        assertType(tokens.get(0), TokenType.PLUS);
        assertType(tokens.get(1), TokenType.MINUS);
        assertType(tokens.get(2), TokenType.STAR);
        assertType(tokens.get(3), TokenType.SLASH);
    }

    // ── Comparison operators ──────────────────────────────────────────────

    @Test
    @DisplayName("scans > and >= correctly")
    void scansGreaterAndGreaterEqual() {
        List<Token> tokens = lexWithoutEof("> >=");
        assertType(tokens.get(0), TokenType.GREATER);
        assertType(tokens.get(1), TokenType.GREATER_EQUAL);
    }

    @Test
    @DisplayName("scans < and <= correctly")
    void scansLessAndLessEqual() {
        List<Token> tokens = lexWithoutEof("< <=");
        assertType(tokens.get(0), TokenType.LESS);
        assertType(tokens.get(1), TokenType.LESS_EQUAL);
    }

    @Test
    @DisplayName("scans == correctly")
    void scansEqualEqual() {
        List<Token> tokens = lexWithoutEof("==");
        assertType(tokens.get(0), TokenType.EQUAL_EQUAL);
    }

    @Test
    @DisplayName("scans != correctly")
    void scansNotEqual() {
        List<Token> tokens = lexWithoutEof("!=");
        assertType(tokens.get(0), TokenType.NOT_EQUAL);
    }

    @Test
    @DisplayName("throws on bare ! with no following =")
    void throwsOnBareExclamation() {
        assertThrows(BloopLexerException.class, () -> lex("!"));
    }

    // ── Symbols ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("scans colon, comma, parentheses")
    void scansSymbols() {
        List<Token> tokens = lexWithoutEof(": , ( )");
        assertType(tokens.get(0), TokenType.COLON);
        assertType(tokens.get(1), TokenType.COMMA);
        assertType(tokens.get(2), TokenType.LEFT_PAREN);
        assertType(tokens.get(3), TokenType.RIGHT_PAREN);
    }

    // ── Whitespace and comments ───────────────────────────────────────────

    @Test
    @DisplayName("ignores inline whitespace between tokens")
    void ignoresInlineWhitespace() {
        List<Token> a = lexWithoutEof("x+y");
        List<Token> b = lexWithoutEof("x + y");
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getType(), b.get(i).getType());
        }
    }

    @Test
    @DisplayName("ignores line comments starting with #")
    void ignoresLineComments() {
        List<Token> tokens = lexWithoutEof("# this is a comment");
        assertTrue(tokens.isEmpty(), "Comment-only line should produce no tokens");
    }

    @Test
    @DisplayName("code after # on same line is ignored")
    void ignoresCodeAfterHashOnSameLine() {
        List<Token> tokens = lexWithoutEof("print x # print the value");
        assertEquals(2, tokens.size());
        assertType(tokens.get(0), TokenType.PRINT);
        assertType(tokens.get(1), TokenType.IDENTIFIER);
    }

    // ── Newline and line tracking ─────────────────────────────────────────

    @Test
    @DisplayName("emits NEWLINE token for each line break")
    void emitsNewlineToken() {
        List<Token> tokens = lexWithoutEof("print x\nprint y");
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.NEWLINE));
    }

    @Test
    @DisplayName("tracks line numbers across newlines")
    void tracksLineNumbers() {
        List<Token> tokens = lex("print x\nprint y");
        Token printY = tokens.stream()
                .filter(t -> t.getType() == TokenType.PRINT)
                .reduce((a, b) -> b) // last PRINT
                .orElseThrow();
        assertEquals(2, printY.getLine());
    }

    // ── Indentation ───────────────────────────────────────────────────────

    @Test
    @DisplayName("emits INDENT when indentation increases")
    void emitsIndentOnIncrease() {
        String source = "if x > 0 then:\n    print x\n";
        List<Token> tokens = lex(source);
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.INDENT));
    }

    @Test
    @DisplayName("emits DEDENT when indentation decreases")
    void emitsDedentOnDecrease() {
        String source = "if x > 0 then:\n    print x\nprint y\n";
        List<Token> tokens = lex(source);
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.DEDENT));
    }

    // ── Full program tokenisation ─────────────────────────────────────────

    @Test
    @DisplayName("tokenises 'put 10 into x' into correct sequence")
    void tokenisesPutStatement() {
        List<Token> tokens = lexWithoutEof("put 10 into x");
        assertEquals(4, tokens.size());
        assertType(tokens.get(0), TokenType.PUT);
        assertType(tokens.get(1), TokenType.NUMBER);
        assertType(tokens.get(2), TokenType.INTO);
        assertType(tokens.get(3), TokenType.IDENTIFIER);
    }

    @Test
    @DisplayName("result list is immutable")
    void resultIsImmutable() {
        List<Token> tokens = lex("print x");
        assertThrows(UnsupportedOperationException.class, () -> tokens.add(null));
    }

    // ── Unknown characters ────────────────────────────────────────────────

    @Test
    @DisplayName("throws on completely unknown character")
    void throwsOnUnknownCharacter() {
        assertThrows(BloopLexerException.class, () -> lex("@"));
    }
}
