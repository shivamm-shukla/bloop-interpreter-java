package bloop.instruction;

import bloop.ast.*;
import bloop.instructions.AssignInstruction;
import bloop.instructions.IfInstruction;
import bloop.instructions.PrintInstruction;
import bloop.instructions.RepeatInstruction;
import bloop.runtime.Environment;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.List;

class InstructionTest {

    private Environment env;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setup() {
        env = new Environment();
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void restore() {
        System.setOut(System.out);
    }

    private String out() {
        return output.toString().replace("\r","").trim();
    }

    @Test
    void assignInstruction_numberExpression() {
        new AssignInstruction("x", new NumberNode(10)).execute(env);
        assertEquals(10.0, env.get("x"));
    }

    @Test
    void assignInstruction_binaryExpression() {
        new AssignInstruction("x",
                new BinaryOpNode(new NumberNode(3), "+", new NumberNode(4)))
                .execute(env);
        assertEquals(7.0, env.get("x"));
    }

    @Test
    void printInstruction_number() {
        new PrintInstruction(new NumberNode(42)).execute(env);
        assertEquals("42", out());
    }

    @Test
    void printInstruction_string() {
        new PrintInstruction(new StringNode("hello")).execute(env);
        assertEquals("hello", out());
    }

    @Test
    void ifInstruction_true_executes() {
        new IfInstruction(
                new BinaryOpNode(new NumberNode(5), ">", new NumberNode(2)),
                List.of(new PrintInstruction(new StringNode("yes")))
        ).execute(env);

        assertEquals("yes", out());
    }

    @Test
    void ifInstruction_false_skips() {
        new IfInstruction(
                new BinaryOpNode(new NumberNode(1), ">", new NumberNode(5)),
                List.of(new PrintInstruction(new StringNode("no")))
        ).execute(env);

        assertEquals("", out());
    }

    @Test
    void repeatInstruction_runsMultipleTimes() {
        new RepeatInstruction(3,
                List.of(new PrintInstruction(new StringNode("hi"))))
                .execute(env);

        assertEquals("hi\nhi\nhi", out());
    }

    @Test
    void repeatInstruction_zeroTimes() {
        new RepeatInstruction(0,
                List.of(new PrintInstruction(new StringNode("hi"))))
                .execute(env);

        assertEquals("", out());
    }

    @Test
    void repeatInstruction_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new RepeatInstruction(-1, List.of()));
    }
}