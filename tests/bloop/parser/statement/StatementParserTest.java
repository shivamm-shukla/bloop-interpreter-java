package bloop.parser.statement;

import bloop.exceptions.BloopParseException;
import bloop.instruction.*;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Statement Parsers")
class StatementParserTest {

    private ExpressionParser ep;

    @BeforeEach
    void setUp() {
        ep = new ExpressionParser();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static Token tok(TokenType type, String value) {
        return new Token(type, value, 1);
    }

    private static Token tok(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    /** BlockParser that returns a fixed single-instruction body */
    private static BlockParser fixedBlock(Instruction... instructions) {
        return () -> List.of(instructions);
    }

    /** BlockParser that returns an empty body */
    private static BlockParser emptyBlock() {
        return List::of;
    }

    private TokenCursor cursor(Token... tokens) {
        List<Token> list = new java.util.ArrayList<>(List.of(tokens));
        list.add(tok(TokenType.EOF, ""));
        return new TokenCursor(list);
    }

    // ── PutStatementParser ────────────────────────────────────────────────

    @Nested
    @DisplayName("PutStatementParser")
    class PutTests {

        private PutStatementParser parser;

        @BeforeEach
        void setUp() { parser = new PutStatementParser(ep); }

        @Test
        @DisplayName("triggerToken is PUT")
        void triggerToken() {
            assertEquals(TokenType.PUT, parser.triggerToken());
        }

        @Test
        @DisplayName("parses 'put 5 into x' into AssignInstruction")
        void basicParse() {
            TokenCursor c = cursor(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.NUMBER, "5"),
                    tok(TokenType.INTO, "into"),
                    tok(TokenType.IDENTIFIER, "x"));
            Instruction instr = parser.parse(c, emptyBlock());
            assertInstanceOf(AssignInstruction.class, instr);
        }

        @Test
        @DisplayName("missing 'into' throws BloopParseException")
        void missingInto() {
            TokenCursor c = cursor(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.NUMBER, "5"),
                    tok(TokenType.IDENTIFIER, "x"));
            assertThrows(BloopParseException.class, () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("missing variable name throws BloopParseException")
        void missingVarName() {
            TokenCursor c = cursor(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.NUMBER, "5"),
                    tok(TokenType.INTO, "into"),
                    tok(TokenType.NUMBER, "99")); // number instead of identifier
            assertThrows(BloopParseException.class, () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("parses string expression: put \"hi\" into msg")
        void putStringExpression() {
            TokenCursor c = cursor(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.STRING, "hi"),
                    tok(TokenType.INTO, "into"),
                    tok(TokenType.IDENTIFIER, "msg"));
            assertInstanceOf(AssignInstruction.class, parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("parses arithmetic expression: put 2 + 3 into y")
        void putArithmeticExpression() {
            TokenCursor c = cursor(
                    tok(TokenType.PUT, "put"),
                    tok(TokenType.NUMBER, "2"),
                    tok(TokenType.PLUS, "+"),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.INTO, "into"),
                    tok(TokenType.IDENTIFIER, "y"));
            assertInstanceOf(AssignInstruction.class, parser.parse(c, emptyBlock()));
        }
    }

    // ── PrintStatementParser ──────────────────────────────────────────────

    @Nested
    @DisplayName("PrintStatementParser")
    class PrintTests {

        private PrintStatementParser parser;

        @BeforeEach
        void setUp() { parser = new PrintStatementParser(ep); }

        @Test
        @DisplayName("triggerToken is PRINT")
        void triggerToken() {
            assertEquals(TokenType.PRINT, parser.triggerToken());
        }

        @Test
        @DisplayName("parses 'print 42' into PrintInstruction")
        void basicPrint() {
            TokenCursor c = cursor(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "42"));
            assertInstanceOf(PrintInstruction.class, parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("parses 'print \"hello\"' into PrintInstruction")
        void printString() {
            TokenCursor c = cursor(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.STRING, "hello"));
            assertInstanceOf(PrintInstruction.class, parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("print with no expression throws BloopParseException")
        void printNoExpr() {
            TokenCursor c = cursor(tok(TokenType.PRINT, "print", 3));
            assertThrows(BloopParseException.class, () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("print followed by NEWLINE only throws BloopParseException")
        void printFollowedByNewline() {
            TokenCursor c = cursor(
                    tok(TokenType.PRINT, "print", 2),
                    tok(TokenType.NEWLINE, ""));
            assertThrows(BloopParseException.class, () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("parses expression: print 1 + 2")
        void printExpression() {
            TokenCursor c = cursor(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.PLUS, "+"),
                    tok(TokenType.NUMBER, "2"));
            assertInstanceOf(PrintInstruction.class, parser.parse(c, emptyBlock()));
        }
    }

    // ── IfStatementParser ─────────────────────────────────────────────────

    @Nested
    @DisplayName("IfStatementParser")
    class IfTests {

        private IfStatementParser parser;

        @BeforeEach
        void setUp() { parser = new IfStatementParser(ep); }

        @Test
        @DisplayName("triggerToken is IF")
        void triggerToken() {
            assertEquals(TokenType.IF, parser.triggerToken());
        }

        @Test
        @DisplayName("parses basic if without else")
        void basicIfNoElse() {
            TokenCursor c = cursor(
                    tok(TokenType.IF, "if"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.EQUAL_EQUAL, "=="),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.THEN, "then"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));
            Instruction instr = parser.parse(c,
                    fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1))));
            assertInstanceOf(IfInstruction.class, instr);
            assertFalse(((IfInstruction) instr).hasElseBlock());
        }

        @Test
        @DisplayName("missing 'then' throws BloopParseException")
        void missingThen() {
            TokenCursor c = cursor(
                    tok(TokenType.IF, "if"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.COLON, ":"));
            assertThrows(BloopParseException.class,
                    () -> parser.parse(c, fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1)))));
        }

        @Test
        @DisplayName("missing ':' after then throws BloopParseException")
        void missingColon() {
            TokenCursor c = cursor(
                    tok(TokenType.IF, "if"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.THEN, "then"));
            assertThrows(BloopParseException.class,
                    () -> parser.parse(c, fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1)))));
        }

        @Test
        @DisplayName("empty then body throws BloopParseException")
        void emptyThenBody() {
            TokenCursor c = cursor(
                    tok(TokenType.IF, "if", 5),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.THEN, "then"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));
            assertThrows(BloopParseException.class,
                    () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("parses if with else block — hasElseBlock true")
        void ifWithElse() {
            // We need a cursor that will detect 'else' after the then-block is parsed
            // Since we control blockParser, we simulate two calls: thenBlock then elseBlock
            PrintInstruction printInstr = new PrintInstruction(new bloop.ast.NumberNode(1));
            // First call: thenBody, second call: elseBody
            final int[] callCount = {0};
            BlockParser twoCallBlock = () -> {
                callCount[0]++;
                return List.of(printInstr);
            };

            TokenCursor c = cursor(
                    tok(TokenType.IF, "if"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.EQUAL_EQUAL, "=="),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.THEN, "then"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""),
                    tok(TokenType.ELSE, "else"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));

            Instruction instr = parser.parse(c, twoCallBlock);
            assertInstanceOf(IfInstruction.class, instr);
            assertTrue(((IfInstruction) instr).hasElseBlock());
        }
    }

    // ── RepeatStatementParser ─────────────────────────────────────────────

    @Nested
    @DisplayName("RepeatStatementParser")
    class RepeatTests {

        private RepeatStatementParser parser;

        @BeforeEach
        void setUp() { parser = new RepeatStatementParser(ep); }

        @Test
        @DisplayName("triggerToken is REPEAT")
        void triggerToken() {
            assertEquals(TokenType.REPEAT, parser.triggerToken());
        }

        @Test
        @DisplayName("parses 'repeat 3 times:' into RepeatInstruction")
        void basicRepeat() {
            TokenCursor c = cursor(
                    tok(TokenType.REPEAT, "repeat"),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.TIMES, "times"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));
            Instruction instr = parser.parse(c,
                    fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1))));
            assertInstanceOf(RepeatInstruction.class, instr);
        }

        @Test
        @DisplayName("missing 'times' throws BloopParseException")
        void missingTimes() {
            TokenCursor c = cursor(
                    tok(TokenType.REPEAT, "repeat"),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.COLON, ":"));
            assertThrows(BloopParseException.class,
                    () -> parser.parse(c, fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1)))));
        }

        @Test
        @DisplayName("missing ':' after times throws BloopParseException")
        void missingColon() {
            TokenCursor c = cursor(
                    tok(TokenType.REPEAT, "repeat"),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.TIMES, "times"));
            assertThrows(BloopParseException.class,
                    () -> parser.parse(c, fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1)))));
        }

        @Test
        @DisplayName("empty repeat body throws BloopParseException")
        void emptyBody() {
            TokenCursor c = cursor(
                    tok(TokenType.REPEAT, "repeat", 2),
                    tok(TokenType.NUMBER, "3"),
                    tok(TokenType.TIMES, "times"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));
            assertThrows(BloopParseException.class, () -> parser.parse(c, emptyBlock()));
        }

        @Test
        @DisplayName("repeat with variable count expression parses successfully")
        void repeatWithVariable() {
            TokenCursor c = cursor(
                    tok(TokenType.REPEAT, "repeat"),
                    tok(TokenType.IDENTIFIER, "n"),
                    tok(TokenType.TIMES, "times"),
                    tok(TokenType.COLON, ":"),
                    tok(TokenType.NEWLINE, ""));
            Instruction instr = parser.parse(c,
                    fixedBlock(new PrintInstruction(new bloop.ast.NumberNode(1))));
            assertInstanceOf(RepeatInstruction.class, instr);
        }
    }
}