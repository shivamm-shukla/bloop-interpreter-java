package bloop.interpreter;


//  Full pipeline: source string → Interpreter.run()
//  Tests real programs as a user would write them.
//  stdout is captured for verification.


import bloop.runtime.Environment;
import bloop.instruction.Instruction;
import bloop.parser.Parser;
import bloop.token.Tokenizer;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ────────────────────────────────────────────────────────────
//  E2E Helper
// ────────────────────────────────────────────────────────────
class E2EHelper {

    // Runs source code and returns trimmed stdout
    static String run(String source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            new Interpreter().run(source);
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        return out.toString().trim();
    }

    // Returns stderr output (for error cases where Interpreter prints to err)
    static String runGetStderr(String source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            new Interpreter().run(source);
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        return err.toString().trim();
    }

    // Directly executes source through Tokenizer+Parser+Environment (bypasses catch in Interpreter)
    static Environment executeRaw(String source) {
        List<?> tokens = new Tokenizer(source).tokenize();
        @SuppressWarnings("unchecked")
        List<bloop.token.Token> toks = (List<bloop.token.Token>) tokens;
        List<Instruction> instructions = Parser.createDefault().parse(toks);
        Environment env = new Environment();
        instructions.forEach(i -> i.execute(env));
        return env;
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: Basic arithmetic programs
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › Arithmetic programs")
class E2EArithmeticTest {

    @Test
    @DisplayName("Print literal number")
    void printLiteral() {
        assertEquals("42", E2EHelper.run("print 42\n"));
    }

    @Test
    @DisplayName("Print 0")
    void printZero() {
        assertEquals("0", E2EHelper.run("print 0\n"));
    }

    @Test
    @DisplayName("Print decimal number")
    void printDecimal() {
        assertEquals("3.14", E2EHelper.run("print 3.14\n"));
    }

    @Test
    @DisplayName("Print result of addition")
    void printAddition() {
        assertEquals("7", E2EHelper.run("print 3 + 4\n"));
    }

    @Test
    @DisplayName("Print result of subtraction")
    void printSubtraction() {
        assertEquals("6", E2EHelper.run("print 10 - 4\n"));
    }

    @Test
    @DisplayName("Print result of multiplication")
    void printMultiplication() {
        assertEquals("12", E2EHelper.run("print 3 * 4\n"));
    }

    @Test
    @DisplayName("Print result of division (exact)")
    void printDivision() {
        assertEquals("5", E2EHelper.run("print 10 / 2\n"));
    }

    @Test
    @DisplayName("Print result of division (non-integer)")
    void printDivisionFraction() {
        assertEquals("3.5", E2EHelper.run("print 7 / 2\n"));
    }

    @Test
    @DisplayName("Operator precedence: 2 + 3 * 4 = 14")
    void operatorPrecedence() {
        assertEquals("14", E2EHelper.run("print 2 + 3 * 4\n"));
    }

    @Test
    @DisplayName("Parentheses override precedence: (2 + 3) * 4 = 20")
    void parenthesesOverride() {
        assertEquals("20", E2EHelper.run("print (2 + 3) * 4\n"));
    }

    @Test
    @DisplayName("Nested parentheses: ((2 + 3) * (4 - 1)) / 5 = 3")
    void nestedParentheses() {
        assertEquals("3", E2EHelper.run("print ((2 + 3) * (4 - 1)) / 5\n"));
    }

    @Test
    @DisplayName("Unary minus: print -5 outputs -5")
    void unaryMinus() {
        assertEquals("-5", E2EHelper.run("print -5\n"));
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: Variable assignment programs
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › Variable programs")
class E2EVariableTest {

    @Test
    @DisplayName("Assign and print variable")
    void assignAndPrint() {
        String src = "put 10 into x\nprint x\n";
        assertEquals("10", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Assign expression result and print")
    void assignExpression() {
        String src = "put 3 * 4 into result\nprint result\n";
        assertEquals("12", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Overwrite variable and print new value")
    void overwriteVariable() {
        String src = "put 1 into x\nput 99 into x\nprint x\n";
        assertEquals("99", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Assign sum of two variables")
    void sumOfVariables() {
        String src = "put 5 into a\nput 7 into b\nput a + b into c\nprint c\n";
        assertEquals("12", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Assign string to variable and print")
    void stringVariable() {
        String src = "put \"hello\" into msg\nprint msg\n";
        assertEquals("hello", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Undefined variable produces error output (Interpreter catches it)")
    void undefinedVariableOutputsError() {
        String err = E2EHelper.runGetStderr("print z\n");
        assertFalse(err.isBlank(), "Expected error output for undefined variable");
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: If / if-else programs
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › If programs")
class E2EIfTest {

    @Test
    @DisplayName("If true: prints 'yes'")
    void ifTrue() {
        String src = "if 5 > 3 then:\n    print \"yes\"\n";
        assertEquals("yes", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If false: nothing printed")
    void ifFalse() {
        String src = "if 3 > 5 then:\n    print \"yes\"\n";
        assertEquals("", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If-else true branch runs")
    void ifElseTrue() {
        String src =
                "if 1 == 1 then:\n" +
                        "    print \"true\"\n" +
                        "else:\n" +
                        "    print \"false\"\n";
        assertEquals("true", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If-else false branch runs")
    void ifElseFalse() {
        String src =
                "if 1 == 2 then:\n" +
                        "    print \"true\"\n" +
                        "else:\n" +
                        "    print \"false\"\n";
        assertEquals("false", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If with >= comparison: 5 >= 5 is true")
    void greaterOrEqualTrue() {
        String src = "if 5 >= 5 then:\n    print \"ok\"\n";
        assertEquals("ok", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If with != comparison: 1 != 2 is true")
    void notEqualTrue() {
        String src = "if 1 != 2 then:\n    print \"different\"\n";
        assertEquals("different", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If condition uses variable")
    void conditionUsesVariable() {
        String src =
                "put 10 into x\n" +
                        "if x > 5 then:\n" +
                        "    print \"big\"\n";
        assertEquals("big", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Multiple instructions in then-body all execute")
    void multipleInThenBody() {
        String src =
                "if 1 == 1 then:\n" +
                        "    print \"a\"\n" +
                        "    print \"b\"\n";
        String out = E2EHelper.run(src);
        assertTrue(out.contains("a"));
        assertTrue(out.contains("b"));
    }

    @Test
    @DisplayName("Nested if inside another if")
    void nestedIf() {
        String src =
                "if 1 == 1 then:\n" +
                        "    if 2 == 2 then:\n" +
                        "        print \"nested\"\n";
        assertEquals("nested", E2EHelper.run(src));
    }

    @Test
    @DisplayName("If with non-boolean condition: error output produced")
    void nonBooleanConditionError() {
        String src = "if 1 + 1 then:\n    print \"x\"\n";
        String err = E2EHelper.runGetStderr(src);
        assertFalse(err.isBlank(), "Expected error for non-boolean condition");
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: Repeat programs
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › Repeat programs")
class E2ERepeatTest {

    @Test
    @DisplayName("repeat 3 times: body runs exactly 3 times")
    void repeatThreeTimes() {
        String src =
                "put 0 into n\n" +
                        "repeat 3 times:\n" +
                        "    put n + 1 into n\n" +
                        "print n\n";
        assertEquals("3", E2EHelper.run(src));
    }

    @Test
    @DisplayName("repeat 0 times: body never runs")
    void repeatZeroTimes() {
        String src =
                "put 0 into n\n" +
                        "repeat 0 times:\n" +
                        "    put n + 1 into n\n" +
                        "print n\n";
        assertEquals("0", E2EHelper.run(src));
    }

    @Test
    @DisplayName("repeat 1 times: body runs exactly once")
    void repeatOnce() {
        String src =
                "put 0 into n\n" +
                        "repeat 1 times:\n" +
                        "    put n + 1 into n\n" +
                        "print n\n";
        assertEquals("1", E2EHelper.run(src));
    }

    @Test
    @DisplayName("repeat prints multiple values")
    void repeatPrints() {
        String src = "repeat 3 times:\n    print \"hi\"\n";
        String out = E2EHelper.run(src);
        assertEquals("hi\nhi\nhi", out.replace("\r\n", "\n").replace("\r", "\n"));
    }

    @Test
    @DisplayName("Repeat with variable count: repeat x times")
    void repeatWithVariable() {
        String src =
                "put 4 into x\n" +
                        "put 0 into n\n" +
                        "repeat x times:\n" +
                        "    put n + 1 into n\n" +
                        "print n\n";
        assertEquals("4", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Fractional repeat count produces error")
    void fractionalCountError() {
        String src = "repeat 2.5 times:\n    print \"x\"\n";
        String err = E2EHelper.runGetStderr(src);
        assertFalse(err.isBlank(), "Expected error for fractional repeat count");
    }

    @Test
    @DisplayName("Negative repeat count produces error")
    void negativeCountError() {
        String src = "repeat -1 times:\n    print \"x\"\n";
        String err = E2EHelper.runGetStderr(src);
        assertFalse(err.isBlank(), "Expected error for negative repeat count");
    }

    @Test
    @DisplayName("Nested repeat: 2 * 3 = 6 total increments")
    void nestedRepeat() {
        String src =
                "put 0 into n\n" +
                        "repeat 2 times:\n" +
                        "    repeat 3 times:\n" +
                        "        put n + 1 into n\n" +
                        "print n\n";
        assertEquals("6", E2EHelper.run(src));
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: Full programs (real Bloop programs)
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › Full programs")
class E2EFullProgramTest {

    @Test
    @DisplayName("Fibonacci-style: compute first few values with repeated addition")
    void fibLike() {
        String src =
                "put 0 into a\n" +
                        "put 1 into b\n" +
                        "put a + b into c\n" +
                        "print c\n" +
                        "put b + c into d\n" +
                        "print d\n";
        String out = E2EHelper.run(src);
        String[] lines = out.split("\n");
        assertEquals("1", lines[0].trim());
        assertEquals("2", lines[1].trim());
    }

    @Test
    @DisplayName("Accumulator: sum 1..5 using repeat")
    void sumOneToFive() {
        String src =
                "put 0 into sum\n" +
                        "put 1 into i\n" +
                        "repeat 5 times:\n" +
                        "    put sum + i into sum\n" +
                        "    put i + 1 into i\n" +
                        "print sum\n";
        assertEquals("15", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Conditional update inside repeat")
    void conditionalUpdate() {
        // Count how many of 3 checked values pass the condition
        // We simulate: if x > 5 then increment count
        String src =
                "put 0 into count\n" +
                        "put 10 into x\n" +
                        "if x > 5 then:\n" +
                        "    put count + 1 into count\n" +
                        "put 3 into x\n" +
                        "if x > 5 then:\n" +
                        "    put count + 1 into count\n" +
                        "print count\n";
        assertEquals("1", E2EHelper.run(src));
    }

    @Test
    @DisplayName("String comparison program")
    void stringComparisonProgram() {
        String src =
                "put \"bloop\" into lang\n" +
                        "if lang == \"bloop\" then:\n" +
                        "    print \"correct\"\n" +
                        "else:\n" +
                        "    print \"wrong\"\n";
        assertEquals("correct", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Multiple print statements output on separate lines")
    void multiPrint() {
        String src = "print 1\nprint 2\nprint 3\n";
        String out = E2EHelper.run(src).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("1\n2\n3", out);
    }

    @Test
    @DisplayName("Division-by-zero error output contains error info")
    void divisionByZeroProgram() {
        String src = "print 10 / 0\n";
        String err = E2EHelper.runGetStderr(src);
        assertFalse(err.isBlank(), "Expected error output for division by zero");
    }

    @Test
    @DisplayName("Lexer error (unknown character) outputs error, does not crash JVM")
    void lexerErrorHandled() {
        // '@' is not a valid character in Bloop
        String err = E2EHelper.runGetStderr("print @ 42\n");
        assertFalse(err.isBlank(), "Expected error output for unknown character");
    }

    @Test
    @DisplayName("Parse error (missing 'into') outputs error, does not crash JVM")
    void parseErrorHandled() {
        String err = E2EHelper.runGetStderr("put 42 x\n");
        assertFalse(err.isBlank(), "Expected error output for missing 'into'");
    }

    @Test
    @DisplayName("Complex expression: (a + b) * (c - d)")
    void complexExpression() {
        String src =
                "put 3 into a\n" +
                        "put 2 into b\n" +
                        "put 6 into c\n" +
                        "put 1 into d\n" +
                        "print (a + b) * (c - d)\n";
        assertEquals("25", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Empty program produces no output")
    void emptyProgram() {
        assertEquals("", E2EHelper.run(""));
    }

    @Test
    @DisplayName("Program with only blank lines produces no output")
    void blankLinesProgram() {
        assertEquals("", E2EHelper.run("\n\n\n"));
    }
}

// ────────────────────────────────────────────────────────────
//  E2E: Edge cases & boundary conditions
// ────────────────────────────────────────────────────────────
@DisplayName("E2E › Edge cases")
class E2EEdgeCasesTest {

    @Test
    @DisplayName("Very large integer: print 1000000")
    void largeInteger() {
        assertEquals("1000000", E2EHelper.run("print 1000000\n"));
    }

    @Test
    @DisplayName("Negative result from subtraction: 3 - 10 = -7")
    void negativeResult() {
        assertEquals("-7", E2EHelper.run("print 3 - 10\n"));
    }

    @Test
    @DisplayName("String with escape characters in print")
    void stringWithEscape() {
        // "a\\nb" in source → prints a, newline, b
        String src = "print \"a\\nb\"\n";
        String out = E2EHelper.run(src).replace("\r\n", "\n").replace("\r", "\n");
        assertEquals("a\nb", out);
    }

    @Test
    @DisplayName("Variable used before assignment errors")
    void variableBeforeAssignment() {
        String err = E2EHelper.runGetStderr("print undeclared\n");
        assertFalse(err.isBlank());
    }

    @Test
    @DisplayName("Re-assigning variable inside if block is visible after")
    void reassignInsideIf() {
        String src =
                "put 0 into x\n" +
                        "if 1 == 1 then:\n" +
                        "    put 99 into x\n" +
                        "print x\n";
        assertEquals("99", E2EHelper.run(src));
    }

    @Test
    @DisplayName("String == number comparison is always false (cross-type)")
    void stringVsNumberFalse() {
        String src =
                "if \"42\" == 42 then:\n" +
                        "    print \"equal\"\n" +
                        "else:\n" +
                        "    print \"not equal\"\n";
        assertEquals("not equal", E2EHelper.run(src));
    }

    @Test
    @DisplayName("Repeat with variable count set to 0 → no output")
    void repeatVariableCountZero() {
        String src =
                "put 0 into n\n" +
                        "repeat n times:\n" +
                        "    print \"x\"\n";
        assertEquals("", E2EHelper.run(src));
    }

    @Test
    @DisplayName("if condition using string equality")
    void stringEqualityCondition() {
        String src =
                "put \"yes\" into answer\n" +
                        "if answer == \"yes\" then:\n" +
                        "    print \"correct\"\n";
        assertEquals("correct", E2EHelper.run(src));
    }
}