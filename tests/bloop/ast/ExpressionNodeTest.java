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


    // ════════════════════════════════════════════════════════════
    //  NumberNode
    // ════════════════════════════════════════════════════════════

    @Test
    void number_positiveInteger() {
        assertEquals(42.0, new NumberNode(42.0).evaluate(env));
    }

    @Test
    void number_zero() {
        assertEquals(0.0, new NumberNode(0.0).evaluate(env));
    }

    @Test
    void number_negativeInteger() {
        assertEquals(-5.0, new NumberNode(-5.0).evaluate(env));
    }

    @Test
    void number_positiveDecimal() {
        assertEquals(3.14, new NumberNode(3.14).evaluate(env));
    }

    @Test
    void number_negativeDecimal() {
        assertEquals(-0.001, new NumberNode(-0.001).evaluate(env));
    }

    @Test
    void number_one() {
        assertEquals(1.0, new NumberNode(1.0).evaluate(env));
    }

    @Test
    void number_minusOne() {
        assertEquals(-1.0, new NumberNode(-1.0).evaluate(env));
    }

    @Test
    void number_largeValue() {
        assertEquals(1_000_000_000.0, new NumberNode(1_000_000_000.0).evaluate(env));
    }

    @Test
    void number_verySmallDecimal() {
        assertEquals(1.0E-10, new NumberNode(1.0E-10).evaluate(env));
    }

    @Test
    void number_negativeZero_equalToZero() {
        assertEquals(0.0, new NumberNode(-0.0).evaluate(env));
    }

    @Test
    void number_maxDouble_throws() {
        // Agar NumberNode MAX_VALUE ko bhi reject karta hai toh throws,
        // warna assertEquals use karo — apni implementation ke hisaab se adjust karo
        assertDoesNotThrow(() -> new NumberNode(Double.MAX_VALUE));
    }

    @Test
    void number_minDouble_doesNotThrow() {
        assertDoesNotThrow(() -> new NumberNode(Double.MIN_VALUE));
    }

    @Test
    void number_positiveInfinity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new NumberNode(Double.POSITIVE_INFINITY));
    }

    @Test
    void number_negativeInfinity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new NumberNode(Double.NEGATIVE_INFINITY));
    }

    @Test
    void number_NaN_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new NumberNode(Double.NaN));
    }

    @Test
    void number_returnsDouble_notInteger() {
        assertInstanceOf(Double.class, new NumberNode(5.0).evaluate(env));
    }

    @Test
    void number_onePointFive() {
        assertEquals(1.5, new NumberNode(1.5).evaluate(env));
    }

    @Test
    void number_pi() {
        assertEquals(Math.PI, new NumberNode(Math.PI).evaluate(env));
    }

    @Test
    void number_eulerNumber() {
        assertEquals(Math.E, new NumberNode(Math.E).evaluate(env));
    }

    @Test
    void number_sameNodeEvaluatedTwice_sameResult() {
        NumberNode node = new NumberNode(7.0);
        assertEquals(node.evaluate(env), node.evaluate(env));
    }

    @Test
    void number_differentEnvs_sameResult() {
        Environment env2 = new Environment();
        env2.set("x", 999.0);
        NumberNode node = new NumberNode(42.0);
        assertEquals(node.evaluate(env), node.evaluate(env2));
    }


    // ════════════════════════════════════════════════════════════
    //  StringNode
    // ════════════════════════════════════════════════════════════

    @Test
    void string_simple() {
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

    @Test
    void string_onlySpaces() {
        assertEquals("   ", new StringNode("   ").evaluate(env));
    }

    @Test
    void string_withSpecialCharacters() {
        assertEquals("!@#$%^&*()", new StringNode("!@#$%^&*()").evaluate(env));
    }

    @Test
    void string_withNumbers() {
        assertEquals("abc123", new StringNode("abc123").evaluate(env));
    }

    @Test
    void string_newlineCharacter() {
        assertEquals("line1\nline2", new StringNode("line1\nline2").evaluate(env));
    }

    @Test
    void string_tabCharacter() {
        assertEquals("col1\tcol2", new StringNode("col1\tcol2").evaluate(env));
    }

    @Test
    void string_unicodeHindi() {
        assertEquals("नमस्ते", new StringNode("नमस्ते").evaluate(env));
    }

    @Test
    void string_unicodeEmoji() {
        assertEquals("😊🎉", new StringNode("😊🎉").evaluate(env));
    }

    @Test
    void string_longString() {
        String longStr = "a".repeat(10_000);
        assertEquals(longStr, new StringNode(longStr).evaluate(env));
    }

    @Test
    void string_returnsString_notOtherType() {
        assertInstanceOf(String.class, new StringNode("test").evaluate(env));
    }

    @Test
    void string_numericString_notTreatedAsNumber() {
        Object result = new StringNode("42").evaluate(env);
        assertInstanceOf(String.class, result);
        assertEquals("42", result);
    }

    @Test
    void string_singleCharacter() {
        assertEquals("a", new StringNode("a").evaluate(env));
    }

    @Test
    void string_escapedQuote() {
        assertEquals("say \"hi\"", new StringNode("say \"hi\"").evaluate(env));
    }

    @Test
    void string_backslash() {
        assertEquals("C:\\Users\\bloop", new StringNode("C:\\Users\\bloop").evaluate(env));
    }

    @Test
    void string_booleanLookingString_notBoolean() {
        Object result = new StringNode("true").evaluate(env);
        assertInstanceOf(String.class, result);
        assertEquals("true", result);
    }

    @Test
    void string_sameNodeEvaluatedTwice_sameResult() {
        StringNode node = new StringNode("bloop");
        assertEquals(node.evaluate(env), node.evaluate(env));
    }

    @Test
    void string_differentEnvs_sameResult() {
        Environment env2 = new Environment();
        StringNode node = new StringNode("hello");
        assertEquals(node.evaluate(env), node.evaluate(env2));
    }

    @Test
    void string_nullLookingString() {
        assertEquals("null", new StringNode("null").evaluate(env));
    }

    @Test
    void string_mixedCasePreserved() {
        assertEquals("HeLLo WoRLd", new StringNode("HeLLo WoRLd").evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  VariableNode
    // ════════════════════════════════════════════════════════════

    @Test
    void variable_lookupDouble() {
        env.set("x", 10.0);
        assertEquals(10.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_lookupString() {
        env.set("name", "Bloop");
        assertEquals("Bloop", new VariableNode("name").evaluate(env));
    }

    @Test
    void variable_undefinedThrows() {
        assertThrows(BloopRuntimeException.class,
                () -> new VariableNode("z").evaluate(env));
    }

    @Test
    void variable_updatedValue() {
        env.set("x", 10.0);
        env.set("x", 99.0);
        assertEquals(99.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_zeroValue() {
        env.set("x", 0.0);
        assertEquals(0.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_negativeValue() {
        env.set("x", -7.0);
        assertEquals(-7.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_emptyString() {
        env.set("s", "");
        assertEquals("", new VariableNode("s").evaluate(env));
    }

    @Test
    void variable_multipleVariables_independent() {
        env.set("a", 1.0);
        env.set("b", 2.0);
        assertEquals(1.0, new VariableNode("a").evaluate(env));
        assertEquals(2.0, new VariableNode("b").evaluate(env));
    }

    @Test
    void variable_caseSensitive_lowercase() {
        env.set("x", 1.0);
        assertEquals(1.0, new VariableNode("x").evaluate(env));
    }

    @Test
    void variable_caseSensitive_uppercase() {
        env.set("X", 2.0);
        assertEquals(2.0, new VariableNode("X").evaluate(env));
    }

    @Test
    void variable_caseSensitive_bothExistIndependently() {
        env.set("x", 1.0);
        env.set("X", 2.0);
        assertNotEquals(
                new VariableNode("x").evaluate(env),
                new VariableNode("X").evaluate(env)
        );
    }

    @Test
    void variable_overwriteStringWithNumber() {
        env.set("v", "hello");
        env.set("v", 42.0);
        assertEquals(42.0, new VariableNode("v").evaluate(env));
    }

    @Test
    void variable_overwriteNumberWithString() {
        env.set("v", 42.0);
        env.set("v", "hello");
        assertEquals("hello", new VariableNode("v").evaluate(env));
    }

    @Test
    void variable_multipleUndefinedNames_allThrow() {
        assertThrows(BloopRuntimeException.class, () -> new VariableNode("a").evaluate(env));
        assertThrows(BloopRuntimeException.class, () -> new VariableNode("b").evaluate(env));
        assertThrows(BloopRuntimeException.class, () -> new VariableNode("xyz").evaluate(env));
    }

    @Test
    void variable_largeNumberValue() {
        env.set("big", Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, new VariableNode("big").evaluate(env));
    }

    @Test
    void variable_setAndReadMultipleTimes() {
        env.set("counter", 1.0);
        assertEquals(1.0, new VariableNode("counter").evaluate(env));
        env.set("counter", 2.0);
        assertEquals(2.0, new VariableNode("counter").evaluate(env));
        env.set("counter", 3.0);
        assertEquals(3.0, new VariableNode("counter").evaluate(env));
    }

    @Test
    void variable_underscoreName() {
        env.set("my_var", 55.0);
        assertEquals(55.0, new VariableNode("my_var").evaluate(env));
    }

    @Test
    void variable_negativeZeroStored() {
        env.set("nz", -0.0);
        assertEquals(0.0, new VariableNode("nz").evaluate(env));
    }

    @Test
    void variable_sameNodeDifferentEnvs_differentResults() {
        Environment env2 = new Environment();
        env.set("x", 10.0);
        env2.set("x", 99.0);
        VariableNode node = new VariableNode("x");
        assertEquals(10.0, node.evaluate(env));
        assertEquals(99.0, node.evaluate(env2));
    }

    @Test
    void variable_definedInOneEnv_undefinedInAnother_throws() {
        env.set("x", 5.0);
        Environment env2 = new Environment();
        assertThrows(BloopRuntimeException.class,
                () -> new VariableNode("x").evaluate(env2));
    }

    @Test
    void variable_numericStringValue_notNumber() {
        env.set("s", "100");
        Object result = new VariableNode("s").evaluate(env);
        assertInstanceOf(String.class, result);
        assertEquals("100", result);
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Addition
    // ════════════════════════════════════════════════════════════

    @Test
    void add_twoPositives() {
        assertEquals(7.0, new BinaryOpNode(
                new NumberNode(3), "+", new NumberNode(4)).evaluate(env));
    }

    @Test
    void add_withZeroLeft() {
        assertEquals(5.0, new BinaryOpNode(
                new NumberNode(0), "+", new NumberNode(5)).evaluate(env));
    }

    @Test
    void add_withZeroRight() {
        assertEquals(5.0, new BinaryOpNode(
                new NumberNode(5), "+", new NumberNode(0)).evaluate(env));
    }

    @Test
    void add_twoNegatives() {
        assertEquals(-8.0, new BinaryOpNode(
                new NumberNode(-3), "+", new NumberNode(-5)).evaluate(env));
    }

    @Test
    void add_positiveAndNegative_positiveResult() {
        assertEquals(2.0, new BinaryOpNode(
                new NumberNode(5), "+", new NumberNode(-3)).evaluate(env));
    }

    @Test
    void add_positiveAndNegative_negativeResult() {
        assertEquals(-2.0, new BinaryOpNode(
                new NumberNode(3), "+", new NumberNode(-5)).evaluate(env));
    }

    @Test
    void add_largeNumbers() {
        assertEquals(2_000_000_000.0, new BinaryOpNode(
                new NumberNode(1_000_000_000.0), "+", new NumberNode(1_000_000_000.0)
        ).evaluate(env));
    }

    @Test
    void add_opposites_givesZero() {
        assertEquals(0.0, new BinaryOpNode(
                new NumberNode(5), "+", new NumberNode(-5)).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Subtraction
    // ════════════════════════════════════════════════════════════

    @Test
    void sub_simple() {
        assertEquals(7.0, new BinaryOpNode(
                new NumberNode(10), "-", new NumberNode(3)).evaluate(env));
    }

    @Test
    void sub_resultNegative() {
        assertEquals(-2.0, new BinaryOpNode(
                new NumberNode(3), "-", new NumberNode(5)).evaluate(env));
    }

    @Test
    void sub_zero() {
        assertEquals(5.0, new BinaryOpNode(
                new NumberNode(5), "-", new NumberNode(0)).evaluate(env));
    }

    @Test
    void sub_sameNumbers_givesZero() {
        assertEquals(0.0, new BinaryOpNode(
                new NumberNode(7), "-", new NumberNode(7)).evaluate(env));
    }

    @Test
    void sub_twoNegatives() {
        assertEquals(2.0, new BinaryOpNode(
                new NumberNode(-3), "-", new NumberNode(-5)).evaluate(env));
    }

    @Test
    void sub_fromZero() {
        assertEquals(-5.0, new BinaryOpNode(
                new NumberNode(0), "-", new NumberNode(5)).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Multiplication
    // ════════════════════════════════════════════════════════════

    @Test
    void mul_simple() {
        assertEquals(20.0, new BinaryOpNode(
                new NumberNode(4), "*", new NumberNode(5)).evaluate(env));
    }

    @Test
    void mul_byZeroLeft() {
        assertEquals(0.0, new BinaryOpNode(
                new NumberNode(0), "*", new NumberNode(999)).evaluate(env));
    }

    @Test
    void mul_byZeroRight() {
        assertEquals(0.0, new BinaryOpNode(
                new NumberNode(999), "*", new NumberNode(0)).evaluate(env));
    }

    @Test
    void mul_byOne() {
        assertEquals(7.0, new BinaryOpNode(
                new NumberNode(7), "*", new NumberNode(1)).evaluate(env));
    }

    @Test
    void mul_twoNegatives_positiveResult() {
        assertEquals(15.0, new BinaryOpNode(
                new NumberNode(-3), "*", new NumberNode(-5)).evaluate(env));
    }

    @Test
    void mul_negativeAndPositive_negativeResult() {
        assertEquals(-12.0, new BinaryOpNode(
                new NumberNode(-3), "*", new NumberNode(4)).evaluate(env));
    }

    @Test
    void mul_byMinusOne() {
        assertEquals(-5.0, new BinaryOpNode(
                new NumberNode(5), "*", new NumberNode(-1)).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Division
    // ════════════════════════════════════════════════════════════

    @Test
    void div_simple() {
        assertEquals(5.0, new BinaryOpNode(
                new NumberNode(10), "/", new NumberNode(2)).evaluate(env));
    }

    @Test
    void div_resultDecimal() {
        assertEquals(2.5, new BinaryOpNode(
                new NumberNode(5), "/", new NumberNode(2)).evaluate(env));
    }

    @Test
    void div_byOne() {
        assertEquals(9.0, new BinaryOpNode(
                new NumberNode(9), "/", new NumberNode(1)).evaluate(env));
    }

    @Test
    void div_negativeByPositive() {
        assertEquals(-5.0, new BinaryOpNode(
                new NumberNode(-10), "/", new NumberNode(2)).evaluate(env));
    }

    @Test
    void div_negativeByNegative_positiveResult() {
        assertEquals(5.0, new BinaryOpNode(
                new NumberNode(-10), "/", new NumberNode(-2)).evaluate(env));
    }

    @Test
    void div_zeroByNumber() {
        assertEquals(0.0, new BinaryOpNode(
                new NumberNode(0), "/", new NumberNode(5)).evaluate(env));
    }

    @Test
    void div_byZero_positive_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new NumberNode(5), "/", new NumberNode(0)
                ).evaluate(env));
    }

    @Test
    void div_byZero_negative_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new NumberNode(-5), "/", new NumberNode(0)
                ).evaluate(env));
    }

    @Test
    void div_byZero_zero_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new NumberNode(0), "/", new NumberNode(0)
                ).evaluate(env));
    }

    @Test
    void div_largeByOne() {
        assertEquals(1_000_000.0, new BinaryOpNode(
                new NumberNode(1_000_000.0), "/", new NumberNode(1.0)).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Comparisons
    // ════════════════════════════════════════════════════════════

    @Test
    void gt_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(10), ">", new NumberNode(5)).evaluate(env));
    }

    @Test
    void gt_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(3), ">", new NumberNode(5)).evaluate(env));
    }

    @Test
    void gt_equal_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(5), ">", new NumberNode(5)).evaluate(env));
    }

    @Test
    void gt_negativeVsPositive_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(-1), ">", new NumberNode(1)).evaluate(env));
    }

    @Test
    void gt_twoNegatives_correctOrder() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(-1), ">", new NumberNode(-5)).evaluate(env));
    }

    @Test
    void lt_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(2), "<", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lt_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(7), "<", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lt_equal_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(5), "<", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lt_negativeVsPositive_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(-1), "<", new NumberNode(1)).evaluate(env));
    }

    @Test
    void gte_greaterCase_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(6), ">=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void gte_equalCase_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(5), ">=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void gte_lessCase_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(4), ">=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lte_lessCase_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(4), "<=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lte_equalCase_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(5), "<=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void lte_greaterCase_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(6), "<=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void eq_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(5), "==", new NumberNode(5)).evaluate(env));
    }

    @Test
    void eq_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(5), "==", new NumberNode(6)).evaluate(env));
    }

    @Test
    void eq_zeroAndNegativeZero_equal() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(0.0), "==", new NumberNode(-0.0)).evaluate(env));
    }

    @Test
    void eq_twoNegatives_equal() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(-3), "==", new NumberNode(-3)).evaluate(env));
    }

    @Test
    void eq_twoNegatives_notEqual() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(-3), "==", new NumberNode(-4)).evaluate(env));
    }

    @Test
    void eq_decimals_equal() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(0.5), "==", new NumberNode(0.5)).evaluate(env));
    }

    @Test
    void neq_true() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(5), "!=", new NumberNode(6)).evaluate(env));
    }

    @Test
    void neq_false() {
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(5), "!=", new NumberNode(5)).evaluate(env));
    }

    @Test
    void neq_negatives() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(-1), "!=", new NumberNode(-2)).evaluate(env));
    }

    @Test
    void comparison_returnsBoolean_notNumber() {
        assertInstanceOf(Boolean.class, new BinaryOpNode(
                new NumberNode(5), ">", new NumberNode(3)).evaluate(env));
    }

    @Test
    void comparison_invalidOperator_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new NumberNode(5), "??", new NumberNode(3)).evaluate(env));
    }

    @Test
    void comparison_ampersandOperator_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new NumberNode(5), "&", new NumberNode(3)).evaluate(env));
    }

    @Test
    void string_plus_string_concatenates() {
        assertEquals("helloworld",
                new BinaryOpNode(
                        new StringNode("hello"), "+", new StringNode("world")
                ).evaluate(env));
    }

    @Test
    void number_plus_string_concatenates() {
        assertEquals("5hello",
                new BinaryOpNode(
                        new NumberNode(5), "+", new StringNode("hello")
                ).evaluate(env));
    }

    @Test
    void string_plus_number_concatenates() {
        assertEquals("hello5",
                new BinaryOpNode(
                        new StringNode("hello"), "+", new NumberNode(5)
                ).evaluate(env));
    }

    @Test
    void number_plus_number_adds() {
        assertEquals(8.0,
                new BinaryOpNode(
                        new NumberNode(5), "+", new NumberNode(3)
                ).evaluate(env));
    }

    @Test
    void double_string_formatting_removes_decimal_if_integer() {
        assertEquals("5x",
                new BinaryOpNode(
                        new NumberNode(5.0), "+", new StringNode("x")
                ).evaluate(env));
    }

    @Test
    void comparison_stringVsString_throws() {
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new StringNode("a"), ">", new StringNode("b")
                ).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  BinaryOpNode — Nested / Precedence
    // ════════════════════════════════════════════════════════════

    @Test
    void nested_multiplyBeforeAdd() {
        // 3 + (4 * 2) = 11
        BinaryOpNode mul = new BinaryOpNode(new NumberNode(4), "*", new NumberNode(2));
        assertEquals(11.0,
                new BinaryOpNode(new NumberNode(3), "+", mul).evaluate(env));
    }

    @Test
    void nested_addBeforeCompare() {
        // (3 + 4) > 5 → true
        BinaryOpNode add = new BinaryOpNode(new NumberNode(3), "+", new NumberNode(4));
        assertTrue((Boolean)
                new BinaryOpNode(add, ">", new NumberNode(5)).evaluate(env));
    }

    @Test
    void nested_fivePlusThreeTimesThreeMinusOne() {
        // (2 + 3) * (4 - 1) = 15
        BinaryOpNode left = new BinaryOpNode(new NumberNode(2), "+", new NumberNode(3));
        BinaryOpNode right = new BinaryOpNode(new NumberNode(4), "-", new NumberNode(1));
        assertEquals(15.0, new BinaryOpNode(left, "*", right).evaluate(env));
    }

    @Test
    void nested_threeLevelsDeep() {
        // ((2 + 3) * 4) / 2 = 10
        BinaryOpNode add = new BinaryOpNode(new NumberNode(2), "+", new NumberNode(3));
        BinaryOpNode mul = new BinaryOpNode(add, "*", new NumberNode(4));
        assertEquals(10.0, new BinaryOpNode(mul, "/", new NumberNode(2)).evaluate(env));
    }

    @Test
    void nested_fourLevelsDeep() {
        // (((1 + 2) * 3) - 4) / 5 = 1
        BinaryOpNode a = new BinaryOpNode(new NumberNode(1), "+", new NumberNode(2));
        BinaryOpNode b = new BinaryOpNode(a, "*", new NumberNode(3));
        BinaryOpNode c = new BinaryOpNode(b, "-", new NumberNode(4));
        assertEquals(1.0, new BinaryOpNode(c, "/", new NumberNode(5)).evaluate(env));
    }

    @Test
    void nested_withVariables_addAndMultiply() {
        env.set("x", 10.0);
        env.set("y", 3.0);
        // x + (y * 2) = 16
        BinaryOpNode inner = new BinaryOpNode(new VariableNode("y"), "*", new NumberNode(2));
        assertEquals(16.0,
                new BinaryOpNode(new VariableNode("x"), "+", inner).evaluate(env));
    }

    @Test
    void nested_compareResultUsedInArithmetic_throws() {
        // (5 == 5) = true, then true + 1 → throw
        BinaryOpNode inner = new BinaryOpNode(new NumberNode(5), "==", new NumberNode(5));
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(inner, "+", new NumberNode(1)).evaluate(env));
    }

    @Test
    void nested_variableComparedWithExpression() {
        env.set("score", 85.0);
        // score >= (80 + 1) → true
        BinaryOpNode add = new BinaryOpNode(new NumberNode(80), "+", new NumberNode(1));
        assertTrue((Boolean) new BinaryOpNode(
                new VariableNode("score"), ">=", add).evaluate(env));
    }

    @Test
    void nested_threeVariables_sumOfProducts() {
        env.set("a", 5.0);
        env.set("b", 3.0);
        env.set("c", 2.0);
        // (a * b) + (c * c) = 19
        BinaryOpNode left = new BinaryOpNode(new VariableNode("a"), "*", new VariableNode("b"));
        BinaryOpNode right = new BinaryOpNode(new VariableNode("c"), "*", new VariableNode("c"));
        assertEquals(19.0, new BinaryOpNode(left, "+", right).evaluate(env));
    }

    @Test
    void nested_subtractThenCompare() {
        env.set("x", 10.0);
        // (x - 3) == 7 → true
        BinaryOpNode sub = new BinaryOpNode(new VariableNode("x"), "-", new NumberNode(3));
        assertTrue((Boolean) new BinaryOpNode(sub, "==", new NumberNode(7)).evaluate(env));
    }

    @Test
    void nested_divisionResultCompared() {
        // (10 / 2) == 5 → true
        BinaryOpNode div = new BinaryOpNode(new NumberNode(10), "/", new NumberNode(2));
        assertTrue((Boolean) new BinaryOpNode(div, "==", new NumberNode(5)).evaluate(env));
    }

    @Test
    void nested_leftAssociativity_subtraction() {
        // (10 - 3) - 2 = 5
        BinaryOpNode left = new BinaryOpNode(new NumberNode(10), "-", new NumberNode(3));
        assertEquals(5.0, new BinaryOpNode(left, "-", new NumberNode(2)).evaluate(env));
    }

    @Test
    void nested_leftAssociativity_division() {
        // (100 / 10) / 2 = 5
        BinaryOpNode left = new BinaryOpNode(new NumberNode(100), "/", new NumberNode(10));
        assertEquals(5.0, new BinaryOpNode(left, "/", new NumberNode(2)).evaluate(env));
    }

    @Test
    void nested_undefinedVariableDeep_throws() {
        env.set("x", 5.0);
        BinaryOpNode inner = new BinaryOpNode(
                new VariableNode("x"), "+", new VariableNode("missing"));
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(inner, "*", new NumberNode(2)).evaluate(env));
    }


    // ════════════════════════════════════════════════════════════
    //  Integration Tests
    // ════════════════════════════════════════════════════════════

    @Test
    void integration_priceAfterDiscount() {
        env.set("price", 100.0);
        env.set("discount", 20.0);
        assertEquals(80.0, new BinaryOpNode(
                new VariableNode("price"), "-", new VariableNode("discount")
        ).evaluate(env));
    }

    @Test
    void integration_priceAfterDiscountPercent() {
        env.set("price", 200.0);
        env.set("discountPct", 10.0);
        BinaryOpNode mul = new BinaryOpNode(
                new VariableNode("price"), "*", new VariableNode("discountPct"));
        BinaryOpNode div = new BinaryOpNode(mul, "/", new NumberNode(100));
        assertEquals(180.0, new BinaryOpNode(
                new VariableNode("price"), "-", div).evaluate(env));
    }

    @Test
    void integration_chainedArithmetic() {
        // ((10 + 5) * 2) - 8 = 22
        BinaryOpNode add = new BinaryOpNode(new NumberNode(10), "+", new NumberNode(5));
        BinaryOpNode mul = new BinaryOpNode(add, "*", new NumberNode(2));
        assertEquals(22.0, new BinaryOpNode(mul, "-", new NumberNode(8)).evaluate(env));
    }

    @Test
    void integration_variableUpdated_expressionReflectsNewValue() {
        env.set("x", 5.0);
        BinaryOpNode expr = new BinaryOpNode(new VariableNode("x"), "+", new NumberNode(5));
        assertEquals(10.0, expr.evaluate(env));
        env.set("x", 20.0);
        assertEquals(25.0, expr.evaluate(env));
    }

    @Test
    void integration_sameEnv_sumAndProduct_compared() {
        env.set("a", 2.0);
        env.set("b", 3.0);
        BinaryOpNode sum = new BinaryOpNode(new VariableNode("a"), "+", new VariableNode("b"));
        BinaryOpNode product = new BinaryOpNode(new VariableNode("a"), "*", new VariableNode("b"));
        assertFalse((Boolean) new BinaryOpNode(sum, "==", product).evaluate(env)); // 5 != 6
    }

    @Test
    void integration_undefinedVariableInNested_throws() {
        env.set("x", 10.0);
        assertThrows(BloopRuntimeException.class, () ->
                new BinaryOpNode(
                        new VariableNode("x"), "+", new VariableNode("y")
                ).evaluate(env));
    }

    @Test
    void integration_ageCheck_isAdult_true() {
        env.set("age", 20.0);
        env.set("limit", 18.0);
        BinaryOpNode diff = new BinaryOpNode(
                new VariableNode("age"), "-", new VariableNode("limit"));
        assertTrue((Boolean) new BinaryOpNode(diff, ">", new NumberNode(0)).evaluate(env));
    }

    @Test
    void integration_ageCheck_isAdult_false() {
        env.set("age", 15.0);
        env.set("limit", 18.0);
        BinaryOpNode diff = new BinaryOpNode(
                new VariableNode("age"), "-", new VariableNode("limit"));
        assertFalse((Boolean) new BinaryOpNode(diff, ">", new NumberNode(0)).evaluate(env));
    }

    @Test
    void integration_ageCheck_exactlyAtLimit_notGreater() {
        env.set("age", 18.0);
        env.set("limit", 18.0);
        BinaryOpNode diff = new BinaryOpNode(
                new VariableNode("age"), "-", new VariableNode("limit"));
        assertFalse((Boolean) new BinaryOpNode(diff, ">", new NumberNode(0)).evaluate(env));
    }

    @Test
    void integration_ageCheck_exactlyAtLimit_greaterOrEqual_true() {
        env.set("age", 18.0);
        env.set("limit", 18.0);
        BinaryOpNode diff = new BinaryOpNode(
                new VariableNode("age"), "-", new VariableNode("limit"));
        assertTrue((Boolean) new BinaryOpNode(diff, ">=", new NumberNode(0)).evaluate(env));
    }

    @Test
    void integration_quadraticExpression() {
        // x*x + 2*x + 1  where x=3 → 16
        env.set("x", 3.0);
        BinaryOpNode xSquared = new BinaryOpNode(
                new VariableNode("x"), "*", new VariableNode("x"));
        BinaryOpNode twoX = new BinaryOpNode(
                new NumberNode(2), "*", new VariableNode("x"));
        BinaryOpNode left = new BinaryOpNode(xSquared, "+", twoX);
        assertEquals(16.0, new BinaryOpNode(left, "+", new NumberNode(1)).evaluate(env));
    }

    @Test
    void integration_quadraticExpression_differentX() {
        // x*x + 2*x + 1  where x=0 → 1
        env.set("x", 0.0);
        BinaryOpNode xSquared = new BinaryOpNode(
                new VariableNode("x"), "*", new VariableNode("x"));
        BinaryOpNode twoX = new BinaryOpNode(
                new NumberNode(2), "*", new VariableNode("x"));
        BinaryOpNode left = new BinaryOpNode(xSquared, "+", twoX);
        assertEquals(1.0, new BinaryOpNode(left, "+", new NumberNode(1)).evaluate(env));
    }

    @Test
    void integration_averageOfThreeVars() {
        // (10 + 20 + 30) / 3 = 20
        env.set("a", 10.0);
        env.set("b", 20.0);
        env.set("c", 30.0);
        BinaryOpNode ab = new BinaryOpNode(new VariableNode("a"), "+", new VariableNode("b"));
        BinaryOpNode abc = new BinaryOpNode(ab, "+", new VariableNode("c"));
        assertEquals(20.0, new BinaryOpNode(abc, "/", new NumberNode(3)).evaluate(env));
    }

    @Test
    void integration_averageAboveThreshold_true() {
        env.set("s1", 60.0);
        env.set("s2", 70.0);
        env.set("s3", 80.0);
        env.set("threshold", 65.0);
        BinaryOpNode sum = new BinaryOpNode(
                new BinaryOpNode(new VariableNode("s1"), "+", new VariableNode("s2")),
                "+", new VariableNode("s3"));
        BinaryOpNode avg = new BinaryOpNode(sum, "/", new NumberNode(3));
        assertTrue((Boolean) new BinaryOpNode(avg, ">", new VariableNode("threshold")).evaluate(env));
    }

    @Test
    void integration_averageBelowThreshold_false() {
        env.set("s1", 40.0);
        env.set("s2", 50.0);
        env.set("s3", 60.0);
        env.set("threshold", 65.0);
        BinaryOpNode sum = new BinaryOpNode(
                new BinaryOpNode(new VariableNode("s1"), "+", new VariableNode("s2")),
                "+", new VariableNode("s3"));
        BinaryOpNode avg = new BinaryOpNode(sum, "/", new NumberNode(3));
        assertFalse((Boolean) new BinaryOpNode(avg, ">", new VariableNode("threshold")).evaluate(env));
    }

    @Test
    void integration_averageExactlyAtThreshold_notGreater() {
        env.set("s1", 60.0);
        env.set("s2", 60.0);
        env.set("s3", 75.0);
        env.set("threshold", 65.0);
        BinaryOpNode sum = new BinaryOpNode(
                new BinaryOpNode(new VariableNode("s1"), "+", new VariableNode("s2")),
                "+", new VariableNode("s3"));
        BinaryOpNode avg = new BinaryOpNode(sum, "/", new NumberNode(3));
        assertFalse((Boolean) new BinaryOpNode(avg, ">", new VariableNode("threshold")).evaluate(env));
    }

    @Test
    void integration_variableUsedMultipleTimes_inOneExpr() {
        // x * x - x = 25 - 5 = 20
        env.set("x", 5.0);
        BinaryOpNode xSquare = new BinaryOpNode(
                new VariableNode("x"), "*", new VariableNode("x"));
        assertEquals(20.0, new BinaryOpNode(xSquare, "-", new VariableNode("x")).evaluate(env));
    }

    @Test
    void integration_envSharedAcrossMultipleExpressions() {
        env.set("base", 10.0);
        env.set("rate", 5.0);
        BinaryOpNode interest = new BinaryOpNode(
                new VariableNode("base"), "*", new VariableNode("rate")); // 50
        BinaryOpNode total = new BinaryOpNode(
                new VariableNode("base"), "+", interest);                  // 60
        BinaryOpNode isGood = new BinaryOpNode(
                total, ">", new NumberNode(55));                           // true
        assertEquals(50.0, interest.evaluate(env));
        assertEquals(60.0, total.evaluate(env));
        assertTrue((Boolean) isGood.evaluate(env));
    }

    @Test
    void integration_numberNodeDirectlyInCondition() {
        assertTrue((Boolean) new BinaryOpNode(
                new NumberNode(1), "==", new NumberNode(1)).evaluate(env));
        assertFalse((Boolean) new BinaryOpNode(
                new NumberNode(1), "!=", new NumberNode(1)).evaluate(env));
    }

    @Test
    void integration_stringVariable_plus_numberVariable_concatenates() {
        env.set("name", "Bloop");
        env.set("x", 5.0);

        assertEquals("Bloop5",
                new BinaryOpNode(
                        new VariableNode("name"), "+", new VariableNode("x")
                ).evaluate(env));
    }
    @Test
    void integration_freshEnv_noVariablesLeak() {
        assertThrows(BloopRuntimeException.class,
                () -> new VariableNode("price").evaluate(env));
        assertThrows(BloopRuntimeException.class,
                () -> new VariableNode("x").evaluate(env));
    }

    @Test
    void integration_countdownExpression() {
        env.set("start", 10.0);
        env.set("step", 3.0);
        env.set("target", 7.0);
        BinaryOpNode sub = new BinaryOpNode(
                new VariableNode("start"), "-", new VariableNode("step"));
        assertTrue((Boolean) new BinaryOpNode(
                sub, "==", new VariableNode("target")).evaluate(env));
    }

    @Test
    void integration_nestedVarsAndLiterals_mixedDepth() {
        // ((x + 1) * (y - 1)) / z  where x=4, y=6, z=5 → 5
        env.set("x", 4.0);
        env.set("y", 6.0);
        env.set("z", 5.0);
        BinaryOpNode left = new BinaryOpNode(new VariableNode("x"), "+", new NumberNode(1));
        BinaryOpNode right = new BinaryOpNode(new VariableNode("y"), "-", new NumberNode(1));
        BinaryOpNode mul = new BinaryOpNode(left, "*", right);
        assertEquals(5.0, new BinaryOpNode(mul, "/", new VariableNode("z")).evaluate(env));
    }

    @Test
    void integration_simpleInterestFormula() {
        // SI = (principal * rate * time) / 100 = 100
        env.set("principal", 1000.0);
        env.set("rate", 5.0);
        env.set("time", 2.0);
        BinaryOpNode pr = new BinaryOpNode(
                new VariableNode("principal"), "*", new VariableNode("rate"));
        BinaryOpNode prt = new BinaryOpNode(pr, "*", new VariableNode("time"));
        assertEquals(100.0, new BinaryOpNode(prt, "/", new NumberNode(100)).evaluate(env));
    }

    @Test
    void integration_boundaryCheck_withinRange_true() {
        env.set("value", 5.0);
        env.set("min", 0.0);
        env.set("max", 10.0);
        BinaryOpNode lowerOk = new BinaryOpNode(
                new VariableNode("value"), ">=", new VariableNode("min"));
        BinaryOpNode upperOk = new BinaryOpNode(
                new VariableNode("value"), "<=", new VariableNode("max"));
        assertTrue((Boolean) lowerOk.evaluate(env));
        assertTrue((Boolean) upperOk.evaluate(env));
    }

    @Test
    void integration_boundaryCheck_outOfRange_false() {
        env.set("value", 15.0);
        env.set("max", 10.0);
        BinaryOpNode upperOk = new BinaryOpNode(
                new VariableNode("value"), "<=", new VariableNode("max"));
        assertFalse((Boolean) upperOk.evaluate(env));
    }

    @Test
    void integration_variableReassigned_conditionFlips() {
        env.set("x", 10.0);
        BinaryOpNode cond = new BinaryOpNode(
                new VariableNode("x"), ">", new NumberNode(5));
        assertTrue((Boolean) cond.evaluate(env));
        env.set("x", 3.0);
        assertFalse((Boolean) cond.evaluate(env));
    }

    @Test
    void integration_twoExpressionsCompared_equal() {
        // (2 * 6) == (3 * 4) → true
        BinaryOpNode left = new BinaryOpNode(new NumberNode(2), "*", new NumberNode(6));
        BinaryOpNode right = new BinaryOpNode(new NumberNode(3), "*", new NumberNode(4));
        assertTrue((Boolean) new BinaryOpNode(left, "==", right).evaluate(env));
    }

    @Test
    void integration_twoExpressionsCompared_notEqual() {
        // (2 + 6) == (3 * 4) → false
        BinaryOpNode left = new BinaryOpNode(new NumberNode(2), "+", new NumberNode(6));
        BinaryOpNode right = new BinaryOpNode(new NumberNode(3), "*", new NumberNode(4));
        assertFalse((Boolean) new BinaryOpNode(left, "==", right).evaluate(env));
    }
}