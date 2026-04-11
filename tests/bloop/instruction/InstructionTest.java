package bloop.instruction;

import bloop.ast.*;
import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all Instruction implementations.
 *
 * PrintInstruction output is captured via System.out redirection.
 * Each test gets a fresh Environment to avoid state leakage.
 */
@DisplayName("Instructions")
class InstructionTest {

    private Environment env;

    @BeforeEach
    void freshEnvironment() {
        env = new Environment();
    }

    // ── helper — capture stdout ───────────────────────────────────────────

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return buffer.toString().trim();
    }

    // ── AssignInstruction ─────────────────────────────────────────────────

    @Test
    @DisplayName("AssignInstruction stores evaluated expression in the environment")
    void assignInstruction_storesValueInEnvironment() {
        AssignInstruction assign = new AssignInstruction("x", new NumberNode(10.0));
        assign.execute(env);
        assertEquals(10.0, (Double) env.get("x"));
    }

    @Test
    @DisplayName("AssignInstruction evaluates expression before storing")
    void assignInstruction_evaluatesExpressionFirst() {
        env.set("y", 3.0);
        // put y * 2 into result
        Expression expr = new BinaryOpNode(new VariableNode("y"), "*", new NumberNode(2.0));
        new AssignInstruction("result", expr).execute(env);
        assertEquals(6.0, (Double) env.get("result"));
    }

    @Test
    @DisplayName("AssignInstruction overwrites an existing variable")
    void assignInstruction_overwritesExistingVariable() {
        env.set("count", 1.0);
        new AssignInstruction("count", new NumberNode(99.0)).execute(env);
        assertEquals(99.0, (Double) env.get("count"));
    }

    @Test
    @DisplayName("AssignInstruction stores string values")
    void assignInstruction_storesStringValue() {
        new AssignInstruction("greeting", new StringNode("hello")).execute(env);
        assertEquals("hello", env.get("greeting"));
    }

    // ── PrintInstruction ──────────────────────────────────────────────────

    @Test
    @DisplayName("PrintInstruction prints a whole-number double without decimal point")
    void printInstruction_formatsWholeNumberWithoutDecimal() {
        String output = captureOutput(
                () -> new PrintInstruction(new NumberNode(16.0)).execute(env));
        assertEquals("16", output);
    }

    @Test
    @DisplayName("PrintInstruction prints a decimal number as-is")
    void printInstruction_formatsDecimalNumber() {
        String output = captureOutput(
                () -> new PrintInstruction(new NumberNode(3.14)).execute(env));
        assertEquals("3.14", output);
    }

    @Test
    @DisplayName("PrintInstruction prints a string value")
    void printInstruction_printsString() {
        String output = captureOutput(
                () -> new PrintInstruction(new StringNode("Sitare")).execute(env));
        assertEquals("Sitare", output);
    }

    @Test
    @DisplayName("PrintInstruction prints a variable's current value")
    void printInstruction_printsVariableValue() {
        env.set("score", 85.0);
        String output = captureOutput(
                () -> new PrintInstruction(new VariableNode("score")).execute(env));
        assertEquals("85", output);
    }

    // ── IfInstruction ─────────────────────────────────────────────────────

    @Test
    @DisplayName("IfInstruction executes then-body when condition is true")
    void ifInstruction_executesThenBodyWhenTrue() {
        Expression trueCondition = new BinaryOpNode(new NumberNode(85), ">", new NumberNode(50));
        IfInstruction ifInstr = new IfInstruction(
                trueCondition,
                List.of(new AssignInstruction("result", new NumberNode(1.0))));

        ifInstr.execute(env);
        assertTrue(env.isDefined("result"));
        assertEquals(1.0, (Double) env.get("result"));
    }

    @Test
    @DisplayName("IfInstruction skips then-body when condition is false")
    void ifInstruction_skipsThenBodyWhenFalse() {
        Expression falseCondition = new BinaryOpNode(new NumberNode(10), ">", new NumberNode(50));
        IfInstruction ifInstr = new IfInstruction(
                falseCondition,
                List.of(new AssignInstruction("result", new NumberNode(1.0))));

        ifInstr.execute(env);
        assertFalse(env.isDefined("result"));
    }

    @Test
    @DisplayName("IfInstruction executes else-body when condition is false")
    void ifInstruction_executesElseBodyWhenFalse() {
        Expression falseCondition = new BinaryOpNode(new NumberNode(10), ">", new NumberNode(50));
        IfInstruction ifInstr = new IfInstruction(
                falseCondition,
                List.of(new AssignInstruction("thenRan", new NumberNode(1.0))),
                List.of(new AssignInstruction("elseRan", new NumberNode(1.0))));

        ifInstr.execute(env);
        assertFalse(env.isDefined("thenRan"),  "then-body must not run");
        assertTrue(env.isDefined("elseRan"),   "else-body must run");
    }

    @Test
    @DisplayName("IfInstruction throws when condition is not boolean")
    void ifInstruction_throwsWhenConditionIsNotBoolean() {
        IfInstruction ifInstr = new IfInstruction(
                new NumberNode(42.0),
                List.of(new PrintInstruction(new StringNode("oops"))));
        assertThrows(BloopRuntimeException.class, () -> ifInstr.execute(env));
    }

    // ── RepeatInstruction ─────────────────────────────────────────────────

    @Test
    @DisplayName("RepeatInstruction executes body exactly N times")
    void repeatInstruction_executesBodyNTimes() {
        // put 0 into counter; repeat 4 times: put counter + 1 into counter
        env.set("counter", 0.0);
        Expression increment = new BinaryOpNode(new VariableNode("counter"), "+", new NumberNode(1.0));
        RepeatInstruction repeat = new RepeatInstruction(
                4, List.of(new AssignInstruction("counter", increment)));

        repeat.execute(env);
        assertEquals(4.0, (Double) env.get("counter"));
    }

    @Test
    @DisplayName("RepeatInstruction with count 0 never executes body")
    void repeatInstruction_zeroCountNeverExecutesBody() {
        RepeatInstruction repeat = new RepeatInstruction(
                0, List.of(new AssignInstruction("touched", new NumberNode(1.0))));
        repeat.execute(env);
        assertFalse(env.isDefined("touched"));
    }

    @Test
    @DisplayName("RepeatInstruction prints each iteration — matches spec program 4")
    void repeatInstruction_matchesProgram4Output() {
        // put 1 into i; repeat 4 times: print i; put i+1 into i
        env.set("i", 1.0);
        Expression increment = new BinaryOpNode(new VariableNode("i"), "+", new NumberNode(1.0));

        List<Instruction> body = List.of(
                new PrintInstruction(new VariableNode("i")),
                new AssignInstruction("i", increment));

        String output = captureOutput(
                () -> new RepeatInstruction(4, body).execute(env));

        assertEquals("1\n2\n3\n4", output);
    }
}
