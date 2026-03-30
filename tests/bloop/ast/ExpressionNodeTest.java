package bloop.ast;

import bloop.runtime.Environment;
import bloop.exceptions.BloopRuntimeException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionNodeTest {

    private Environment env;

    @BeforeEach
    void setUp() {
        env = new Environment();
    }

    // ── NumberNode ──────────────────────────

    @Test
    void number_returnsValue() {
        NumberNode node = new NumberNode(42.0);
        assertEquals(42.0, node.evaluate(env));
    }

    @Test
    void number_decimal() {
        NumberNode node = new NumberNode(3.14);
        assertEquals(3.14, node.evaluate(env));
    }

    @Test
    void number_zero() {
        assertEquals(0.0, new NumberNode(0.0).evaluate(env));
    }

    @Test
    void number_negative() {
        assertEquals(-5.0, new NumberNode(-5.0).evaluate(env));
    }

    // ── StringNode ──────────────────────────

    @Test
    void string_returnsValue() {
        assertEquals("hello", new StringNode("hello").evaluate(env));
    }

    @Test
    void string_empty() {
        assertEquals("", new StringNode("").evaluate(env));
    }

    @Test
    void string_withSpaces() {
        assertEquals("hello world", new StringNode("hello world").evaluate(env));
    }

    // ── VariableNode ────────────────────────

    @Test
    void variable_lookupFromEnvironment() {
        env.set("x", 10.0);
        assertEquals(10.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_undefinedThrows() {
        assertThrows(BloopRuntimeException.class,
                () -> new VariableNode("z").evaluate(env));
    }

    @Test
    void variable_updatedValue() {
        env.set("x", 10.0);
        env.set("x", 50.0);
        assertEquals(50.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_stringValue() {
        env.set("name", "Sitare");
        assertEquals("Sitare", new VariableNode("name").evaluate(env));
    }

    // ── BinaryOpNode — Arithmetic ───────────

    @Test
    void binary_addition() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(3), "+", new NumberNode(4)
        );
        assertEquals(7.0, node.evaluate(env));
    }

    @Test
    void binary_subtraction() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(10), "-", new NumberNode(3)
        );
        assertEquals(7.0, node.evaluate(env));
    }

    @Test
    void binary_multiplication() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(4), "*", new NumberNode(5)
        );
        assertEquals(20.0, node.evaluate(env));
    }

    @Test
    void binary_division() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(10), "/", new NumberNode(2)
        );
        assertEquals(5.0, node.evaluate(env));
    }

    // ── BinaryOpNode — Comparisons ──────────

    @Test
    void binary_greaterThan_true() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(10), ">", new NumberNode(5)
        );
        assertTrue((Boolean) node.evaluate(env));
    }

    @Test
    void binary_greaterThan_false() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(3), ">", new NumberNode(5)
        );
        assertFalse((Boolean) node.evaluate(env));
    }

    @Test
    void binary_lessThan_true() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(2), "<", new NumberNode(5)
        );
        assertTrue((Boolean) node.evaluate(env));
    }

    @Test
    void binary_equality_true() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(5), "==", new NumberNode(5)
        );
        assertTrue((Boolean) node.evaluate(env));
    }

    @Test
    void binary_equality_false() {
        BinaryOpNode node = new BinaryOpNode(
                new NumberNode(5), "==", new NumberNode(6)
        );
        assertFalse((Boolean) node.evaluate(env));
    }

    // ── BinaryOpNode — Nested ───────────────

    @Test
    void nested_multiplyBeforeAdd() {
        BinaryOpNode inner = new BinaryOpNode(
                new NumberNode(4), "*", new NumberNode(2)
        );
        BinaryOpNode outer = new BinaryOpNode(
                new NumberNode(3), "+", inner
        );
        assertEquals(11.0, outer.evaluate(env));
    }

    @Test
    void nested_withVariables() {
        env.set("x", 10.0);
        env.set("y", 3.0);

        BinaryOpNode inner = new BinaryOpNode(
                new VariableNode("y"), "*", new NumberNode(2)
        );
        BinaryOpNode outer = new BinaryOpNode(
                new VariableNode("x"), "+", inner
        );

        assertEquals(16.0, outer.evaluate(env));
    }

    @Test
    void nested_deeplyNested() {
        BinaryOpNode left = new BinaryOpNode(
                new NumberNode(2), "+", new NumberNode(3)
        );
        BinaryOpNode right = new BinaryOpNode(
                new NumberNode(4), "-", new NumberNode(1)
        );
        BinaryOpNode outer = new BinaryOpNode(left, "*", right);

        assertEquals(15.0, outer.evaluate(env));
    }
}