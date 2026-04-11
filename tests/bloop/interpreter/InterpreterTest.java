package bloop.interpreter;

import bloop.exceptions.BloopException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for the full BLOOP interpreter pipeline.
 *
 * These tests run real BLOOP source code through Tokenizer → Parser → Executor
 * and assert the exact output printed to stdout matches the project spec.
 */
@DisplayName("Interpreter (end-to-end)")
class InterpreterTest {

    private Interpreter interpreter;

    @BeforeEach
    void freshInterpreter() {
        interpreter = new Interpreter();
    }

    // ── helper ────────────────────────────────────────────────────────────

    private String run(String source) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            interpreter.run(source);
        } finally {
            System.setOut(originalOut);
        }
        return buffer.toString().trim();
    }

    // ── Spec programs — exact output match ────────────────────────────────

    @Test
    @DisplayName("Program 1 — arithmetic and variables → 16")
    void program1_arithmeticAndVariables() {
        String source = """
                put 10 into x
                put 3 into y
                put x + y * 2 into result
                print result
                """;
        assertEquals("16", run(source));
    }

    @Test
    @DisplayName("Program 2 — string output → Sitare / Hello from BLOOP")
    void program2_stringOutput() {
        String source = """
                put "Sitare" into name
                print name
                print "Hello from BLOOP"
                """;
        assertEquals("Sitare\nHello from BLOOP", run(source));
    }

    @Test
    @DisplayName("Program 3 — conditional true → Pass")
    void program3_conditionalTrue() {
        String source = """
                put 85 into score
                if score > 50 then:
                    print "Pass"
                """;
        assertEquals("Pass", run(source));
    }

    @Test
    @DisplayName("Program 3 — conditional false → no output")
    void program3_conditionalFalse() {
        String source = """
                put 30 into score
                if score > 50 then:
                    print "Pass"
                """;
        assertEquals("", run(source));
    }

    @Test
    @DisplayName("Program 4 — loop → 1 2 3 4")
    void program4_loop() {
        String source = """
                put 1 into i
                repeat 4 times:
                    print i
                    put i + 1 into i
                """;
        assertEquals("1\n2\n3\n4", run(source));
    }

    // ── Bonus: else block ─────────────────────────────────────────────────

    @Test
    @DisplayName("else block executes when condition is false")
    void bonusElseBlock_executesWhenConditionFalse() {
        String source = """
                put 30 into score
                if score > 50 then:
                    print "Pass"
                else:
                    print "Fail"
                """;
        assertEquals("Fail", run(source));
    }

    @Test
    @DisplayName("else block skipped when condition is true")
    void bonusElseBlock_skippedWhenConditionTrue() {
        String source = """
                put 85 into score
                if score > 50 then:
                    print "Pass"
                else:
                    print "Fail"
                """;
        assertEquals("Pass", run(source));
    }

    // ── Arithmetic correctness ────────────────────────────────────────────

    @Test
    @DisplayName("multiplication before addition (operator precedence)")
    void operatorPrecedence_multiplicationBeforeAddition() {
        // 2 + 3 * 4 = 14, not 20
        String source = """
                put 2 + 3 * 4 into result
                print result
                """;
        assertEquals("14", run(source));
    }

    @Test
    @DisplayName("parentheses override default precedence")
    void parenthesesOverridePrecedence() {
        // (2 + 3) * 4 = 20
        String source = """
                put (2 + 3) * 4 into result
                print result
                """;
        assertEquals("20", run(source));
    }

    @Test
    @DisplayName("subtraction evaluates left to right")
    void subtraction_leftToRight() {
        String source = """
                put 10 - 3 - 2 into result
                print result
                """;
        assertEquals("5", run(source));
    }

    @Test
    @DisplayName("division produces decimal result")
    void division_producesDecimalResult() {
        String source = """
                put 7 / 2 into result
                print result
                """;
        assertEquals("3.5", run(source));
    }

    // ── Comparisons ───────────────────────────────────────────────────────

    @Test
    @DisplayName(">= comparison works correctly")
    void greaterOrEqual_comparison() {
        String source = """
                put 50 into x
                if x >= 50 then:
                    print "ok"
                """;
        assertEquals("ok", run(source));
    }

    @Test
    @DisplayName("== comparison on numbers")
    void equalEqual_numbersComparison() {
        String source = """
                put 5 into x
                if x == 5 then:
                    print "match"
                """;
        assertEquals("match", run(source));
    }

    @Test
    @DisplayName("!= comparison on numbers")
    void notEqual_numbersComparison() {
        String source = """
                put 5 into x
                if x != 6 then:
                    print "different"
                """;
        assertEquals("different", run(source));
    }

    // ── Nested blocks (bonus) ─────────────────────────────────────────────

    @Test
    @DisplayName("if block nested inside repeat executes correctly")
    void nestedBlock_ifInsideRepeat() {
        String source = """
                put 1 into i
                repeat 3 times:
                    if i == 2 then:
                        print "two"
                    put i + 1 into i
                """;
        assertEquals("two", run(source));
    }

    @Test
    @DisplayName("repeat nested inside repeat executes correctly")
    void nestedBlock_repeatInsideRepeat() {
        // outer runs 2 times, inner runs 2 times each → 4 prints total
        String source = """
                put 0 into count
                repeat 2 times:
                    repeat 2 times:
                        put count + 1 into count
                print count
                """;
        assertEquals("4", run(source));
    }

    // ── Variable mutation ─────────────────────────────────────────────────

    @Test
    @DisplayName("variable can be reassigned to a new value")
    void variableReassignment() {
        String source = """
                put 10 into x
                put 20 into x
                print x
                """;
        assertEquals("20", run(source));
    }

    // ── Error handling ────────────────────────────────────────────────────

    @Test
    @DisplayName("using undefined variable throws BloopException")
    void undefinedVariable_throwsBloopException() {
        assertThrows(BloopException.class, () -> run("print undeclared"));
    }

    @Test
    @DisplayName("division by zero throws BloopException")
    void divisionByZero_throwsBloopException() {
        assertThrows(BloopException.class, () -> run("put 1 / 0 into x"));
    }

    @Test
    @DisplayName("comments are ignored — code still executes")
    void lineComments_areIgnored() {
        String source = """
                # This is a comment
                put 42 into x  # inline comment
                print x
                """;
        assertEquals("42", run(source));
    }
}
