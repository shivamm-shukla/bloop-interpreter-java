package bloop.parser;

import bloop.exceptions.BloopParseException;
import bloop.instructions.*;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    // ════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════

    private Token createToken(TokenType type, String value) {
        return new Token(type, value, 1);
    }

    private Token createToken(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    private List<Instruction> parseTokens(Token... tokens) {
        Token[] withEof = new Token[tokens.length + 1];
        System.arraycopy(tokens, 0, withEof, 0, tokens.length);
        withEof[tokens.length] = createToken(TokenType.EOF, "");
        return new Parser(List.of(withEof)).parse();
    }

    // ════════════════════════════════════════════
    // Constructor Tests (2)
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
    // Empty Program (2)
    // ════════════════════════════════════════════

    @Test
    void parse_emptyProgram_returnsEmptyList() {
        assertEquals(0, parseTokens().size());
    }

    @Test
    void parse_onlyNewlines_returnsEmptyList() {
        assertEquals(0, parseTokens(
                createToken(TokenType.NEWLINE, "\n"),
                createToken(TokenType.NEWLINE, "\n")
        ).size());
    }

    // ════════════════════════════════════════════
    // Put (6)
    // ════════════════════════════════════════════

    @Test
    void parse_putNumber_returnsAssignInstruction() {
        List<Instruction> list = parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.NUMBER, "10"),
                createToken(TokenType.INTO, "into"),
                createToken(TokenType.IDENTIFIER, "x"),
                createToken(TokenType.NEWLINE, "\n")
        );
        assertEquals(1, list.size());
        assertInstanceOf(AssignInstruction.class, list.get(0));
    }

    @Test
    void parse_putString_returnsAssignInstruction() {
        List<Instruction> list = parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.STRING, "Sitare"),
                createToken(TokenType.INTO, "into"),
                createToken(TokenType.IDENTIFIER, "name"),
                createToken(TokenType.NEWLINE, "\n")
        );
        assertInstanceOf(AssignInstruction.class, list.get(0));
    }

    @Test
    void parse_putExpression_returnsAssignInstruction() {
        List<Instruction> list = parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.IDENTIFIER, "x"),
                createToken(TokenType.PLUS, "+"),
                createToken(TokenType.IDENTIFIER, "y"),
                createToken(TokenType.STAR, "*"),
                createToken(TokenType.NUMBER, "2"),
                createToken(TokenType.INTO, "into"),
                createToken(TokenType.IDENTIFIER, "result"),
                createToken(TokenType.NEWLINE, "\n")
        );
        assertInstanceOf(AssignInstruction.class, list.get(0));
    }

    @Test
    void parse_putMissingInto_throwsException() {
        assertThrows(BloopParseException.class, () -> parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.NUMBER, "10"),
                createToken(TokenType.IDENTIFIER, "x"),
                createToken(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_putMissingVariableName_throwsException() {
        assertThrows(BloopParseException.class, () -> parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.NUMBER, "10"),
                createToken(TokenType.INTO, "into"),
                createToken(TokenType.NEWLINE, "\n")
        ));
    }

    @Test
    void parse_putMissingExpression_throwsException() {
        assertThrows(BloopParseException.class, () -> parseTokens(
                createToken(TokenType.PUT, "put"),
                createToken(TokenType.INTO, "into"),
                createToken(TokenType.IDENTIFIER, "x"),
                createToken(TokenType.NEWLINE, "\n")
        ));
    }

    // ════════════════════════════════════════════
    // Print (4)
    // ════════════════════════════════════════════

    @Test
    void parse_printNumber_returnsPrintInstruction() {
        assertInstanceOf(PrintInstruction.class, parseTokens(
                createToken(TokenType.PRINT, "print"),
                createToken(TokenType.NUMBER, "42"),
                createToken(TokenType.NEWLINE, "\n")
        ).get(0));
    }

    @Test
    void parse_printString_returnsPrintInstruction() {
        assertInstanceOf(PrintInstruction.class, parseTokens(
                createToken(TokenType.PRINT, "print"),
                createToken(TokenType.STRING, "hello"),
                createToken(TokenType.NEWLINE, "\n")
        ).get(0));
    }

    @Test
    void parse_printVariable_returnsPrintInstruction() {
        assertInstanceOf(PrintInstruction.class, parseTokens(
                createToken(TokenType.PRINT, "print"),
                createToken(TokenType.IDENTIFIER, "result"),
                createToken(TokenType.NEWLINE, "\n")
        ).get(0));
    }

    @Test
    void parse_printMissingExpression_throwsException() {
        assertThrows(BloopParseException.class, () -> parseTokens(
                createToken(TokenType.PRINT, "print"),
                createToken(TokenType.NEWLINE, "\n")
        ));
    }

    // ════════════════════════════════════════════
    // IF (6)
    // ════════════════════════════════════════════

    @Test
    void parse_ifInstruction_returnsIfInstruction() {
        assertInstanceOf(IfInstruction.class, parseTokens(
                createToken(TokenType.IF, "if"),
                createToken(TokenType.IDENTIFIER, "score"),
                createToken(TokenType.GREATER, ">"),
                createToken(TokenType.NUMBER, "50"),
                createToken(TokenType.THEN, "then"),
                createToken(TokenType.COLON, ":"),
                createToken(TokenType.NEWLINE, "\n"),
                createToken(TokenType.INDENT, ""),
                createToken(TokenType.PRINT, "print"),
                createToken(TokenType.STRING, "Pass"),
                createToken(TokenType.NEWLINE, "\n"),
                createToken(TokenType.DEDENT, "")
        ).get(0));
    }

    @Test void parse_ifMissingThen_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.IF,"if"),createToken(TokenType.IDENTIFIER,"score"),createToken(TokenType.GREATER,">"),createToken(TokenType.NUMBER,"50"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_ifMissingColon_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.IF,"if"),createToken(TokenType.IDENTIFIER,"score"),createToken(TokenType.GREATER,">"),createToken(TokenType.NUMBER,"50"),createToken(TokenType.THEN,"then"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_ifEmptyBody_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.IF,"if"),createToken(TokenType.IDENTIFIER,"score"),createToken(TokenType.GREATER,">"),createToken(TokenType.NUMBER,"50"),createToken(TokenType.THEN,"then"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.INDENT,""),createToken(TokenType.DEDENT,"")));}

    @Test void parse_ifMissingIndent_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.IF,"if"),createToken(TokenType.IDENTIFIER,"score"),createToken(TokenType.GREATER,">"),createToken(TokenType.NUMBER,"50"),createToken(TokenType.THEN,"then"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.PRINT,"print"),createToken(TokenType.STRING,"Pass"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_ifWithMultipleBodyInstructions(){assertInstanceOf(IfInstruction.class,parseTokens(createToken(TokenType.IF,"if"),createToken(TokenType.IDENTIFIER,"x"),createToken(TokenType.GREATER,">"),createToken(TokenType.NUMBER,"0"),createToken(TokenType.THEN,"then"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.INDENT,""),createToken(TokenType.PRINT,"print"),createToken(TokenType.STRING,"positive"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.PRINT,"print"),createToken(TokenType.STRING,"yes"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.DEDENT,"")).get(0));}

    // ════════════════════════════════════════════
    // Repeat (7)
    // ════════════════════════════════════════════

    @Test
    void parse_repeatInstruction_returnsRepeatInstruction() {
        assertInstanceOf(RepeatInstruction.class, parseTokens(
                createToken(TokenType.REPEAT,"repeat"),
                createToken(TokenType.NUMBER,"3"),
                createToken(TokenType.TIMES,"times"),
                createToken(TokenType.COLON,":"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.INDENT,""),
                createToken(TokenType.PRINT,"print"),
                createToken(TokenType.STRING,"hello"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.DEDENT,"")
        ).get(0));
    }

    @Test void parse_repeatMissingCount_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.TIMES,"times"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_repeatMissingTimes_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"3"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_repeatMissingColon_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"3"),createToken(TokenType.TIMES,"times"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_repeatNegativeCount_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"-3"),createToken(TokenType.TIMES,"times"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_repeatDecimalCount_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"3.5"),createToken(TokenType.TIMES,"times"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n")));}

    @Test void parse_repeatEmptyBody_throwsException(){assertThrows(BloopParseException.class,()->parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"3"),createToken(TokenType.TIMES,"times"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.INDENT,""),createToken(TokenType.DEDENT,"")));}

    @Test void parse_repeatZeroTimes_returnsRepeatInstruction(){assertInstanceOf(RepeatInstruction.class,parseTokens(createToken(TokenType.REPEAT,"repeat"),createToken(TokenType.NUMBER,"0"),createToken(TokenType.TIMES,"times"),createToken(TokenType.COLON,":"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.INDENT,""),createToken(TokenType.PRINT,"print"),createToken(TokenType.STRING,"hello"),createToken(TokenType.NEWLINE,"\n"),createToken(TokenType.DEDENT,"")).get(0));}

    // ════════════════════════════════════════════
    // Expression + Others
    // ════════════════════════════════════════════

    @Test
    void parse_expressionPrecedence_multiplyBeforeAdd() {
        assertInstanceOf(AssignInstruction.class, parseTokens(
                createToken(TokenType.PUT,"put"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.PLUS,"+"),
                createToken(TokenType.IDENTIFIER,"y"),
                createToken(TokenType.STAR,"*"),
                createToken(TokenType.NUMBER,"2"),
                createToken(TokenType.INTO,"into"),
                createToken(TokenType.IDENTIFIER,"result"),
                createToken(TokenType.NEWLINE,"\n")
        ).get(0));
    }

    @Test
    void parse_allComparisonOperators() {
        TokenType[] ops={TokenType.GREATER,TokenType.LESS,TokenType.GREATER_EQUAL,TokenType.LESS_EQUAL,TokenType.EQUAL_EQUAL,TokenType.NOT_EQUAL};
        String[] sym={">","<",">=","<=","==","!="};

        for(int i=0;i<ops.length;i++){
            int idx=i;
            assertDoesNotThrow(()->parseTokens(
                    createToken(TokenType.IF,"if"),
                    createToken(TokenType.IDENTIFIER,"x"),
                    createToken(ops[idx],sym[idx]),
                    createToken(TokenType.NUMBER,"5"),
                    createToken(TokenType.THEN,"then"),
                    createToken(TokenType.COLON,":"),
                    createToken(TokenType.NEWLINE,"\n"),
                    createToken(TokenType.INDENT,""),
                    createToken(TokenType.PRINT,"print"),
                    createToken(TokenType.STRING,"yes"),
                    createToken(TokenType.NEWLINE,"\n"),
                    createToken(TokenType.DEDENT,"")
            ));
        }
    }

    @Test
    void parse_multipleInstructions_returnsAll() {
        List<Instruction> list=parseTokens(
                createToken(TokenType.PUT,"put"),
                createToken(TokenType.NUMBER,"10"),
                createToken(TokenType.INTO,"into"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.PUT,"put"),
                createToken(TokenType.NUMBER,"20"),
                createToken(TokenType.INTO,"into"),
                createToken(TokenType.IDENTIFIER,"y"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.PRINT,"print"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.NEWLINE,"\n")
        );
        assertEquals(3,list.size());
    }

    @Test
    void parse_blankLinesBetweenInstructions_ignored() {
        assertEquals(2,parseTokens(
                createToken(TokenType.PUT,"put"),
                createToken(TokenType.NUMBER,"10"),
                createToken(TokenType.INTO,"into"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.NEWLINE,"\n"),
                createToken(TokenType.PRINT,"print"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.NEWLINE,"\n")
        ).size());
    }

    @Test
    void parse_unknownToken_throwsException() {
        assertThrows(BloopParseException.class,()->parseTokens(
                createToken(TokenType.IDENTIFIER,"unknownKeyword"),
                createToken(TokenType.NEWLINE,"\n")
        ));
    }

    @Test
    void parse_invalidNumberFormat_throwsException() {
        assertThrows(BloopParseException.class,()->parseTokens(
                createToken(TokenType.PUT,"put"),
                createToken(TokenType.NUMBER,"abc"),
                createToken(TokenType.INTO,"into"),
                createToken(TokenType.IDENTIFIER,"x"),
                createToken(TokenType.NEWLINE,"\n")
        ));
    }
}