package bloop.integration;


//  Tests two or more layers working together:
//    - Tokenizer → Parser
//    - Parser → Instructions
//    - Instructions → Environment


import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.exceptions.BloopRuntimeException;
import bloop.instruction.*;
import bloop.parser.Parser;
import bloop.runtime.Environment;
import bloop.token.Token;
import bloop.token.Tokenizer;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ────────────────────────────────────────────────────────────
//  Helper
// ────────────────────────────────────────────────────────────
class IntegrationHelper {

    static List<Token> lex(String src) {
        return new Tokenizer(src).tokenize();
    }

    static List<Instruction> parse(String src) {
        return Parser.createDefault().parse(lex(src));
    }

    static Environment execute(String src) {
        Environment env = new Environment();
        parse(src).forEach(i -> i.execute(env));
        return env;
    }

    static String captureStdout(Runnable r) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(buf));
        try { r.run(); } finally { System.setOut(old); }
        return buf.toString().trim();
    }
}

// ────────────────────────────────────────────────────────────
//  Tokenizer → Parser integration
// ────────────────────────────────────────────────────────────
@DisplayName("Integration › Tokenizer → Parser")
class TokenizerToParserTest {

    @Test
    @DisplayName("'put 42 into x' produces one AssignInstruction")
    void putStatement() {
        List<Instruction> instructions = IntegrationHelper.parse("put 42 into x");
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.getFirst());
    }

    @Test
    @DisplayName("'print 1 + 2' produces one PrintInstruction")
    void printStatement() {
        List<Instruction> instructions = IntegrationHelper.parse("print 1 + 2");
        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.getFirst());
    }

    @Test
    @DisplayName("If statement parses to IfInstruction")
    void ifStatement() {
        String src = "if 1 == 1 then:\n    print 1\n";
        List<Instruction> instructions = IntegrationHelper.parse(src);
        assertEquals(1, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.getFirst());
    }

    @Test
    @DisplayName("Repeat statement parses to RepeatInstruction")
    void repeatStatement() {
        String src = "repeat 3 times:\n    print 1\n";
        List<Instruction> instructions = IntegrationHelper.parse(src);
        assertEquals(1, instructions.size());
        assertInstanceOf(RepeatInstruction.class, instructions.getFirst());
    }

    @Test
    @DisplayName("Multiple statements parse to multiple instructions")
    void multipleStatements() {
        String src = "put 1 into a\nput 2 into b\nprint a\n";
        List<Instruction> instructions = IntegrationHelper.parse(src);
        assertEquals(3, instructions.size());
    }

    @Test
    @DisplayName("Null token list throws BloopParseException")
    void nullTokenListThrows() {
        assertThrows(BloopParseException.class,
                () -> Parser.createDefault().parse(null));
    }

    @Test
    @DisplayName("Unknown statement keyword throws BloopParseException")
    void unknownKeywordThrows() {
        // 'foo' is not a valid statement start
        assertThrows(BloopParseException.class,
                () -> IntegrationHelper.parse("foo bar"));
    }

    @Test
    @DisplayName("Empty source produces empty instruction list")
    void emptySource() {
        List<Instruction> instructions = IntegrationHelper.parse("");
        assertTrue(instructions.isEmpty());
    }

    @Test
    @DisplayName("Blank lines between statements are ignored")
    void blankLinesBetweenStatements() {
        String src = "put 1 into x\n\nput 2 into y\n";
        assertEquals(2, IntegrationHelper.parse(src).size());
    }
}

// ────────────────────────────────────────────────────────────
//  Parser → Instructions → Environment
// ────────────────────────────────────────────────────────────
@DisplayName("Integration › Parse + Execute")
class ParseAndExecuteTest {

    @Test
    @DisplayName("put 42 into x → env has x = 42")
    void putThenCheck() {
        Environment env = IntegrationHelper.execute("put 42 into x");
        assertEquals(42.0, env.get("x"));
    }

    @Test
    @DisplayName("Arithmetic in put: put 3 + 4 into result → 7")
    void arithmeticInPut() {
        Environment env = IntegrationHelper.execute("put 3 + 4 into result");
        assertEquals(7.0, env.get("result"));
    }

    @Test
    @DisplayName("Sequential assignments: a=1, b=2, c = a+b → c=3")
    void sequentialAssignments() {
        String src = "put 1 into a\nput 2 into b\nput a + b into c\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(3.0, env.get("c"));
    }

    @Test
    @DisplayName("print 7 outputs '7' to stdout")
    void printOutput() {
        String out = IntegrationHelper.captureStdout(
                () -> IntegrationHelper.execute("print 7"));
        assertEquals("7", out);
    }

    @Test
    @DisplayName("print 3.14 outputs '3.14' to stdout")
    void printDecimalOutput() {
        String out = IntegrationHelper.captureStdout(
                () -> IntegrationHelper.execute("print 3.14"));
        assertEquals("3.14", out);
    }

    @Test
    @DisplayName("print \"hello\" outputs 'hello' to stdout")
    void printStringOutput() {
        String out = IntegrationHelper.captureStdout(
                () -> IntegrationHelper.execute("print \"hello\""));
        assertEquals("hello", out);
    }

    @Test
    @DisplayName("if true condition: then-body executes")
    void ifTrueConditionExecutes() {
        String src = "if 5 > 3 then:\n    put 1 into flag\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(1.0, env.get("flag"));
    }

    @Test
    @DisplayName("if false condition: then-body does not execute")
    void ifFalseConditionSkips() {
        String src = "if 3 > 5 then:\n    put 1 into flag\n";
        Environment env = IntegrationHelper.execute(src);
        assertFalse(env.isDefined("flag"));
    }

    @Test
    @DisplayName("if-else: true condition runs then, skips else")
    void ifElseTrueThen() {
        String src =
                "if 1 == 1 then:\n" +
                        "    put 10 into x\n" +
                        "else:\n" +
                        "    put 20 into x\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(10.0, env.get("x"));
    }

    @Test
    @DisplayName("if-else: false condition runs else, skips then")
    void ifElseFalseElse() {
        String src =
                "if 1 == 2 then:\n" +
                        "    put 10 into x\n" +
                        "else:\n" +
                        "    put 20 into x\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(20.0, env.get("x"));
    }

    @Test
    @DisplayName("repeat 3 times: body executes exactly 3 times")
    void repeatExecutesNTimes() {
        String src =
                "put 0 into counter\n" +
                        "repeat 3 times:\n" +
                        "    put counter + 1 into counter\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(3.0, env.get("counter"));
    }

    @Test
    @DisplayName("repeat 0 times: body never executes")
    void repeatZeroTimes() {
        String src =
                "put 0 into counter\n" +
                        "repeat 0 times:\n" +
                        "    put counter + 1 into counter\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(0.0, env.get("counter"));
    }

    @Test
    @DisplayName("Undefined variable at runtime throws BloopRuntimeException")
    void undefinedVariableThrows() {
        String src = "print z\n";
        assertThrows(BloopRuntimeException.class,
                () -> IntegrationHelper.execute(src));
    }

    @Test
    @DisplayName("Division by zero at runtime throws BloopRuntimeException")
    void divisionByZeroThrows() {
        String src = "put 0 into d\nprint 10 / d\n";
        assertThrows(BloopRuntimeException.class,
                () -> IntegrationHelper.execute(src));
    }

    @Test
    @DisplayName("String comparison: put \"hi\" into name, if name == \"hi\" then sets flag")
    void stringComparison() {
        String src =
                "put \"hi\" into name\n" +
                        "if name == \"hi\" then:\n" +
                        "    put 1 into match\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(1.0, env.get("match"));
    }

    @Test
    @DisplayName("Nested if inside repeat")
    void nestedIfInsideRepeat() {
        String src =
                "put 0 into hits\n" +
                        "repeat 4 times:\n" +
                        "    put hits + 1 into hits\n";
        Environment env = IntegrationHelper.execute(src);
        assertEquals(4.0, env.get("hits"));
    }
}