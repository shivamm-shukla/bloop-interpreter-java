package bloop.parser;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.instructions.*;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ParserTest {

    // ════════════════════════════════════════════
    //  Helper — Token list banane ke liye
    // ════════════════════════════════════════════

    private Token token(TokenType type, String value) {
        return new Token(type, value, 1);
    }

    private Token token(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    private List<Instruction> parse(Token... tokens) {
        // EOF hamesha end mein add karo
        Token[] withEof = new Token[tokens.length + 1];
        System.arraycopy(tokens, 0, withEof, 0, tokens.length);
        withEof[tokens.length] = token(TokenType.EOF, "");
        return new Parser(List.of(withEof)).parse();
    }

    // ════════════════════════════════════════════
    //  Constructor Tests
    // ════════════════════════════════════════════

    @Test
    void constructor_nullTokenList_throwsException() {
        assertThrows(BloopParseException.class, () -> new Parser(null));
    }

    @Test
    void constructor_emptyTokenList_throwsException() {
        assertThrows(BloopParseException.class, () -> new Parser(List.of()));
    }

    // ════════════════════════════════════════════
    //  Empty Program
    // ════════════════════════════════════════════

    @Test
    void parse_emptyProgram_returnsEmptyList() {
        List<Instruction> instructions = parse();
        assertEquals(0, instructions.size());
    }

    @Test
    void parse_onlyNewlines_returnsEmptyList() {
        List<Instruction> instructions = parse(
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.NEWLINE, "\n")
        );
        assertEquals(0, instructions.size());
    }

    // ════════════════════════════════════════════
    //  Put Instruction Tests
    // ════════════════════════════════════════════

    @Test
    void parse_putNumber_returnsAssignInstruction() {
        // put 10 into x
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "10"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    void parse_putString_returnsAssignInstruction() {
        // put "Sitare" into name
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.STRING,     "Sitare"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "name"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    void parse_putExpression_returnsAssignInstruction() {
        // put x + y * 2 into result
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.PLUS,       "+"),
                token(TokenType.IDENTIFIER, "y"),
                token(TokenType.STAR,       "*"),
                token(TokenType.NUMBER,     "2"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "result"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    void parse_putMissingInto_throwsException() {
        // put 10 x  ← 'into' missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "10"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    @Test
    void parse_putMissingVariableName_throwsException() {
        // put 10 into  ← variable missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.PUT,     "put"),
                token(TokenType.NUMBER,  "10"),
                token(TokenType.INTO,    "into"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_putMissingExpression_throwsException() {
        // put into x  ← expression missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    // ════════════════════════════════════════════
    //  Print Instruction Tests
    // ════════════════════════════════════════════

    @Test
    void parse_printNumber_returnsPrintInstruction() {
        // print 42
        List<Instruction> instructions = parse(
                token(TokenType.PRINT,   "print"),
                token(TokenType.NUMBER,  "42"),
                token(TokenType.NEWLINE, "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.get(0));
    }

    @Test
    void parse_printString_returnsPrintInstruction() {
        // print "hello"
        List<Instruction> instructions = parse(
                token(TokenType.PRINT,   "print"),
                token(TokenType.STRING,  "hello"),
                token(TokenType.NEWLINE, "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.get(0));
    }

    @Test
    void parse_printVariable_returnsPrintInstruction() {
        // print result
        List<Instruction> instructions = parse(
                token(TokenType.PRINT,      "print"),
                token(TokenType.IDENTIFIER, "result"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(PrintInstruction.class, instructions.get(0));
    }

    @Test
    void parse_printMissingExpression_throwsException() {
        // print  ← expression missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.PRINT,   "print"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    // ════════════════════════════════════════════
    //  If Instruction Tests
    // ════════════════════════════════════════════

    @Test
    void parse_ifInstruction_returnsIfInstruction() {
        // if score > 50 then:
        //     print "Pass"
        List<Instruction> instructions = parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "score"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "50"),
                token(TokenType.THEN,       "then"),
                token(TokenType.COLON,      ":"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.INDENT,     ""),
                token(TokenType.PRINT,      "print"),
                token(TokenType.STRING,     "Pass"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.DEDENT,     "")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.get(0));
    }

    @Test
    void parse_ifMissingThen_throwsException() {
        // if score > 50 :  ← 'then' missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "score"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "50"),
                token(TokenType.COLON,      ":"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    @Test
    void parse_ifMissingColon_throwsException() {
        // if score > 50 then  ← ':' missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "score"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "50"),
                token(TokenType.THEN,       "then"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    @Test
    void parse_ifEmptyBody_throwsException() {
        // if score > 50 then:
        //     ← empty body
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "score"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "50"),
                token(TokenType.THEN,       "then"),
                token(TokenType.COLON,      ":"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.INDENT,     ""),
                token(TokenType.DEDENT,     "")
        ));
    }

    @Test
    void parse_ifMissingIndent_throwsException() {
        // if score > 50 then:
        // print "Pass"  ← no indent
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "score"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "50"),
                token(TokenType.THEN,       "then"),
                token(TokenType.COLON,      ":"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.PRINT,      "print"),
                token(TokenType.STRING,     "Pass"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    @Test
    void parse_ifWithMultipleBodyInstructions() {
        // if x > 0 then:
        //     print "positive"
        //     print "yes"
        List<Instruction> instructions = parse(
                token(TokenType.IF,         "if"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.GREATER,    ">"),
                token(TokenType.NUMBER,     "0"),
                token(TokenType.THEN,       "then"),
                token(TokenType.COLON,      ":"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.INDENT,     ""),
                token(TokenType.PRINT,      "print"),
                token(TokenType.STRING,     "positive"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.PRINT,      "print"),
                token(TokenType.STRING,     "yes"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.DEDENT,     "")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(IfInstruction.class, instructions.get(0));
    }

    // ════════════════════════════════════════════
    //  Repeat Instruction Tests
    // ════════════════════════════════════════════

    @Test
    void parse_repeatInstruction_returnsRepeatInstruction() {
        // repeat 3 times:
        //     print "hello"
        List<Instruction> instructions = parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "3"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.INDENT,  ""),
                token(TokenType.PRINT,   "print"),
                token(TokenType.STRING,  "hello"),
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.DEDENT,  "")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(RepeatInstruction.class, instructions.get(0));
    }

    @Test
    void parse_repeatMissingCount_throwsException() {
        // repeat times:  ← count missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_repeatMissingTimes_throwsException() {
        // repeat 3 :  ← 'times' missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "3"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_repeatMissingColon_throwsException() {
        // repeat 3 times  ← ':' missing
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "3"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_repeatNegativeCount_throwsException() {
        // repeat -3 times:
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "-3"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_repeatDecimalCount_throwsException() {
        // repeat 3.5 times:
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "3.5"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_repeatEmptyBody_throwsException() {
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "3"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.INDENT,  ""),
                token(TokenType.DEDENT,  "")
        ));
    }

    @Test
    void parse_repeatZeroTimes_returnsRepeatInstruction() {
        // repeat 0 times: ← valid, just runs 0 times
        List<Instruction> instructions = parse(
                token(TokenType.REPEAT,  "repeat"),
                token(TokenType.NUMBER,  "0"),
                token(TokenType.TIMES,   "times"),
                token(TokenType.COLON,   ":"),
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.INDENT,  ""),
                token(TokenType.PRINT,   "print"),
                token(TokenType.STRING,  "hello"),
                token(TokenType.NEWLINE, "\n"),
                token(TokenType.DEDENT,  "")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(RepeatInstruction.class, instructions.get(0));
    }

    // ════════════════════════════════════════════
    //  Expression Precedence Tests
    // ════════════════════════════════════════════

    @Test
    void parse_expressionPrecedence_multiplyBeforeAdd() {
        // put x + y * 2 into result
        // Should parse as x + (y * 2), not (x + y) * 2
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.PLUS,       "+"),
                token(TokenType.IDENTIFIER, "y"),
                token(TokenType.STAR,       "*"),
                token(TokenType.NUMBER,     "2"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "result"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(1, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
    }

    @Test
    void parse_allComparisonOperators() {
        // > < >= <= == != — sab valid hone chahiye
        TokenType[] operators = {
                TokenType.GREATER, TokenType.LESS,
                TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
                TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL
        };
        String[] symbols = {">", "<", ">=", "<=", "==", "!="};

        for (int i = 0; i < operators.length; i++) {
            final int index = i;
            assertDoesNotThrow(() -> parse(
                    token(TokenType.IF,         "if"),
                    token(TokenType.IDENTIFIER, "x"),
                    token(operators[index],     symbols[index]),
                    token(TokenType.NUMBER,     "5"),
                    token(TokenType.THEN,       "then"),
                    token(TokenType.COLON,      ":"),
                    token(TokenType.NEWLINE,    "\n"),
                    token(TokenType.INDENT,     ""),
                    token(TokenType.PRINT,      "print"),
                    token(TokenType.STRING,     "yes"),
                    token(TokenType.NEWLINE,    "\n"),
                    token(TokenType.DEDENT,     "")
            ));
        }
    }

    // ════════════════════════════════════════════
    //  Multiple Instructions Tests
    // ════════════════════════════════════════════

    @Test
    void parse_multipleInstructions_returnsAll() {
        // put 10 into x
        // put 20 into y
        // print x
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "10"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "20"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "y"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.PRINT,      "print"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(3, instructions.size());
        assertInstanceOf(AssignInstruction.class, instructions.get(0));
        assertInstanceOf(AssignInstruction.class, instructions.get(1));
        assertInstanceOf(PrintInstruction.class,  instructions.get(2));
    }

    @Test
    void parse_blankLinesBetweenInstructions_ignored() {
        // put 10 into x
        //
        // print x
        List<Instruction> instructions = parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "10"),
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.NEWLINE,    "\n"),
                token(TokenType.PRINT,      "print"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        );
        assertEquals(2, instructions.size());
    }

    // ════════════════════════════════════════════
    //  Unknown Token Tests
    // ════════════════════════════════════════════

    @Test
    void parse_unknownToken_throwsException() {
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.IDENTIFIER, "unknownKeyword"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }

    @Test
    void parse_invalidNumberFormat_throwsException() {
        assertThrows(BloopParseException.class, () -> parse(
                token(TokenType.PUT,        "put"),
                token(TokenType.NUMBER,     "abc"),  // invalid number
                token(TokenType.INTO,       "into"),
                token(TokenType.IDENTIFIER, "x"),
                token(TokenType.NEWLINE,    "\n")
        ));
    }
}