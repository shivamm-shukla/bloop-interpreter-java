package bloop.parser;

import bloop.exceptions.BloopParseException;
import bloop.instruction.*;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parser")
class ParserTest {

    private Parser parser;

    @BeforeEach
    void setUp() {
        parser = Parser.createDefault();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static Token tok(TokenType type, String value) {
        return new Token(type, value, 1);
    }

    private static Token tok(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    private static Token eof() {
        return tok(TokenType.EOF, "");
    }

    private static Token nl() {
        return tok(TokenType.NEWLINE, "");
    }

    private List<Instruction> parse(Token... tokens) {
        return parser.parse(List.of(tokens));
    }

    // ── null / empty input ────────────────────────────────────────────────

    @Test
    @DisplayName("null token list throws BloopParseException")
    void nullTokenListThrows() {
        assertThrows(BloopParseException.class, () -> parser.parse(null));
    }

    @Test
    @DisplayName("only EOF token gives empty instruction list")
    void onlyEOF() {
        List<Instruction> result = parse(eof());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("only NEWLINE then EOF gives empty instruction list")
    void onlyNewlineEOF() {
        List<Instruction> result = parse(nl(), eof());
        assertTrue(result.isEmpty());
    }

    // ── single statements ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Single statement parsing")
    class SingleStatements {

        @Test
        @DisplayName("parses 'put 5 into x' as AssignInstruction")
        void parsePut() {
            List<Instruction> result = parse(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.NUMBER, "5"),
                    tok(TokenType.INTO, "into"),
                    tok(TokenType.IDENTIFIER, "x"),
                    eof());
            assertEquals(1, result.size());
            assertInstanceOf(AssignInstruction.class, result.get(0));
        }

        @Test
        @DisplayName("parses 'print 42' as PrintInstruction")
        void parsePrint() {
            List<Instruction> result = parse(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "42"),
                    eof());
            assertEquals(1, result.size());
            assertInstanceOf(PrintInstruction.class, result.get(0));
        }

        @Test
        @DisplayName("parses if statement as IfInstruction")
        void parseIf() {
            List<Instruction> result = parse(
                    tok(TokenType.IF, "if"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.EQUAL_EQUAL, "=="),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.THEN, "then"),
                    tok(TokenType.COLON, ":"),
                    nl(),
                    tok(TokenType.INDENT, ""),
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    nl(),
                    tok(TokenType.DEDENT, ""),
                    eof());
            assertEquals(1, result.size());
            assertInstanceOf(IfInstruction.class, result.get(0));
        }

        @Test
        @DisplayName("parses repeat statement as RepeatInstruction")
        void parseRepeat() {
            List<Instruction> result = parse(
                    tok(TokenType.REPEAT, "repeat"),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.TIMES, "times"),
                    tok(TokenType.COLON, ":"),
                    nl(),
                    tok(TokenType.INDENT, ""),
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    nl(),
                    tok(TokenType.DEDENT, ""),
                    eof());
            assertEquals(1, result.size());
            assertInstanceOf(RepeatInstruction.class, result.get(0));
        }
    }

    // ── multiple statements ───────────────────────────────────────────────

    @Test
    @DisplayName("parses multiple statements in sequence")
    void multipleStatements() {
        List<Instruction> result = parse(
                tok(TokenType.PUT, "put"),
                tok(TokenType.NUMBER, "1"),
                tok(TokenType.INTO, "into"),
                tok(TokenType.IDENTIFIER, "x"),
                nl(),
                tok(TokenType.PRINT, "print"),
                tok(TokenType.IDENTIFIER, "x"),
                eof());
        assertEquals(2, result.size());
        assertInstanceOf(AssignInstruction.class, result.get(0));
        assertInstanceOf(PrintInstruction.class, result.get(1));
    }

    @Test
    @DisplayName("returned list is unmodifiable")
    void resultIsImmutable() {
        List<Instruction> result = parse(eof());
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(null));
    }

    // ── error cases ───────────────────────────────────────────────────────

    @Test
    @DisplayName("unknown starting token throws BloopParseException")
    void unknownToken() {
        assertThrows(BloopParseException.class, () ->
                parse(tok(TokenType.NUMBER, "42", 3), eof()));
    }

    @Test
    @DisplayName("if body missing INDENT throws BloopParseException")
    void ifBodyMissingIndent() {
        assertThrows(BloopParseException.class, () ->
                parse(
                        tok(TokenType.IF, "if"),
                        tok(TokenType.NUMBER, "1"),
                        tok(TokenType.THEN, "then"),
                        tok(TokenType.COLON, ":"),
                        nl(),
                        // Missing INDENT
                        tok(TokenType.PRINT, "print"),
                        tok(TokenType.NUMBER, "1"),
                        eof()));
    }

    // ── newline handling ──────────────────────────────────────────────────

    @Test
    @DisplayName("leading newlines before first statement are skipped")
    void leadingNewlines() {
        List<Instruction> result = parse(
                nl(), nl(),
                tok(TokenType.PRINT, "print"),
                tok(TokenType.NUMBER, "1"),
                eof());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("trailing newlines after last statement are skipped")
    void trailingNewlines() {
        List<Instruction> result = parse(
                tok(TokenType.PRINT, "print"),
                tok(TokenType.NUMBER, "1"),
                nl(), nl(),
                eof());
        assertEquals(1, result.size());
    }
}