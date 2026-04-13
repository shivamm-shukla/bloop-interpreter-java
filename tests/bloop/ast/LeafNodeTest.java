package bloop.ast;

import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Leaf AST Nodes — exhaustive")
class LeafNodeTest {

    private Environment env;

    @BeforeEach
    void setUp() { env = new Environment(); }

    // ══════════════════════════════════════════════════════════════════════
    // NUMBER NODE
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("NumberNode")
    class NumberNodeTests {

        @Test void returnsStoredDouble()    { assertEquals(42.0,  (double) new NumberNode(42).evaluate(env)); }
        @Test void zero()                   { assertEquals(0.0,   (double) new NumberNode(0).evaluate(env)); }
        @Test void negative()               { assertEquals(-7.5,  (double) new NumberNode(-7.5).evaluate(env)); }
        @Test void fractional()             { assertEquals(3.14,  (double) new NumberNode(3.14).evaluate(env), 1e-9); }
        @Test void maxValue()               { assertEquals(Double.MAX_VALUE, (double) new NumberNode(Double.MAX_VALUE).evaluate(env)); }
        @Test void minValue()               { assertEquals(Double.MIN_VALUE, (double) new NumberNode(Double.MIN_VALUE).evaluate(env)); }
        @Test void positiveInfinity()       { assertEquals(Double.POSITIVE_INFINITY, (double) new NumberNode(Double.POSITIVE_INFINITY).evaluate(env)); }
        @Test void negativeInfinity()       { assertEquals(Double.NEGATIVE_INFINITY, (double) new NumberNode(Double.NEGATIVE_INFINITY).evaluate(env)); }
        @Test void negativeZero()           { assertEquals(-0.0, (double) new NumberNode(-0.0).evaluate(env), 0.0); }
        @Test void one()                    { assertEquals(1.0,   (double) new NumberNode(1).evaluate(env)); }
        @Test void minusOne()               { assertEquals(-1.0,  (double) new NumberNode(-1).evaluate(env)); }
        @Test void verySmall()              { assertEquals(1e-300, (double) new NumberNode(1e-300).evaluate(env)); }
        @Test void veryLarge()              { assertEquals(1e300,  (double) new NumberNode(1e300).evaluate(env)); }
        @Test void pi()                     { assertEquals(Math.PI, (double) new NumberNode(Math.PI).evaluate(env), 1e-15); }
        @Test void resultTypeIsDouble()     { assertInstanceOf(Double.class, new NumberNode(5).evaluate(env)); }
        @Test void doesNotMutateEnv()       { new NumberNode(99).evaluate(env); assertTrue(env.snapshot().isEmpty()); }
        @Test void sameValueEachCall()      {
            NumberNode n = new NumberNode(7);
            assertEquals(n.evaluate(env), n.evaluate(env));
        }
        @Test void envIgnored()             {
            env.set("x", 999.0);
            assertEquals(42.0, (double) new NumberNode(42).evaluate(env));
        }

        @Nested @DisplayName("toString")
        class ToStr {
            @Test void containsClassName()  { assertTrue(new NumberNode(5).toString().contains("NumberNode")); }
            @Test void containsValue()      { assertTrue(new NumberNode(99.0).toString().contains("99")); }
            @Test void negativeValue()      { assertTrue(new NumberNode(-3.0).toString().contains("-3") || new NumberNode(-3.0).toString().contains("3")); }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STRING NODE
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("StringNode")
    class StringNodeTests {

        @Test void returnsStoredString()     { assertEquals("hello",    new StringNode("hello").evaluate(env)); }
        @Test void emptyString()             { assertEquals("",         new StringNode("").evaluate(env)); }
        @Test void singleChar()              { assertEquals("x",        new StringNode("x").evaluate(env)); }
        @Test void stringWithSpaces()        { assertEquals("a b c",    new StringNode("a b c").evaluate(env)); }
        @Test void leadingSpaces()           { assertEquals("  hi",     new StringNode("  hi").evaluate(env)); }
        @Test void trailingSpaces()          { assertEquals("hi  ",     new StringNode("hi  ").evaluate(env)); }
        @Test void specialChars()            { assertEquals("!@#$%^&*()", new StringNode("!@#$%^&*()").evaluate(env)); }
        @Test void embeddedNewline()         { assertEquals("a\nb",     new StringNode("a\nb").evaluate(env)); }
        @Test void embeddedTab()             { assertEquals("a\tb",     new StringNode("a\tb").evaluate(env)); }
        @Test void numericLookingString()    {
            Object r = new StringNode("42").evaluate(env);
            assertInstanceOf(String.class, r);
            assertEquals("42", r);
        }
        @Test void unicodeString()           { assertEquals("こんにちは", new StringNode("こんにちは").evaluate(env)); }
        @Test void longString()              {
            String s = "x".repeat(10_000);
            assertEquals(s, new StringNode(s).evaluate(env));
        }
        @Test void backslashString()         { assertEquals("a\\b", new StringNode("a\\b").evaluate(env)); }
        @Test void quoteInsideString()       { assertEquals("say \"hi\"", new StringNode("say \"hi\"").evaluate(env)); }
        @Test void resultTypeIsString()      { assertInstanceOf(String.class, new StringNode("x").evaluate(env)); }
        @Test void doesNotMutateEnv()        { new StringNode("test").evaluate(env); assertTrue(env.snapshot().isEmpty()); }
        @Test void sameValueEachCall()       {
            StringNode n = new StringNode("hello");
            assertEquals(n.evaluate(env), n.evaluate(env));
        }

        @Nested @DisplayName("toString")
        class ToStr {
            @Test void containsClassName()  { assertTrue(new StringNode("hi").toString().contains("StringNode")); }
            @Test void containsValue()      { assertTrue(new StringNode("hi").toString().contains("hi")); }
            @Test void emptyStringInToStr() { assertTrue(new StringNode("").toString().contains("StringNode")); }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // VARIABLE NODE
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("VariableNode")
    class VariableNodeTests {

        @Test void retrievesDouble()         { env.set("x", 10.0);     assertEquals(10.0,    (double) new VariableNode("x").evaluate(env)); }
        @Test void retrievesString()         { env.set("s", "bloop");  assertEquals("bloop", new VariableNode("s").evaluate(env)); }
        @Test void retrievesBoolean()        { env.set("b", true);     assertEquals(true,    new VariableNode("b").evaluate(env)); }
        @Test void retrievesZero()           { env.set("z", 0.0);      assertEquals(0.0,     (double) new VariableNode("z").evaluate(env)); }
        @Test void retrievesNegative()       { env.set("n", -5.0);     assertEquals(-5.0,    (double) new VariableNode("n").evaluate(env)); }
        @Test void retrievesEmptyString()    { env.set("e", "");       assertEquals("",      new VariableNode("e").evaluate(env)); }
        @Test void retrievesUpdatedValue()   {
            env.set("x", 1.0); env.set("x", 99.0);
            assertEquals(99.0, (double) new VariableNode("x").evaluate(env));
        }
        @Test void undefinedVariableThrows() {
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class,
                    () -> new VariableNode("ghost").evaluate(env));
            assertTrue(ex.getMessage().contains("ghost"));
        }
        @Test void caseSensitiveLowerUpper() {
            env.set("x", 1.0);
            assertThrows(BloopRuntimeException.class, () -> new VariableNode("X").evaluate(env));
        }
        @Test void caseSensitiveUpperLower() {
            env.set("X", 2.0);
            assertThrows(BloopRuntimeException.class, () -> new VariableNode("x").evaluate(env));
        }
        @Test void underscoreName()          { env.set("my_var", 5.0); assertEquals(5.0, (double) new VariableNode("my_var").evaluate(env)); }
        @Test void numericSuffixName()       { env.set("x1", 3.0);    assertEquals(3.0, (double) new VariableNode("x1").evaluate(env)); }
        @Test void longVariableName()        {
            String name = "a".repeat(100);
            env.set(name, 7.0);
            assertEquals(7.0, (double) new VariableNode(name).evaluate(env));
        }
        @Test void multipleVariablesIndep() {
            env.set("a", 1.0); env.set("b", 2.0); env.set("c", 3.0);
            assertEquals(1.0, (double) new VariableNode("a").evaluate(env));
            assertEquals(2.0, (double) new VariableNode("b").evaluate(env));
            assertEquals(3.0, (double) new VariableNode("c").evaluate(env));
        }
        @Test void sameNodeDifferentEnvValues() {
            VariableNode node = new VariableNode("x");
            env.set("x", 10.0); assertEquals(10.0, (double) node.evaluate(env));
            env.set("x", 20.0); assertEquals(20.0, (double) node.evaluate(env));
        }
        @Test void typeChangeInEnvReturnedCorrectly() {
            env.set("v", 1.0);
            env.set("v", "now a string");
            assertInstanceOf(String.class, new VariableNode("v").evaluate(env));
        }

        @Test void getVariableNameReturnsName() { assertEquals("myVar", new VariableNode("myVar").getVariableName()); }
        @Test void getVariableNameUnderscored()  { assertEquals("my_var", new VariableNode("my_var").getVariableName()); }

        @Nested @DisplayName("toString")
        class ToStr {
            @Test void containsClassName()  { assertTrue(new VariableNode("total").toString().contains("VariableNode")); }
            @Test void containsVarName()    { assertTrue(new VariableNode("total").toString().contains("total")); }
        }
    }
}