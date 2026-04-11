package bloop.parser;

import bloop.exceptions.BloopParseException;
import bloop.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Parser.
 *
 * The Tokenizer is used to generate real token lists from source strings,
 * so these tests cover the full lex → parse pipeline.
 */
@DisplayName("Parser")
class ParserTest {

    private Parser parser;

    @BeforeEach
    void createParser() {
        parser = Parser.createDefault();
    }

    // ── helper ────────────────────────────────────────────────────────────

    private List<Instruction> parse(String source) {
        List<Token> tokens = new Tokenizer(source).tokenize();
        return parser.parse(tokens);
    }

    // ── empty program ─────────────────────────────────────────────────────

    @Test
    @DisplayName("empty source produces empty instruction list")
    void emptySource_producesEmptyInstructionList() {
        List<Instruction> instructions = parse("");
        assertTrue(instructions.isEmpty());
    }

    @Test
    @DisplayName("result list is immutable")
    void resultList_isImmutable() {
        List<Instruction> instructions = parse("print x");
        assertThrows(UnsupportedOperationException.class, () -> instructions.add(null));
    }

    // ── PUT statement ─────────────────────────────────────────────────────

    @Test
    @DisplayName("parses 'put 10 into x' as AssignInstruction")
    void parsesPutStatement() {
        List<Instruction> instructions = parse("put 10 into x");
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("parses 'put \"hello\" into greeting' as AssignInstruction")
    void parsesPutStringStatement() {
        List<Instruction> instructions = parse("put \"hello\" into greeting");
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("missing 'into' in put statement throws BloopParseException")
    void putWithoutInto_throwsParseException() {
        assertThrows(BloopParseException.class, () -> parse("put 10 x"));
    }

    @Test
    @DisplayName("missing identifier after 'into' throws BloopParseException")
    void putWithoutIdentifier_throwsParseException() {
        assertThrows(BloopParseException.class, () -> parse("put 10 into"));
    }

    // ── PRINT statement ───────────────────────────────────────────────────

    @Test
    @DisplayName("parses 'print z' as PrintInstruction")
    void parsesPrintStatement() {
        List<Instruction> instructions = parse("print z");
        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("bare 'print' with no expression throws BloopParseException")
    void barePrint_throwsParseException() {
        assertThrows(BloopParseException.class, () -> parse("print\n"));
    }

    // ── IF statement ──────────────────────────────────────────────────────

    @Test
    @DisplayName("parses if-then block as IfInstruction")
    void parsesIfStatement() {
        String source = "if score > 50 then:\n    print \"Pass\"\n";
        List<Instruction> instructions = parse(source);
        assertEquals(1, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("missing 'then' throws BloopParseException")
    void ifWithoutThen_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("if score > 50:\n    print \"Pass\"\n"));
    }

    @Test
    @DisplayName("missing ':' after then throws BloopParseException")
    void ifWithoutColon_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("if score > 50 then\n    print \"Pass\"\n"));
    }

    @Test
    @DisplayName("empty if body throws BloopParseException")
    void emptyIfBody_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("if x > 0 then:\n"));
    }

    // ── REPEAT statement ──────────────────────────────────────────────────

    @Test
    @DisplayName("parses repeat block as RepeatInstruction")
    void parsesRepeatStatement() {
        String source = "repeat 4 times:\n    print \"hello\"\n";
        List<Instruction> instructions = parse(source);
        assertEquals(1, instructions.size());
        assertInstanceOf(RepeatInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("float repeat count throws BloopParseException")
    void floatRepeatCount_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("repeat 3.5 times:\n    print \"hi\"\n"));
    }

    @Test
    @DisplayName("negative repeat count throws BloopParseException")
    void negativeRepeatCount_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("repeat -1 times:\n    print \"hi\"\n"));
    }

    @Test
    @DisplayName("missing 'times' throws BloopParseException")
    void repeatWithoutTimes_throwsParseException() {
        assertThrows(BloopParseException.class,
                () -> parse("repeat 3:\n    print \"hi\"\n"));
    }

    // ── Expression precedence ─────────────────────────────────────────────

    @Test
    @DisplayName("parses 'put x + y * 2 into result' as single AssignInstruction")
    void parsesComplexExpression() {
        List<Instruction> instructions = parse("put x + y * 2 into result");
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    @DisplayName("parses parenthesised expression")
    void parsesParenthesisedExpression() {
        List<Instruction> instructions = parse("put (x + y) * 2 into result");
        assertEquals(1, instructions.size());
    }

    // ── Multi-statement programs ──────────────────────────────────────────

    @Test
    @DisplayName("parses multiple statements separated by newlines")
    void parsesMultipleStatements() {
        String source = "put 10 into x\nput 20 into y\nprint x\n";
        List<Instruction> instructions = parse(source);
        assertEquals(3, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
        assertInstanceOf(AssignInstruction.class, instructions.get(1));
        assertInstanceOf(PrintInstruction.class,  instructions.get(2));
    }

    @Test
    @DisplayName("blank lines between statements are skipped")
    void blankLinesBetweenStatements_areSkipped() {
        String source = "put 10 into x\n\nprint x\n";
        List<Instruction> instructions = parse(source);
        assertEquals(2, instructions.size());
    }

    // ── null guard ────────────────────────────────────────────────────────

    @Test
    @DisplayName("null token list throws immediately")
    void nullTokenList_throwsImmediately() {
        assertThrows(BloopParseException.class, () -> parser.parse(null));
    }

    // ── Unknown token ─────────────────────────────────────────────────────

    @Test
    @DisplayName("token not starting a valid statement throws BloopParseException")
    void unknownStartToken_throwsParseException() {
        // Manually construct a token list with an unexpected leading token
        List<Token> tokens = List.of(
                new Token(bloop.token.TokenType.PLUS, "+", 1),
                new Token(bloop.token.TokenType.EOF,  "",  1));
        assertThrows(BloopParseException.class, () -> parser.parse(tokens));
    }
}
