package bloop.instruction;

import bloop.ast.*;
import bloop.exceptions.BloopRuntimeException;
import bloop.instruction.*;
import bloop.runtime.Environment;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Instructions — exhaustive")
class InstructionTest {

    private Environment env;

    @BeforeEach
    void setUp() { env = new Environment(); }

    // ══════════════════════════════════════════════════════════════════════
    // ASSIGN INSTRUCTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("AssignInstruction")
    class AssignInstructionTests {

        @Test void assignInteger()              { new AssignInstruction("x", new NumberNode(42)).execute(env); assertEquals(42.0, (double)env.get("x")); }
        @Test void assignFloat()                { new AssignInstruction("x", new NumberNode(3.14)).execute(env); assertEquals(3.14, (double)env.get("x"), 1e-9); }
        @Test void assignNegative()             { new AssignInstruction("x", new NumberNode(-5)).execute(env); assertEquals(-5.0, (double)env.get("x")); }
        @Test void assignZero()                 { new AssignInstruction("x", new NumberNode(0)).execute(env); assertEquals(0.0, (double)env.get("x")); }
        @Test void assignString()               { new AssignInstruction("msg", new StringNode("hello")).execute(env); assertEquals("hello", env.get("msg")); }
        @Test void assignEmptyString()          { new AssignInstruction("msg", new StringNode("")).execute(env); assertEquals("", env.get("msg")); }
        @Test void assignStringWithSpaces()     { new AssignInstruction("s", new StringNode("hi there")).execute(env); assertEquals("hi there", env.get("s")); }
        @Test void assignBooleanFromComparison(){
            Expression cmp = new BinaryOpNode(new NumberNode(1), "==", new NumberNode(1));
            new AssignInstruction("b", cmp).execute(env);
            assertEquals(true, env.get("b"));
        }
        @Test void overwriteNumber()            {
            new AssignInstruction("x", new NumberNode(1)).execute(env);
            new AssignInstruction("x", new NumberNode(2)).execute(env);
            assertEquals(2.0, (double)env.get("x"));
        }
        @Test void overwriteNumberWithString()  {
            new AssignInstruction("v", new NumberNode(1)).execute(env);
            new AssignInstruction("v", new StringNode("now")).execute(env);
            assertEquals("now", env.get("v"));
        }
        @Test void assignArithmeticResult()     {
            Expression e = new BinaryOpNode(new NumberNode(3), "+", new NumberNode(4));
            new AssignInstruction("r", e).execute(env);
            assertEquals(7.0, (double)env.get("r"));
        }
        @Test void assignFromAnotherVar()       {
            env.set("a", 10.0);
            new AssignInstruction("b", new VariableNode("a")).execute(env);
            assertEquals(10.0, (double)env.get("b"));
        }
        @Test void copyIsIndependent()          {
            env.set("a", 10.0);
            new AssignInstruction("b", new VariableNode("a")).execute(env);
            env.set("a", 99.0);
            assertEquals(10.0, (double)env.get("b")); // b should still be 10
        }
        @Test void assignMakesVarDefined()      {
            assertFalse(env.isDefined("x"));
            new AssignInstruction("x", new NumberNode(1)).execute(env);
            assertTrue(env.isDefined("x"));
        }
        @Test void multipleVarsIndependent()    {
            new AssignInstruction("a", new NumberNode(1)).execute(env);
            new AssignInstruction("b", new NumberNode(2)).execute(env);
            assertEquals(1.0, (double)env.get("a"));
            assertEquals(2.0, (double)env.get("b"));
        }
        @Test void assignFromUndefinedThrows()  {
            assertThrows(BloopRuntimeException.class,
                    () -> new AssignInstruction("x", new VariableNode("ghost")).execute(env));
        }
        @Test void assignChained()              {
            new AssignInstruction("a", new NumberNode(5)).execute(env);
            new AssignInstruction("b", new BinaryOpNode(new VariableNode("a"), "*", new NumberNode(2))).execute(env);
            new AssignInstruction("c", new BinaryOpNode(new VariableNode("b"), "+", new NumberNode(1))).execute(env);
            assertEquals(11.0, (double)env.get("c"));
        }
        @Test void underscoreVariableName()     {
            new AssignInstruction("my_var", new NumberNode(7)).execute(env);
            assertEquals(7.0, (double)env.get("my_var"));
        }
        @Test void longVariableName()           {
            String name = "a".repeat(50);
            new AssignInstruction(name, new NumberNode(3)).execute(env);
            assertEquals(3.0, (double)env.get(name));
        }

        @Test void toStringContainsVarName()    { assertTrue(new AssignInstruction("x", new NumberNode(1)).toString().contains("x")); }
        @Test void toStringContainsKeyword()    {
            String s = new AssignInstruction("x", new NumberNode(1)).toString();
            assertTrue(s.contains("AssignInstruction") || s.contains("put") || s.contains("into"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRINT INSTRUCTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("PrintInstruction")
    class PrintInstructionTests {

        private ByteArrayOutputStream out;
        private PrintStream original;

        @BeforeEach
        void capture()  { out = new ByteArrayOutputStream(); original = System.out; System.setOut(new PrintStream(out)); }
        @AfterEach
        void restore()  { System.setOut(original); }
        private String printed() { return out.toString().strip(); }

        @Test void printInteger()           { new PrintInstruction(new NumberNode(7)).execute(env);          assertEquals("7", printed()); }
        @Test void printZero()              { new PrintInstruction(new NumberNode(0)).execute(env);          assertEquals("0", printed()); }
        @Test void printNegativeInt()       { new PrintInstruction(new NumberNode(-3)).execute(env);         assertEquals("-3", printed()); }
        @Test void printFloat()             { new PrintInstruction(new NumberNode(3.14)).execute(env);       assertEquals("3.14", printed()); }
        @Test void printLargeInt()          { new PrintInstruction(new NumberNode(1_000_000)).execute(env);  assertEquals("1000000", printed()); }
        @Test void printString()            { new PrintInstruction(new StringNode("hello")).execute(env);    assertEquals("hello", printed()); }
        @Test void printEmptyString()       { new PrintInstruction(new StringNode("")).execute(env);         assertEquals("", printed()); }
        @Test void printStringWithSpaces()  { new PrintInstruction(new StringNode("hi there")).execute(env); assertEquals("hi there", printed()); }
        @Test void printVariable()          { env.set("x", 99.0); new PrintInstruction(new VariableNode("x")).execute(env); assertEquals("99", printed()); }
        @Test void printStringVar()         { env.set("s","bloop"); new PrintInstruction(new VariableNode("s")).execute(env); assertEquals("bloop", printed()); }
        @Test void printExpression()        {
            new PrintInstruction(new BinaryOpNode(new NumberNode(3), "*", new NumberNode(4))).execute(env);
            assertEquals("12", printed());
        }
        @Test void printBoolean()           {
            new PrintInstruction(new BinaryOpNode(new NumberNode(1), "<", new NumberNode(2))).execute(env);
            assertEquals("true", printed());
        }
        @Test void printFalse()             {
            new PrintInstruction(new BinaryOpNode(new NumberNode(5), "<", new NumberNode(2))).execute(env);
            assertEquals("false", printed());
        }
        @Test void intFormattedWithoutDecimal() {
            new PrintInstruction(new NumberNode(42.0)).execute(env);
            assertFalse(printed().contains("."));
        }
        @Test void floatFormattedWithDecimal()  {
            new PrintInstruction(new NumberNode(42.5)).execute(env);
            assertTrue(printed().contains("."));
        }
        @Test void addsNewline()            {
            new PrintInstruction(new NumberNode(1)).execute(env);
            assertTrue(out.toString().endsWith(System.lineSeparator()) || out.toString().endsWith("\n"));
        }
        @Test void twoPrintsOnTwoLines()    {
            new PrintInstruction(new NumberNode(1)).execute(env);
            new PrintInstruction(new NumberNode(2)).execute(env);
            String[] lines = out.toString().split("\\r?\\n");
            assertEquals(2, lines.length);
            assertEquals("1", lines[0]);
            assertEquals("2", lines[1]);
        }
        @Test void printMaxInt()            {
            new PrintInstruction(new NumberNode(Integer.MAX_VALUE)).execute(env);
            assertEquals(String.valueOf((long) Integer.MAX_VALUE), printed());
        }
        @Test void undefinedVarThrows()     {
            assertThrows(BloopRuntimeException.class,
                    () -> new PrintInstruction(new VariableNode("ghost")).execute(env));
        }
        @Test void toStringContainsPrint()  {
            assertTrue(new PrintInstruction(new NumberNode(1)).toString().toLowerCase().contains("print"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // IF INSTRUCTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("IfInstruction")
    class IfInstructionTests {

        private ByteArrayOutputStream out;
        private PrintStream original;

        @BeforeEach void capture()  { out = new ByteArrayOutputStream(); original = System.out; System.setOut(new PrintStream(out)); }
        @AfterEach  void restore()  { System.setOut(original); }
        private String printed()    { return out.toString().strip(); }

        private Expression TRUE  = new BinaryOpNode(new NumberNode(1), "==", new NumberNode(1));
        private Expression FALSE = new BinaryOpNode(new NumberNode(1), "==", new NumberNode(2));

        @Test void thenExecutesWhenTrue()       {
            new IfInstruction(TRUE, List.of(new PrintInstruction(new StringNode("yes")))).execute(env);
            assertEquals("yes", printed());
        }
        @Test void thenSkippedWhenFalse()       {
            new IfInstruction(FALSE, List.of(new PrintInstruction(new StringNode("no")))).execute(env);
            assertEquals("", printed());
        }
        @Test void elseExecutesWhenFalse()      {
            new IfInstruction(FALSE,
                    List.of(new PrintInstruction(new StringNode("then"))),
                    List.of(new PrintInstruction(new StringNode("else")))).execute(env);
            assertEquals("else", printed());
        }
        @Test void elseSkippedWhenTrue()        {
            new IfInstruction(TRUE,
                    List.of(new PrintInstruction(new StringNode("then"))),
                    List.of(new PrintInstruction(new StringNode("else")))).execute(env);
            assertEquals("then", printed());
        }
        @Test void nonBooleanConditionThrows()  {
            assertThrows(BloopRuntimeException.class,
                    () -> new IfInstruction(new NumberNode(1), List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void stringConditionThrows()      {
            assertThrows(BloopRuntimeException.class,
                    () -> new IfInstruction(new StringNode("true"), List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void multipleInThenBody()         {
            new IfInstruction(TRUE, List.of(
                    new AssignInstruction("a", new NumberNode(1)),
                    new AssignInstruction("b", new NumberNode(2)))).execute(env);
            assertEquals(1.0, (double)env.get("a"));
            assertEquals(2.0, (double)env.get("b"));
        }
        @Test void multipleInElseBody()         {
            new IfInstruction(FALSE,
                    List.of(new AssignInstruction("x", new NumberNode(0))),
                    List.of(
                            new AssignInstruction("a", new NumberNode(10)),
                            new AssignInstruction("b", new NumberNode(20)))).execute(env);
            assertEquals(10.0, (double)env.get("a"));
            assertEquals(20.0, (double)env.get("b"));
        }
        @Test void conditionFromVariable()      {
            env.set("score", 90.0);
            new IfInstruction(
                    new BinaryOpNode(new VariableNode("score"), ">=", new NumberNode(50)),
                    List.of(new PrintInstruction(new StringNode("pass")))).execute(env);
            assertEquals("pass", printed());
        }
        @Test void hasElseBlockFalseWhenNoElse(){
            assertFalse(new IfInstruction(TRUE, List.of(new PrintInstruction(new StringNode("x")))).hasElseBlock());
        }
        @Test void hasElseBlockTrueWithElse()   {
            assertTrue(new IfInstruction(TRUE,
                    List.of(new PrintInstruction(new StringNode("a"))),
                    List.of(new PrintInstruction(new StringNode("b")))).hasElseBlock());
        }
        @Test void emptyElseListMeansNoElse()   {
            assertFalse(new IfInstruction(TRUE, List.of(new PrintInstruction(new StringNode("x"))), List.of()).hasElseBlock());
        }
        @Test void nestedIfInThenBody()         {
            new IfInstruction(TRUE, List.of(
                    new IfInstruction(TRUE, List.of(new PrintInstruction(new StringNode("deep")))))).execute(env);
            assertEquals("deep", printed());
        }
        @Test void nestedIfFalseInThenBody()    {
            new IfInstruction(TRUE, List.of(
                    new IfInstruction(FALSE, List.of(new PrintInstruction(new StringNode("should not")))))).execute(env);
            assertEquals("", printed());
        }
        @Test void sideEffectsOnlyInExecutedBranch() {
            new IfInstruction(TRUE,
                    List.of(new AssignInstruction("x", new NumberNode(1))),
                    List.of(new AssignInstruction("y", new NumberNode(2)))).execute(env);
            assertTrue(env.isDefined("x"));
            assertFalse(env.isDefined("y"));
        }
        @Test void sideEffectsOnlyInElseBranch() {
            new IfInstruction(FALSE,
                    List.of(new AssignInstruction("x", new NumberNode(1))),
                    List.of(new AssignInstruction("y", new NumberNode(2)))).execute(env);
            assertFalse(env.isDefined("x"));
            assertTrue(env.isDefined("y"));
        }
        @Test void greaterThanCondition()       {
            new IfInstruction(new BinaryOpNode(new NumberNode(10), ">", new NumberNode(5)),
                    List.of(new PrintInstruction(new StringNode("gt")))).execute(env);
            assertEquals("gt", printed());
        }
        @Test void equalityStringCondition()    {
            env.set("s", "hi");
            new IfInstruction(
                    new BinaryOpNode(new VariableNode("s"), "==", new StringNode("hi")),
                    List.of(new PrintInstruction(new StringNode("match")))).execute(env);
            assertEquals("match", printed());
        }
        @Test void toStringContainsIfInstruction() {
            String s = new IfInstruction(TRUE, List.of(new PrintInstruction(new StringNode("x")))).toString();
            assertTrue(s.contains("IfInstruction"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // REPEAT INSTRUCTION
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("RepeatInstruction")
    class RepeatInstructionTests {

        @Test void repeatsNTimes()              {
            env.set("c", 0.0);
            Expression inc = new BinaryOpNode(new VariableNode("c"), "+", new NumberNode(1));
            new RepeatInstruction(new NumberNode(5), List.of(new AssignInstruction("c", inc))).execute(env);
            assertEquals(5.0, (double)env.get("c"));
        }
        @Test void zeroRepeatSkipsBody()        {
            env.set("x", 0.0);
            new RepeatInstruction(new NumberNode(0), List.of(new AssignInstruction("x", new NumberNode(99)))).execute(env);
            assertEquals(0.0, (double)env.get("x"));
        }
        @Test void repeatOnce()                 {
            env.set("flag", 0.0);
            new RepeatInstruction(new NumberNode(1), List.of(new AssignInstruction("flag", new NumberNode(1)))).execute(env);
            assertEquals(1.0, (double)env.get("flag"));
        }
        @Test void repeatFromVariable()         {
            env.set("n", 3.0); env.set("s", 0.0);
            Expression add = new BinaryOpNode(new VariableNode("s"), "+", new NumberNode(1));
            new RepeatInstruction(new VariableNode("n"), List.of(new AssignInstruction("s", add))).execute(env);
            assertEquals(3.0, (double)env.get("s"));
        }
        @Test void nonNumberCountThrows()       {
            assertThrows(BloopRuntimeException.class,
                    () -> new RepeatInstruction(new StringNode("five"), List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void negativeCountThrows()        {
            assertThrows(BloopRuntimeException.class,
                    () -> new RepeatInstruction(new NumberNode(-1), List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void fractionalCountThrows()      {
            assertThrows(BloopRuntimeException.class,
                    () -> new RepeatInstruction(new NumberNode(2.5), List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void tooLargeCountThrows()        {
            assertThrows(BloopRuntimeException.class,
                    () -> new RepeatInstruction(new NumberNode((double)Integer.MAX_VALUE + 1),
                            List.of(new PrintInstruction(new StringNode("x")))).execute(env));
        }
        @Test void multipleInstructionsPerIteration() {
            env.set("a", 0.0); env.set("b", 0.0);
            new RepeatInstruction(new NumberNode(3), List.of(
                    new AssignInstruction("a", new BinaryOpNode(new VariableNode("a"), "+", new NumberNode(1))),
                    new AssignInstruction("b", new BinaryOpNode(new VariableNode("b"), "+", new NumberNode(2))))).execute(env);
            assertEquals(3.0, (double)env.get("a"));
            assertEquals(6.0, (double)env.get("b"));
        }
        @Test void repeatAccumulatesMul()       {
            env.set("r", 1.0);
            new RepeatInstruction(new NumberNode(4), List.of(
                    new AssignInstruction("r", new BinaryOpNode(new VariableNode("r"), "*", new NumberNode(2))))).execute(env);
            assertEquals(16.0, (double)env.get("r")); // 2^4
        }
        @Test void repeatWithPrintProducesLines() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream orig = System.out;
            System.setOut(new PrintStream(out));
            try {
                new RepeatInstruction(new NumberNode(3), List.of(new PrintInstruction(new StringNode("hi")))).execute(env);
                String[] lines = out.toString().split("\\r?\\n");
                assertEquals(3, lines.length);
            } finally {
                System.setOut(orig);
            }
        }
        @Test void nestedRepeat()               {
            env.set("total", 0.0);
            Expression inc = new BinaryOpNode(new VariableNode("total"), "+", new NumberNode(1));
            RepeatInstruction inner = new RepeatInstruction(new NumberNode(3), List.of(new AssignInstruction("total", inc)));
            new RepeatInstruction(new NumberNode(3), List.of(inner)).execute(env);
            assertEquals(9.0, (double)env.get("total")); // 3*3
        }
        @Test void repeatIfInsideBody()         {
            env.set("count", 0.0);
            Expression cond = new BinaryOpNode(new NumberNode(1), "==", new NumberNode(1));
            Expression inc = new BinaryOpNode(new VariableNode("count"), "+", new NumberNode(1));
            IfInstruction ifInstr = new IfInstruction(cond, List.of(new AssignInstruction("count", inc)));
            new RepeatInstruction(new NumberNode(5), List.of(ifInstr)).execute(env);
            assertEquals(5.0, (double)env.get("count"));
        }
        @Test void errorMessageForNonNumber()   {
            RepeatInstruction r = new RepeatInstruction(new StringNode("bad"),
                    List.of(new PrintInstruction(new StringNode("x"))));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> r.execute(env));
            assertTrue(ex.getMessage().contains("number") || ex.getMessage().contains("Repeat"));
        }
        @Test void errorMessageForFractional()  {
            RepeatInstruction r = new RepeatInstruction(new NumberNode(1.5),
                    List.of(new PrintInstruction(new StringNode("x"))));
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> r.execute(env));
            assertTrue(ex.getMessage().contains("whole") || ex.getMessage().contains("non-negative"));
        }
        @Test void toStringContainsRepeat()     {
            String s = new RepeatInstruction(new NumberNode(3), List.of(new PrintInstruction(new StringNode("x")))).toString();
            assertTrue(s.contains("RepeatInstruction") || s.toLowerCase().contains("repeat"));
        }
    }
}