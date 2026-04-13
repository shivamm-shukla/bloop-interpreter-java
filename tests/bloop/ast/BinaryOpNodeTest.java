package bloop.ast;

import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BinaryOpNode — exhaustive")
class BinaryOpNodeTest {

    private Environment env;

    @BeforeEach
    void setUp() { env = new Environment(); }

    private BinaryOpNode numOp(double l, String op, double r) {
        return new BinaryOpNode(new NumberNode(l), op, new NumberNode(r));
    }
    private double evalNum(double l, String op, double r) {
        return (double) numOp(l, op, r).evaluate(env);
    }
    private boolean evalBool(double l, String op, double r) {
        return (boolean) numOp(l, op, r).evaluate(env);
    }
    private boolean evalBoolStr(String l, String op, String r) {
        return (boolean) new BinaryOpNode(new StringNode(l), op, new StringNode(r)).evaluate(env);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ADDITION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("+ operator")
    class Addition {
        @Test void posPos()              { assertEquals(5.0,  evalNum(2, "+", 3)); }
        @Test void posNeg()              { assertEquals(-1.0, evalNum(2, "+", -3)); }
        @Test void negPos()              { assertEquals(1.0,  evalNum(-2, "+", 3)); }
        @Test void negNeg()              { assertEquals(-5.0, evalNum(-2, "+", -3)); }
        @Test void zeroZero()            { assertEquals(0.0,  evalNum(0, "+", 0)); }
        @Test void rightIdentity()       { assertEquals(7.0,  evalNum(7, "+", 0)); }
        @Test void leftIdentity()        { assertEquals(7.0,  evalNum(0, "+", 7)); }
        @Test void addInverse()          { assertEquals(0.0,  evalNum(42, "+", -42)); }
        @Test void floatPrecision()      { assertEquals(0.3,  evalNum(0.1, "+", 0.2), 1e-9); }
        @Test void largeNumbers()        { assertEquals(2e15, evalNum(1e15, "+", 1e15), 1.0); }
        @Test void overflowToInfinity()  { assertTrue(Double.isInfinite(evalNum(Double.MAX_VALUE, "+", Double.MAX_VALUE))); }
        @Test void verySmallFloats()     { assertEquals(2e-300, evalNum(1e-300, "+", 1e-300), 1e-310); }
        @Test void commutative()         { assertEquals(evalNum(3, "+", 7), evalNum(7, "+", 3)); }
        @Test void resultIsDouble()      { assertInstanceOf(Double.class, numOp(1, "+", 2).evaluate(env)); }
        @Test void minValuePlusMin()     { assertTrue(evalNum(Double.MIN_VALUE, "+", Double.MIN_VALUE) > 0); }
        @Test void negZeroPlusZero()     { assertEquals(0.0, evalNum(-0.0, "+", 0.0)); }
        @Test void integerResult()       { assertEquals(100.0, evalNum(75, "+", 25)); }
        @Test void decimalAddition()     { assertEquals(1.5, evalNum(0.75, "+", 0.75), 1e-15); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SUBTRACTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("- operator")
    class Subtraction {
        @Test void largerMinusSmaller()  { assertEquals(2.0,  evalNum(5, "-", 3)); }
        @Test void smallerMinusLarger()  { assertEquals(-1.0, evalNum(2, "-", 3)); }
        @Test void rightIdentity()       { assertEquals(9.0,  evalNum(9, "-", 0)); }
        @Test void zeroMinusX()          { assertEquals(-9.0, evalNum(0, "-", 9)); }
        @Test void selfSubtract()        { assertEquals(0.0,  evalNum(42, "-", 42)); }
        @Test void negMinusNeg()         { assertEquals(1.0,  evalNum(-2, "-", -3)); }
        @Test void negMinusPos()         { assertEquals(-5.0, evalNum(-2, "-", 3)); }
        @Test void floatSubtract()       { assertEquals(0.1,  evalNum(0.3, "-", 0.2), 1e-9); }
        @Test void maxMinusMax()         { assertEquals(0.0,  evalNum(Double.MAX_VALUE, "-", Double.MAX_VALUE)); }
        @Test void notCommutative()      { assertNotEquals(evalNum(5, "-", 3), evalNum(3, "-", 5)); }
        @Test void resultIsDouble()      { assertInstanceOf(Double.class, numOp(5, "-", 3).evaluate(env)); }
        @Test void doubleNegation()      { assertEquals(5.0, evalNum(0, "-", -5)); }
        @Test void decimalSubtraction()  { assertEquals(0.25, evalNum(0.75, "-", 0.50), 1e-15); }
        @Test void largeSubtraction()    { assertEquals(1e14, evalNum(1e15, "-", 9e14), 1.0); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MULTIPLICATION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("* operator")
    class Multiplication {
        @Test void posPos()              { assertEquals(12.0, evalNum(3, "*", 4)); }
        @Test void timesZeroRight()      { assertEquals(0.0,  evalNum(99, "*", 0)); }
        @Test void timesZeroLeft()       { assertEquals(0.0,  evalNum(0, "*", 99)); }
        @Test void timesOneRight()       { assertEquals(7.0,  evalNum(7, "*", 1)); }
        @Test void timesOneLeft()        { assertEquals(7.0,  evalNum(1, "*", 7)); }
        @Test void timesMinusOne()       { assertEquals(-5.0, evalNum(5, "*", -1)); }
        @Test void negTimesPos()         { assertEquals(-6.0, evalNum(-2, "*", 3)); }
        @Test void posTimesNeg()         { assertEquals(-6.0, evalNum(2, "*", -3)); }
        @Test void negTimesNeg()         { assertEquals(6.0,  evalNum(-2, "*", -3)); }
        @Test void floatMul()            { assertEquals(0.06, evalNum(0.2, "*", 0.3), 1e-9); }
        @Test void commutative()         { assertEquals(evalNum(3, "*", 7), evalNum(7, "*", 3)); }
        @Test void overflow()            { assertTrue(Double.isInfinite(evalNum(Double.MAX_VALUE, "*", 2))); }
        @Test void resultIsDouble()      { assertInstanceOf(Double.class, numOp(2, "*", 3).evaluate(env)); }
        @Test void squaring()            { assertEquals(25.0, evalNum(5, "*", 5)); }
        @Test void fractionTimesFraction(){ assertEquals(0.25, evalNum(0.5, "*", 0.5), 1e-15); }
        @Test void associative()         {
            // (2*3)*4 == 2*(3*4)
            assertEquals(evalNum(6, "*", 4), evalNum(2, "*", 12));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DIVISION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("/ operator")
    class Division {
        @Test void exactDivision()       { assertEquals(4.0,   evalNum(12, "/", 3)); }
        @Test void fractionalResult()    { assertEquals(2.5,   evalNum(5, "/", 2)); }
        @Test void divByOne()            { assertEquals(8.0,   evalNum(8, "/", 1)); }
        @Test void zeroNumerator()       { assertEquals(0.0,   evalNum(0, "/", 5)); }
        @Test void negDivPos()           { assertEquals(-2.0,  evalNum(-6, "/", 3)); }
        @Test void posDivNeg()           { assertEquals(-2.0,  evalNum(6, "/", -3)); }
        @Test void negDivNeg()           { assertEquals(2.0,   evalNum(-6, "/", -3)); }
        @Test void selfDivision()        { assertEquals(1.0,   evalNum(7, "/", 7)); }
        @Test void oneThird()            { assertEquals(1.0/3, evalNum(1, "/", 3), 1e-15); }
        @Test void floatDiv()            { assertEquals(0.1,   evalNum(1.0, "/", 10.0), 1e-15); }
        @Test void resultIsDouble()      { assertInstanceOf(Double.class, numOp(6, "/", 3).evaluate(env)); }

        @Test @DisplayName("x / 0 throws BloopRuntimeException")
        void divByZero() {
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> evalNum(5, "/", 0));
            assertTrue(ex.getMessage().contains("Division by zero"));
        }
        @Test @DisplayName("-x / 0 throws")
        void negDivZero() { assertThrows(BloopRuntimeException.class, () -> evalNum(-5, "/", 0)); }
        @Test @DisplayName("0 / 0 throws")
        void zeroDivZero() { assertThrows(BloopRuntimeException.class, () -> evalNum(0, "/", 0)); }
        @Test @DisplayName("div-by-zero error message mentions zero")
        void divByZeroMessage() {
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> evalNum(1, "/", 0));
            assertTrue(ex.getMessage().toLowerCase().contains("zero") ||
                    ex.getMessage().toLowerCase().contains("division"));
        }
        @Test void largeDiv()            { assertEquals(1e100, evalNum(1e200, "/", 1e100), 1.0); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GREATER THAN  >
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("> operator")
    class GreaterThan {
        @Test void strictlyGreater()    { assertTrue(evalBool(5, ">", 3)); }
        @Test void notGreater()         { assertFalse(evalBool(3, ">", 5)); }
        @Test void equalNotGreater()    { assertFalse(evalBool(5, ">", 5)); }
        @Test void negativeGreater()    { assertTrue(evalBool(-1, ">", -5)); }
        @Test void zeroGreaterNeg()     { assertTrue(evalBool(0, ">", -1)); }
        @Test void negNotGreaterZero()  { assertFalse(evalBool(-1, ">", 0)); }
        @Test void floatGreater()       { assertTrue(evalBool(3.5, ">", 3.4)); }
        @Test void floatNotGreater()    { assertFalse(evalBool(3.4, ">", 3.5)); }
        @Test void floatTinyDiff()      { assertTrue(evalBool(1.0000001, ">", 1.0000000)); }
        @Test void resultIsBoolean()    { assertInstanceOf(Boolean.class, numOp(1, ">", 0).evaluate(env)); }
        @Test void maxGtMin()           { assertTrue(evalBool(Double.MAX_VALUE, ">", Double.MIN_VALUE)); }
        @Test @DisplayName("string left throws")
        void stringLeftThrows() {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new StringNode("a"), ">", new NumberNode(1)).evaluate(env));
        }
        @Test @DisplayName("string right throws")
        void stringRightThrows() {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new NumberNode(1), ">", new StringNode("b")).evaluate(env));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GREATER EQUAL  >=
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName(">= operator")
    class GreaterEqual {
        @Test void strictlyGreater()    { assertTrue(evalBool(5, ">=", 3)); }
        @Test void equal()              { assertTrue(evalBool(5, ">=", 5)); }
        @Test void less()               { assertFalse(evalBool(3, ">=", 5)); }
        @Test void negativeEqual()      { assertTrue(evalBool(-3, ">=", -3)); }
        @Test void zeroGeNeg()          { assertTrue(evalBool(0, ">=", -1)); }
        @Test void floatGe()            { assertTrue(evalBool(3.5, ">=", 3.5)); }
        @Test void floatGeStrict()      { assertTrue(evalBool(3.6, ">=", 3.5)); }
        @Test void resultIsBoolean()    { assertInstanceOf(Boolean.class, numOp(1, ">=", 1).evaluate(env)); }
        @Test @DisplayName("string throws")
        void stringThrows() {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new StringNode("x"), ">=", new NumberNode(1)).evaluate(env));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LESS THAN  <
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("< operator")
    class LessThan {
        @Test void strictlyLess()       { assertTrue(evalBool(3, "<", 5)); }
        @Test void notLess()            { assertFalse(evalBool(5, "<", 3)); }
        @Test void equalNotLess()       { assertFalse(evalBool(5, "<", 5)); }
        @Test void negativeLess()       { assertTrue(evalBool(-5, "<", -1)); }
        @Test void negLessZero()        { assertTrue(evalBool(-1, "<", 0)); }
        @Test void floatLess()          { assertTrue(evalBool(3.4, "<", 3.5)); }
        @Test void floatTinyDiff()      { assertTrue(evalBool(0.9999999, "<", 1.0000000)); }
        @Test void resultIsBoolean()    { assertInstanceOf(Boolean.class, numOp(0, "<", 1).evaluate(env)); }
        @Test void minLtMax()           { assertTrue(evalBool(Double.MIN_VALUE, "<", Double.MAX_VALUE)); }
        @Test @DisplayName("string throws")
        void stringThrows() {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new StringNode("z"), "<", new NumberNode(1)).evaluate(env));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LESS EQUAL  <=
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("<= operator")
    class LessEqual {
        @Test void strictlyLess()       { assertTrue(evalBool(3, "<=", 5)); }
        @Test void equal()              { assertTrue(evalBool(5, "<=", 5)); }
        @Test void greater()            { assertFalse(evalBool(5, "<=", 3)); }
        @Test void negativeEqual()      { assertTrue(evalBool(-3, "<=", -3)); }
        @Test void negLeZero()          { assertTrue(evalBool(-1, "<=", 0)); }
        @Test void floatLe()            { assertTrue(evalBool(3.5, "<=", 3.5)); }
        @Test void floatLeStrict()      { assertTrue(evalBool(3.4, "<=", 3.5)); }
        @Test void resultIsBoolean()    { assertInstanceOf(Boolean.class, numOp(1, "<=", 1).evaluate(env)); }
        @Test @DisplayName("string throws")
        void stringThrows() {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new StringNode("x"), "<=", new NumberNode(1)).evaluate(env));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // EQUAL EQUAL  ==
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("== operator")
    class EqualEqual {
        @Test void equalIntegers()          { assertTrue(evalBool(5, "==", 5)); }
        @Test void unequalIntegers()        { assertFalse(evalBool(5, "==", 6)); }
        @Test void equalFloats()            { assertTrue(evalBool(3.14, "==", 3.14)); }
        @Test void unequalFloats()          { assertFalse(evalBool(3.14, "==", 3.15)); }
        @Test void zeroEqualsZero()         { assertTrue(evalBool(0, "==", 0)); }
        @Test void negEqualsNeg()           { assertTrue(evalBool(-7, "==", -7)); }
        @Test void negNotEqualsPos()        { assertFalse(evalBool(-5, "==", 5)); }
        @Test void equalStrings()           { assertTrue(evalBoolStr("hi", "==", "hi")); }
        @Test void unequalStrings()         { assertFalse(evalBoolStr("hi", "==", "bye")); }
        @Test void emptyStringEq()          { assertTrue(evalBoolStr("", "==", "")); }
        @Test void caseSensitive()          { assertFalse(evalBoolStr("Hi", "==", "hi")); }
        @Test void spaceMatters()           { assertFalse(evalBoolStr("hi ", "==", "hi")); }
        @Test void numberVsStringFalse()    {
            assertFalse((boolean) new BinaryOpNode(new NumberNode(1), "==", new StringNode("1")).evaluate(env));
        }
        @Test void stringVsNumberFalse()    {
            assertFalse((boolean) new BinaryOpNode(new StringNode("1"), "==", new NumberNode(1)).evaluate(env));
        }
        @Test void zeroVsEmptyStringFalse() {
            assertFalse((boolean) new BinaryOpNode(new NumberNode(0), "==", new StringNode("")).evaluate(env));
        }
        @Test void maxValueEqItself()       { assertTrue(evalBool(Double.MAX_VALUE, "==", Double.MAX_VALUE)); }
        @Test void resultIsBoolean()        { assertInstanceOf(Boolean.class, numOp(1, "==", 1).evaluate(env)); }
        @Test void longStringEquality()     {
            String s = "a".repeat(1000);
            assertTrue(evalBoolStr(s, "==", s));
        }
        @Test void specialCharsStringEq()   { assertTrue(evalBoolStr("!@#$%", "==", "!@#$%")); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NOT EQUAL  !=
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("!= operator")
    class NotEqual {
        @Test void unequalNumbers()         { assertTrue(evalBool(3, "!=", 4)); }
        @Test void equalNumbersFalse()      { assertFalse(evalBool(3, "!=", 3)); }
        @Test void zeroVsOne()              { assertTrue(evalBool(0, "!=", 1)); }
        @Test void negVsPos()               { assertTrue(evalBool(-1, "!=", 1)); }
        @Test void unequalStrings()         { assertTrue(evalBoolStr("a", "!=", "b")); }
        @Test void equalStringsFalse()      { assertFalse(evalBoolStr("x", "!=", "x")); }
        @Test void emptyVsNonEmpty()        { assertTrue(evalBoolStr("", "!=", "x")); }
        @Test void numberVsStringTrue()     {
            assertTrue((boolean) new BinaryOpNode(new NumberNode(0), "!=", new StringNode("0")).evaluate(env));
        }
        @Test void resultIsBoolean()        { assertInstanceOf(Boolean.class, numOp(1, "!=", 2).evaluate(env)); }
        @Test @DisplayName("!= is negation of ==")
        void isNegationOfEquals() {
            assertEquals(!evalBool(5, "==", 5), evalBool(5, "!=", 5));
            assertEquals(!evalBool(5, "==", 6), evalBool(5, "!=", 6));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TYPE ERRORS
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Type errors")
    class TypeErrors {
        @ParameterizedTest(name = "string left of ''{0}''")
        @ValueSource(strings = {"+", "-", "*", "/"})
        void stringLeftArithmetic(String op) {
            BinaryOpNode node = new BinaryOpNode(new StringNode("a"), op, new NumberNode(1));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
            assertTrue(ex.getMessage().contains("must be a number"));
        }

        @ParameterizedTest(name = "string right of ''{0}''")
        @ValueSource(strings = {"+", "-", "*", "/"})
        void stringRightArithmetic(String op) {
            BinaryOpNode node = new BinaryOpNode(new NumberNode(1), op, new StringNode("b"));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
            assertTrue(ex.getMessage().contains("must be a number"));
        }

        @ParameterizedTest(name = "string left of ''{0}''")
        @ValueSource(strings = {">", ">=", "<", "<="})
        void stringLeftComparison(String op) {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new StringNode("a"), op, new NumberNode(1)).evaluate(env));
        }

        @ParameterizedTest(name = "string right of ''{0}''")
        @ValueSource(strings = {">", ">=", "<", "<="})
        void stringRightComparison(String op) {
            assertThrows(BloopRuntimeException.class,
                    () -> new BinaryOpNode(new NumberNode(1), op, new StringNode("b")).evaluate(env));
        }

        @Test @DisplayName("boolean result as arithmetic operand throws")
        void booleanAsArithmetic() {
            BinaryOpNode cmp = new BinaryOpNode(new NumberNode(1), "==", new NumberNode(1));
            BinaryOpNode add = new BinaryOpNode(cmp, "+", new NumberNode(1));
            assertThrows(BloopRuntimeException.class, () -> add.evaluate(env));
        }

        @Test @DisplayName("error message contains offending value")
        void errorContainsValue() {
            BinaryOpNode node = new BinaryOpNode(new StringNode("badval"), "+", new NumberNode(1));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
            assertTrue(ex.getMessage().contains("badval"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UNKNOWN OPERATOR
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Unknown operators")
    class UnknownOperators {
        @ParameterizedTest(name = "''{0}'' throws")
        @ValueSource(strings = {"^", "%", "&&", "||", "**", "//", "~", "and", "or", "mod", "xor"})
        void unknownOp(String op) {
            BinaryOpNode node = new BinaryOpNode(new NumberNode(1), op, new NumberNode(2));
            assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
        }

        @Test @DisplayName("error message mentions the operator")
        void errorMentionsOp() {
            BinaryOpNode node = new BinaryOpNode(new NumberNode(1), "^", new NumberNode(2));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
            assertTrue(ex.getMessage().contains("^") || ex.getMessage().contains("Unknown"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // NESTING & COMPOSITION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Nesting and composition")
    class Nesting {
        @Test @DisplayName("(2 + 3) * 4 = 20")
        void addThenMul() {
            BinaryOpNode inner = new BinaryOpNode(new NumberNode(2), "+", new NumberNode(3));
            assertEquals(20.0, (double) new BinaryOpNode(inner, "*", new NumberNode(4)).evaluate(env));
        }

        @Test @DisplayName("10 - (3 + 2) = 5")
        void subInnerAdd() {
            BinaryOpNode inner = new BinaryOpNode(new NumberNode(3), "+", new NumberNode(2));
            assertEquals(5.0, (double) new BinaryOpNode(new NumberNode(10), "-", inner).evaluate(env));
        }

        @Test @DisplayName("((((1+1)+1)+1)+1) = 5 — deeply nested")
        void deeplyNested() {
            BinaryOpNode n = new BinaryOpNode(new NumberNode(1), "+", new NumberNode(1));
            n = new BinaryOpNode(n, "+", new NumberNode(1));
            n = new BinaryOpNode(n, "+", new NumberNode(1));
            n = new BinaryOpNode(n, "+", new NumberNode(1));
            assertEquals(5.0, (double) n.evaluate(env));
        }

        @Test @DisplayName("comparison of computed: (3*4) > (5+6) → true (12 > 11)")
        void compareComputed() {
            BinaryOpNode left  = new BinaryOpNode(new NumberNode(3), "*", new NumberNode(4));
            BinaryOpNode right = new BinaryOpNode(new NumberNode(5), "+", new NumberNode(6));
            assertTrue((boolean) new BinaryOpNode(left, ">", right).evaluate(env));
        }

        @Test @DisplayName("variable operands resolved from env")
        void variableOperands() {
            env.set("a", 10.0); env.set("b", 3.0);
            assertEquals(7.0, (double) new BinaryOpNode(new VariableNode("a"), "-", new VariableNode("b")).evaluate(env));
        }

        @Test @DisplayName("same node re-evaluated after env change")
        void reEvalAfterEnvChange() {
            env.set("x", 1.0);
            BinaryOpNode node = new BinaryOpNode(new VariableNode("x"), "+", new NumberNode(0));
            assertEquals(1.0, (double) node.evaluate(env));
            env.set("x", 99.0);
            assertEquals(99.0, (double) node.evaluate(env));
        }

        @Test @DisplayName("undefined variable in left operand throws")
        void undefinedLeft() {
            BinaryOpNode node = new BinaryOpNode(new VariableNode("ghost"), "+", new NumberNode(1));
            assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
        }

        @Test @DisplayName("undefined variable in right operand throws")
        void undefinedRight() {
            BinaryOpNode node = new BinaryOpNode(new NumberNode(1), "+", new VariableNode("ghost"));
            assertThrows(BloopRuntimeException.class, () -> node.evaluate(env));
        }

        @Test @DisplayName("(a+b)*(a-b) = a²-b² identity")
        void algebraicIdentity() {
            env.set("a", 5.0); env.set("b", 3.0);
            BinaryOpNode sum  = new BinaryOpNode(new VariableNode("a"), "+", new VariableNode("b"));
            BinaryOpNode diff = new BinaryOpNode(new VariableNode("a"), "-", new VariableNode("b"));
            BinaryOpNode product = new BinaryOpNode(sum, "*", diff);
            assertEquals(16.0, (double) product.evaluate(env)); // 5²-3² = 25-9 = 16
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SPECIAL DOUBLE VALUES
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Special double values")
    class SpecialDoubles {
        @Test void infinityPlusOne()      { assertTrue(Double.isInfinite(evalNum(Double.POSITIVE_INFINITY, "+", 1))); }
        @Test void infinityEquality()     { assertTrue(evalBool(Double.POSITIVE_INFINITY, "==", Double.POSITIVE_INFINITY)); }
        @Test void negInfLtPosInf()       { assertTrue(evalBool(Double.NEGATIVE_INFINITY, "<", Double.POSITIVE_INFINITY)); }
        @Test void infinityNotEqNegInf()  { assertFalse(evalBool(Double.POSITIVE_INFINITY, "==", Double.NEGATIVE_INFINITY)); }
        @Test void minValueAddMin()       { assertTrue(evalNum(Double.MIN_VALUE, "+", Double.MIN_VALUE) > 0); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TOSTRING
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("toString")
    class ToStringTests {
        @Test void containsClassName()    { assertTrue(numOp(1, "+", 2).toString().contains("BinaryOpNode")); }
        @Test void containsOperator()     { assertTrue(numOp(1, "+", 2).toString().contains("+")); }
        @Test void containsLeftValue()    { assertTrue(numOp(3, "*", 4).toString().contains("3")); }
        @Test void containsRightValue()   { assertTrue(numOp(3, "*", 4).toString().contains("4")); }
        @Test void differentOpsDistinct() { assertNotEquals(numOp(1,"+",2).toString(), numOp(1,"*",2).toString()); }
    }
}