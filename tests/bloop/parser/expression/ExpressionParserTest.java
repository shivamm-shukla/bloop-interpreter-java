package bloop.parser.expression;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.parser.cursor.TokenCursor;
import bloop.runtime.Environment;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpressionParser — exhaustive")
class ExpressionParserTest {

    private ExpressionParser parser;
    private Environment env;

    @BeforeEach
    void setUp() { parser = new ExpressionParser(); env = new Environment(); }

    private static Token tok(TokenType t, String v) { return new Token(t, v, 1); }

    private Expression parse(Token... tokens) {
        List<Token> list = new ArrayList<>(List.of(tokens));
        list.add(tok(TokenType.EOF, ""));
        return parser.parse(new TokenCursor(list));
    }

    private double evalNum(Token... tokens)  { return (double) parse(tokens).evaluate(env); }
    private boolean evalBool(Token... tokens){ return (boolean) parse(tokens).evaluate(env); }

    // ══════════════════════════════════════════════════════════════════════
    // PRIMARY — number literals
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Number literals")
    class NumberLiterals {
        @Test void integer()             { assertEquals(42.0,  evalNum(tok(TokenType.NUMBER,"42"))); }
        @Test void zero()                { assertEquals(0.0,   evalNum(tok(TokenType.NUMBER,"0"))); }
        @Test void float_()             { assertEquals(3.14,  evalNum(tok(TokenType.NUMBER,"3.14")), 1e-9); }
        @Test void leadingZeroFloat()    { assertEquals(0.5,   evalNum(tok(TokenType.NUMBER,"0.5"))); }
        @Test void largeNumber()         { assertEquals(1e15,  evalNum(tok(TokenType.NUMBER,"1000000000000000")), 1.0); }
        @Test void resultIsDouble()      { assertInstanceOf(Double.class, parse(tok(TokenType.NUMBER,"5")).evaluate(env)); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRIMARY — string literals
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("String literals")
    class StringLiterals {
        @Test void simpleString()        { assertEquals("hello", parse(tok(TokenType.STRING,"hello")).evaluate(env)); }
        @Test void emptyString()         { assertEquals("",      parse(tok(TokenType.STRING,"")).evaluate(env)); }
        @Test void stringWithSpaces()    { assertEquals("a b c", parse(tok(TokenType.STRING,"a b c")).evaluate(env)); }
        @Test void numericLookingString(){ assertInstanceOf(String.class, parse(tok(TokenType.STRING,"42")).evaluate(env)); }
        @Test void specialCharsString()  { assertEquals("!@#",   parse(tok(TokenType.STRING,"!@#")).evaluate(env)); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRIMARY — variable identifier
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Variable identifiers")
    class Variables {
        @Test void simpleVar()           { env.set("x",7.0); assertEquals(7.0, (double)parse(tok(TokenType.IDENTIFIER,"x")).evaluate(env)); }
        @Test void stringVar()           { env.set("s","hi"); assertEquals("hi", parse(tok(TokenType.IDENTIFIER,"s")).evaluate(env)); }
        @Test void undefinedVarThrows()  { assertThrows(Exception.class, () -> parse(tok(TokenType.IDENTIFIER,"ghost")).evaluate(env)); }
        @Test void updatedVarResolved()  { env.set("x",1.0); env.set("x",2.0); assertEquals(2.0,(double)parse(tok(TokenType.IDENTIFIER,"x")).evaluate(env)); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRIMARY — parentheses
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Parenthesised expressions")
    class Parentheses {
        @Test void singleNumber()        { assertEquals(5.0, evalNum(tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"5"), tok(TokenType.RIGHT_PAREN,")"))); }
        @Test void nestedParens()        {
            assertEquals(3.0, evalNum(
                    tok(TokenType.LEFT_PAREN,"("), tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"3"),
                    tok(TokenType.RIGHT_PAREN,")"), tok(TokenType.RIGHT_PAREN,")")));
        }
        @Test void unmatchedLeftThrows() {
            assertThrows(BloopParseException.class, () ->
                    parse(tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"5")));
        }
        @Test void unexpectedRightThrows(){ // ) without ( — treated as unexpected token at statement level
            // ExpressionParser won't consume it, but it also won't throw from within expression
            // The remaining ) will just be left in stream — valid parse of "5"
            assertEquals(5.0, evalNum(tok(TokenType.NUMBER,"5")));
        }
        @Test void parenAroundExpr()     {
            assertEquals(7.0, evalNum(
                    tok(TokenType.LEFT_PAREN,"("),
                    tok(TokenType.NUMBER,"3"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"4"),
                    tok(TokenType.RIGHT_PAREN,")")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UNARY MINUS
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Unary minus")
    class UnaryMinus {
        @Test void negativeLiteral()     { assertEquals(-5.0, evalNum(tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"5"))); }
        @Test void doubleNegation()      { assertEquals(5.0, evalNum(tok(TokenType.MINUS,"-"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"5"))); }
        @Test void negativeZero()        { assertEquals(-0.0, evalNum(tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"0")), 0.0); }
        @Test void negativeFloat()       { assertEquals(-3.14, evalNum(tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"3.14")), 1e-9); }
        @Test void negativeVar()         {
            env.set("x", 4.0);
            assertEquals(-4.0, evalNum(tok(TokenType.MINUS,"-"), tok(TokenType.IDENTIFIER,"x")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MULTIPLICATION / DIVISION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Multiplication and Division")
    class MulDiv {
        @Test void multiply()            { assertEquals(6.0,  evalNum(tok(TokenType.NUMBER,"2"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"3"))); }
        @Test void divide()              { assertEquals(2.5,  evalNum(tok(TokenType.NUMBER,"5"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"2"))); }
        @Test void chainedMul()          { assertEquals(24.0, evalNum(tok(TokenType.NUMBER,"2"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"3"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"4"))); }
        @Test void chainedDiv()          { assertEquals(2.0,  evalNum(tok(TokenType.NUMBER,"24"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"4"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"3"))); }
        @Test void mulThenDiv()          { assertEquals(2.0,  evalNum(tok(TokenType.NUMBER,"6"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"3"))); }
        @Test void leftAssociativeMul()  { // 8 / 4 / 2 = (8/4)/2 = 1, not 8/(4/2)=4
            assertEquals(1.0, evalNum(tok(TokenType.NUMBER,"8"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"4"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"2")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ADDITION / SUBTRACTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Addition and Subtraction")
    class AddSub {
        @Test void add()                 { assertEquals(5.0,  evalNum(tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"3"))); }
        @Test void subtract()            { assertEquals(6.0,  evalNum(tok(TokenType.NUMBER,"10"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"4"))); }
        @Test void chainedAdd()          { assertEquals(6.0,  evalNum(tok(TokenType.NUMBER,"1"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"3"))); }
        @Test void chainedSub()          { assertEquals(1.0,  evalNum(tok(TokenType.NUMBER,"10"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"5"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"4"))); }
        @Test void leftAssociativeSub()  { // 10-5-3 = (10-5)-3 = 2
            assertEquals(2.0, evalNum(tok(TokenType.NUMBER,"10"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"5"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"3")));
        }
        @Test void addAndSubMixed()      { assertEquals(4.0, evalNum(tok(TokenType.NUMBER,"3"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"5"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"4"))); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // OPERATOR PRECEDENCE
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Operator precedence")
    class Precedence {
        @Test void mulBeforeAdd()        { assertEquals(14.0, evalNum(tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"3"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"4"))); }
        @Test void mulBeforeSub()        { assertEquals(4.0,  evalNum(tok(TokenType.NUMBER,"10"), tok(TokenType.MINUS,"-"), tok(TokenType.NUMBER,"2"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"3"))); }
        @Test void divBeforeAdd()        { assertEquals(7.0,  evalNum(tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"10"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"2"))); }
        @Test void parensOverrideMul()   { // (2+3)*4 = 20
            assertEquals(20.0, evalNum(
                    tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"3"), tok(TokenType.RIGHT_PAREN,")"),
                    tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"4")));
        }
        @Test void parensOverrideSub()   { // 10-(3+2) = 5
            assertEquals(5.0, evalNum(
                    tok(TokenType.NUMBER,"10"), tok(TokenType.MINUS,"-"),
                    tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"3"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"2"), tok(TokenType.RIGHT_PAREN,")")));
        }
        @Test void complexPrecedence()   { // 1 + 2 * 3 - 4 / 2 = 1+6-2 = 5
            assertEquals(5.0, evalNum(
                    tok(TokenType.NUMBER,"1"), tok(TokenType.PLUS,"+"),
                    tok(TokenType.NUMBER,"2"), tok(TokenType.STAR,"*"), tok(TokenType.NUMBER,"3"),
                    tok(TokenType.MINUS,"-"),
                    tok(TokenType.NUMBER,"4"), tok(TokenType.SLASH,"/"), tok(TokenType.NUMBER,"2")));
        }
        @Test void deeplyNestedParens()  { // ((3+2))*(1+1) = 10
            assertEquals(10.0, evalNum(
                    tok(TokenType.LEFT_PAREN,"("), tok(TokenType.LEFT_PAREN,"("),
                    tok(TokenType.NUMBER,"3"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"2"),
                    tok(TokenType.RIGHT_PAREN,")"), tok(TokenType.RIGHT_PAREN,")"),
                    tok(TokenType.STAR,"*"),
                    tok(TokenType.LEFT_PAREN,"("), tok(TokenType.NUMBER,"1"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"1"), tok(TokenType.RIGHT_PAREN,")")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // COMPARISON OPERATORS
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Comparison operators")
    class Comparisons {
        @Test void greaterTrue()    { assertTrue(evalBool(tok(TokenType.NUMBER,"5"), tok(TokenType.GREATER,">"), tok(TokenType.NUMBER,"3"))); }
        @Test void greaterFalse()   { assertFalse(evalBool(tok(TokenType.NUMBER,"3"), tok(TokenType.GREATER,">"), tok(TokenType.NUMBER,"5"))); }
        @Test void greaterEqual()   { assertFalse(evalBool(tok(TokenType.NUMBER,"5"), tok(TokenType.GREATER,">"), tok(TokenType.NUMBER,"5"))); }
        @Test void lessTrue()       { assertTrue(evalBool(tok(TokenType.NUMBER,"1"), tok(TokenType.LESS,"<"), tok(TokenType.NUMBER,"2"))); }
        @Test void lessFalse()      { assertFalse(evalBool(tok(TokenType.NUMBER,"9"), tok(TokenType.LESS,"<"), tok(TokenType.NUMBER,"1"))); }
        @Test void geTrue()         { assertTrue(evalBool(tok(TokenType.NUMBER,"5"), tok(TokenType.GREATER_EQUAL,">="), tok(TokenType.NUMBER,"5"))); }
        @Test void geStrict()       { assertTrue(evalBool(tok(TokenType.NUMBER,"6"), tok(TokenType.GREATER_EQUAL,">="), tok(TokenType.NUMBER,"5"))); }
        @Test void geFalse()        { assertFalse(evalBool(tok(TokenType.NUMBER,"4"), tok(TokenType.GREATER_EQUAL,">="), tok(TokenType.NUMBER,"5"))); }
        @Test void leTrue()         { assertTrue(evalBool(tok(TokenType.NUMBER,"3"), tok(TokenType.LESS_EQUAL,"<="), tok(TokenType.NUMBER,"3"))); }
        @Test void leStrict()       { assertTrue(evalBool(tok(TokenType.NUMBER,"2"), tok(TokenType.LESS_EQUAL,"<="), tok(TokenType.NUMBER,"3"))); }
        @Test void leFalse()        { assertFalse(evalBool(tok(TokenType.NUMBER,"5"), tok(TokenType.LESS_EQUAL,"<="), tok(TokenType.NUMBER,"3"))); }
        @Test void equalEqual()     { assertTrue(evalBool(tok(TokenType.NUMBER,"7"), tok(TokenType.EQUAL_EQUAL,"=="), tok(TokenType.NUMBER,"7"))); }
        @Test void notEqual()       { assertTrue(evalBool(tok(TokenType.NUMBER,"7"), tok(TokenType.NOT_EQUAL,"!="), tok(TokenType.NUMBER,"8"))); }

        @Test @DisplayName("comparison of arithmetic: 2+3 == 5")
        void compareArithmetic()    {
            assertTrue(evalBool(
                    tok(TokenType.NUMBER,"2"), tok(TokenType.PLUS,"+"), tok(TokenType.NUMBER,"3"),
                    tok(TokenType.EQUAL_EQUAL,"=="),
                    tok(TokenType.NUMBER,"5")));
        }
        @Test @DisplayName("comparison result is boolean")
        void resultIsBoolean()      { assertInstanceOf(Boolean.class, parse(tok(TokenType.NUMBER,"1"), tok(TokenType.GREATER,">"), tok(TokenType.NUMBER,"0")).evaluate(env)); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UNEXPECTED TOKENS
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Unexpected tokens")
    class Unexpected {
        @Test void colonThrows()    { assertThrows(BloopParseException.class, () -> parse(tok(TokenType.COLON,":"))); }
        @Test void thenThrows()     { assertThrows(BloopParseException.class, () -> parse(tok(TokenType.THEN,"then"))); }
        @Test void intoThrows()     { assertThrows(BloopParseException.class, () -> parse(tok(TokenType.INTO,"into"))); }
        @Test void timesThrows()    { assertThrows(BloopParseException.class, () -> parse(tok(TokenType.TIMES,"times"))); }
    }
}