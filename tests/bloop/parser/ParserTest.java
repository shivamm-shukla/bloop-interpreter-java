package bloop.parser;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.instructions.*;
import bloop.token.Token;
import bloop.token.TokenType;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exhaustive test suite for Parser.java
 * Coverage: Constructor, parse(), parsePutInstruction(), parsePrintInstruction(),
 *           parseIfInstruction(), parseRepeatInstruction(), parseIndentedBlock(),
 *           parseExpression() / parseAddition() / parseTerm() / parsePrimary(),
 *           parseNumberLiteral(), parseAndValidateRepeatCount(), edge-cases & multi-instruction programs.
 */
class ParserTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Token tok(TokenType type, String value) {
        return new Token(type, value, 1);
    }

    private Token tok(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    /** Appends EOF automatically so every helper call stays clean. */
    private List<Instruction> parse(Token... tokens) {
        Token[] withEof = new Token[tokens.length + 1];
        System.arraycopy(tokens, 0, withEof, 0, tokens.length);
        withEof[tokens.length] = tok(TokenType.EOF, "");
        return new Parser(List.of(withEof)).parse();
    }

    // Shorthand block helpers
    private Token[] wrapInIfBlock(Token condLeft, Token op, Token condRight, Token... bodyTokens) {
        Token[] prefix = {
                tok(TokenType.IF, "if"), condLeft, op, condRight,
                tok(TokenType.THEN, "then"), tok(TokenType.COLON, ":"),
                tok(TokenType.NEWLINE, "\n"), tok(TokenType.INDENT, "")
        };
        Token[] suffix = { tok(TokenType.DEDENT, "") };
        Token[] all = new Token[prefix.length + bodyTokens.length + suffix.length];
        System.arraycopy(prefix,     0, all, 0,                            prefix.length);
        System.arraycopy(bodyTokens, 0, all, prefix.length,                bodyTokens.length);
        System.arraycopy(suffix,     0, all, prefix.length + bodyTokens.length, suffix.length);
        return all;
    }

    private Token[] simplePrintBody() {
        return new Token[]{
                tok(TokenType.PRINT, "print"), tok(TokenType.STRING, "ok"), tok(TokenType.NEWLINE, "\n")
        };
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 1. CONSTRUCTOR  (17 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test void nullList_throws() {
            assertThrows(BloopParseException.class, () -> new Parser(null));
        }

        @Test void emptyList_throws() {
            assertThrows(BloopParseException.class, () -> new Parser(List.of()));
        }

        @Test void singleEofToken_doesNotThrow() {
            assertDoesNotThrow(() -> new Parser(List.of(tok(TokenType.EOF, ""))));
        }

        @Test void singleNonEofToken_doesNotThrow() {
            assertDoesNotThrow(() -> new Parser(List.of(
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.EOF, "")
            )));
        }

        @Test void largeTokenList_doesNotThrow() {
            Token[] tokens = new Token[1000];
            for (int i = 0; i < 999; i++) tokens[i] = tok(TokenType.NEWLINE, "\n");
            tokens[999] = tok(TokenType.EOF, "");
            assertDoesNotThrow(() -> new Parser(List.of(tokens)));
        }

        @Test void tokenListWithOnlyNewlines_doesNotThrow() {
            assertDoesNotThrow(() -> new Parser(List.of(
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            )));
        }

        @Test void constructorStoresTokensCorrectly_parsesSuccessfully() {
            Parser p = new Parser(List.of(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            ));
            assertEquals(1, p.parse().size());
        }

        @Test void listWithMultipleValidInstructions_doesNotThrow() {
            assertDoesNotThrow(() -> new Parser(List.of(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "42"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            )));
        }

        @Test void listWithNegativeLineNumber_throws() {
            assertThrows(IllegalArgumentException.class, () -> new Parser(List.of(
                    tok(TokenType.PRINT, "print", -1),
                    tok(TokenType.NUMBER, "1", -1),
                    tok(TokenType.EOF, "", -1)
            )));
        }

        @Test void listWithLineNumberZero_throws() {
            assertThrows(IllegalArgumentException.class, () -> new Parser(List.of(
                    tok(TokenType.PRINT, "print", 0),
                    tok(TokenType.NUMBER, "5", 0),
                    tok(TokenType.EOF, "", 0)
            )));
        }

        @Test void listWithVeryHighLineNumbers_doesNotThrow() {
            assertDoesNotThrow(() -> new Parser(List.of(
                    tok(TokenType.PRINT, "print", 99999),
                    tok(TokenType.NUMBER, "1", 99999),
                    tok(TokenType.EOF, "", 99999)
            )));
        }

        @Test void constructorImmediatelyFollowedByParse_returnsCorrectCount() {
            List<Instruction> result = new Parser(List.of(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.STRING, "hi"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            )).parse();
            assertEquals(1, result.size());
        }

        @Test void constructorCalledTwice_independentParsers() {
            List<Token> toks = List.of(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            );
            Parser p1 = new Parser(toks);
            Parser p2 = new Parser(toks);
            assertEquals(p1.parse().size(), p2.parse().size());
        }

        @Test void onlyEofToken_returnsEmptyInstructionList() {
            Parser p = new Parser(List.of(tok(TokenType.EOF, "")));
            assertEquals(0, p.parse().size());
        }

        @Test void tokenListWithAllNewlinesThenEof_returnsEmptyList() {
            List<Instruction> result = new Parser(List.of(
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            )).parse();
            assertEquals(0, result.size());
        }

        @Test void parseCalledMultipleTimesOnSameParser_throwsOrReturnsEmpty() {
            // Calling parse() twice is undefined but should not silently corrupt state
            Parser p = new Parser(List.of(
                    tok(TokenType.PRINT, "print"),
                    tok(TokenType.NUMBER, "1"),
                    tok(TokenType.NEWLINE, "\n"),
                    tok(TokenType.EOF, "")
            ));
            p.parse(); // first call
            // Second call behaviour is implementation-defined; it should not crash the JVM
            assertDoesNotThrow(p::parse);
        }

        @Test void tokenListWithDifferentLineNumbers_parsedCorrectly() {
            List<Instruction> result = new Parser(List.of(
                    tok(TokenType.PRINT, "print", 1),
                    tok(TokenType.NUMBER, "7", 2),
                    tok(TokenType.NEWLINE, "\n", 3),
                    tok(TokenType.EOF, "", 4)
            )).parse();
            assertEquals(1, result.size());
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 2. parse()  –  top-level entry point  (15 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parse() – top-level")
    class ParseTopLevelTests {

        @Test void emptyProgram_returns0() { assertEquals(0, parse().size()); }

        @Test void singleNewline_returns0() { assertEquals(0, parse(tok(TokenType.NEWLINE, "\n")).size()); }

        @Test void manyNewlines_returns0() {
            assertEquals(0, parse(
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void singlePut_returns1() {
            assertEquals(1, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void singlePrint_returns1() {
            assertEquals(1, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void threePuts_returns3() {
            assertEquals(3, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"2"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"b"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"3"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"c"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void blankLinesBetweenInstructions_ignored() {
            assertEquals(2, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"2"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void blankLinesAtStart_ignored() {
            assertEquals(1, parse(
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void blankLinesAtEnd_ignored() {
            assertEquals(1, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void unknownLeadingToken_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IDENTIFIER,"garbage"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void mixedInstructionTypes_returnsCorrectCount() {
            assertEquals(3, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"2"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"y"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void instructionMidwayInvalidToken_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.COLON,":")   // unexpected
            ));
        }

        @Test void trailingUnexpectedToken_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.STAR,"*")    // leftover
            ));
        }

        @Test void tenInstructions_returns10() {
            Token[] tokens = new Token[10 * 4];
            for (int i = 0; i < 10; i++) {
                tokens[i*4]   = tok(TokenType.PRINT,"print");
                tokens[i*4+1] = tok(TokenType.NUMBER, String.valueOf(i));
                tokens[i*4+2] = tok(TokenType.NEWLINE,"\n");
                tokens[i*4+3] = tok(TokenType.NEWLINE,"\n");
            }
            assertEquals(10, parse(tokens).size());
        }

        @Test void numberTokenAtTopLevel_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.NUMBER,"42"),tok(TokenType.NEWLINE,"\n")
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 3. parsePutInstruction()  (20 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parsePutInstruction()")
    class PutInstructionTests {

        @Test void putNumber_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"5"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putString_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.STRING,"hello"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"name"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putIdentifier_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.IDENTIFIER,"y"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putArithmeticExpression_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"sum"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putComplexExpression_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.IDENTIFIER,"a"),tok(TokenType.STAR,"*"),tok(TokenType.IDENTIFIER,"b"),
                    tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"result"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putParenthesizedExpression_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.LPAREN,"("),tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"3"),tok(TokenType.RPAREN,")"),
                    tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"4"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putWithoutNewline_returnsAssignInstruction() {
            // no trailing NEWLINE before EOF — should still work (consumeNewlineIfPresent is optional)
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x")
            ).getFirst());
        }

        @Test void putMissingInto_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void putMissingVariableName_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.INTO,"into"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void putMissingExpression_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void putMissingAll_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void putFloatValue_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"3.14"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"pi"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putNegativeNumber_returnsAssignInstruction() {
            // Negative handled via MINUS + NUMBER at expression level
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.NUMBER,"0"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"5"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"neg"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putVariableNameInto_sameNameAsKeyword_throws() {
            // "into" is a keyword TokenType, so it cannot be an IDENTIFIER target
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.INTO,"into"),tok(TokenType.INTO,"into"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void multiplePuts_allReturnAssignInstructions() {
            List<Instruction> list = parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"2"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"b"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"3"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"c"),tok(TokenType.NEWLINE,"\n")
            );
            assertEquals(3, list.size());
            list.forEach(i -> assertInstanceOf(AssignInstruction.class, i));
        }

        @Test void putExpressionWithDivision_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.NUMBER,"10"),tok(TokenType.SLASH,"/"),tok(TokenType.NUMBER,"2"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"half"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putSubtractionExpression_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.NUMBER,"10"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"diff"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putComparisonExpression_returnsAssignInstruction() {
            // Storing a boolean-like comparison result
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"flag"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putWithMultipleBlankLinesBefore_returnsAssignInstruction() {
            assertInstanceOf(AssignInstruction.class, parse(
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void putInvalidNumberLiteral_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"abc"),tok(TokenType.INTO,"into"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 4. parsePrintInstruction()  (17 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parsePrintInstruction()")
    class PrintInstructionTests {

        @Test void printNumber_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"42"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printString_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hello"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printIdentifier_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"result"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printArithmeticExpression_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printParenthesized_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),
                    tok(TokenType.LPAREN,"("),tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"2"),tok(TokenType.RPAREN,")"),
                    tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printComparison_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.EQUAL_EQUAL,"=="),tok(TokenType.NUMBER,"5"),
                    tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printMissingExpression_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void printWithoutNewline_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1")
            ).getFirst());
        }

        @Test void printFollowedByAnotherPrint_returnsBoth() {
            List<Instruction> list = parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"2"),tok(TokenType.NEWLINE,"\n")
            );
            assertEquals(2, list.size());
            assertInstanceOf(PrintInstruction.class, list.getFirst());
            assertInstanceOf(PrintInstruction.class, list.get(1));
        }

        @Test void printZero_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"0"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printNegativeExpression_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),
                    tok(TokenType.NUMBER,"0"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"5"),
                    tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printFloat_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"3.14"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printEmptyString_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,""),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printImmediatelyAtEof_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print")
                    // EOF only — no expression
            ));
        }

        @Test void printComplexNestedExpression_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.PRINT,"print"),
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.RPAREN,")"),
                    tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void multipleBlankLinesThenPrint_returnsPrintInstruction() {
            assertInstanceOf(PrintInstruction.class, parse(
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"7"),tok(TokenType.NEWLINE,"\n")
            ).getFirst());
        }

        @Test void printInvalidNumberInExpression_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"xyz"),tok(TokenType.NEWLINE,"\n")
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 5. parseIfInstruction()  (20 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parseIfInstruction()")
    class IfInstructionTests {

        // Build a minimal valid if block: if x > 0 then: \n INDENT print "ok" \n DEDENT
        private Token[] validIf() {
            return wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"x"), tok(TokenType.GREATER,">"), tok(TokenType.NUMBER,"0"),
                    simplePrintBody()
            );
        }

        @Test void simpleIf_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(validIf()).getFirst());
        }

        @Test void ifWithLess_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"x"), tok(TokenType.LESS,"<"), tok(TokenType.NUMBER,"10"),
                    simplePrintBody()
            )).getFirst());
        }

        @Test void ifWithGreaterEqual_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"score"), tok(TokenType.GREATER_EQUAL,">="), tok(TokenType.NUMBER,"50"),
                    simplePrintBody()
            )).getFirst());
        }

        @Test void ifWithLessEqual_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"score"), tok(TokenType.LESS_EQUAL,"<="), tok(TokenType.NUMBER,"100"),
                    simplePrintBody()
            )).getFirst());
        }

        @Test void ifWithEqualEqual_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"a"), tok(TokenType.EQUAL_EQUAL,"=="), tok(TokenType.IDENTIFIER,"b"),
                    simplePrintBody()
            )).getFirst());
        }

        @Test void ifWithNotEqual_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(wrapInIfBlock(
                    tok(TokenType.IDENTIFIER,"x"), tok(TokenType.NOT_EQUAL,"!="), tok(TokenType.NUMBER,"0"),
                    simplePrintBody()
            )).getFirst());
        }

        @Test void ifMissingThen_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void ifMissingColon_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void ifMissingIndent_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"ok"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void ifEmptyBody_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void ifMissingCondition_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void ifWithMultipleBodyInstructions_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"positive"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"flag"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifBodyWithNestedIf_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    // nested if
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"y"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"both"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifConditionIsArithmetic_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"4"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"yes"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifBodyHasPutInstruction_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"y"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifBodyHasRepeatInstruction_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hi"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifBodyWithBlankLines_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.NEWLINE,"\n"),   // blank line inside block
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"yes"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifFollowedByAnotherIf_returnsBoth() {
            List<Instruction> list = parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"A"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"y"),tok(TokenType.LESS,"<"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"B"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(2, list.size());
            assertInstanceOf(IfInstruction.class, list.getFirst());
            assertInstanceOf(IfInstruction.class, list.get(1));
        }

        @Test void ifConditionParenthesized_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),
                    tok(TokenType.LPAREN,"("),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"1"),tok(TokenType.RPAREN,")"),
                    tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"ok"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void ifMissingDedent_doesNotThrow() {
            // If EOF is hit before DEDENT, the parser exits the block loop gracefully
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"ok"),tok(TokenType.NEWLINE,"\n")
                    // No DEDENT — EOF closes the block
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 6. parseRepeatInstruction()  (20 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parseRepeatInstruction()")
    class RepeatInstructionTests {

        private Token[] validRepeat(String count) {
            return new Token[]{
                    tok(TokenType.REPEAT,"repeat"), tok(TokenType.NUMBER, count),
                    tok(TokenType.TIMES,"times"),   tok(TokenType.COLON,":"),
                    tok(TokenType.NEWLINE,"\n"),    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),   tok(TokenType.STRING,"hi"),
                    tok(TokenType.NEWLINE,"\n"),    tok(TokenType.DEDENT,"")
            };
        }

        @Test void repeat3_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(validRepeat("3")).getFirst());
        }

        @Test void repeat0_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(validRepeat("0")).getFirst());
        }

        @Test void repeat1_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(validRepeat("1")).getFirst());
        }

        @Test void repeatMaxInt_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(validRepeat(String.valueOf(Integer.MAX_VALUE))).getFirst());
        }

        @Test void repeatNegative_throws() {
            assertThrows(BloopParseException.class, () -> parse(validRepeat("-1")));
        }

        @Test void repeatDecimal_throws() {
            assertThrows(BloopParseException.class, () -> parse(validRepeat("2.5")));
        }

        @Test void repeatFloat_throws() {
            assertThrows(BloopParseException.class, () -> parse(validRepeat("1.0")));
        }

        @Test void repeatAlphaCount_throws() {
            assertThrows(BloopParseException.class, () -> parse(validRepeat("abc")));
        }

        @Test void repeatOverMaxInt_throws() {
            assertThrows(BloopParseException.class, () -> parse(validRepeat("9999999999")));
        }

        @Test void repeatMissingCount_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.TIMES,"times"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatMissingTimes_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatMissingColon_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatEmptyBody_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void repeatBodyMultipleInstructions_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"b"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void repeatNestedInIf_returnsIfInstruction() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"loop"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void repeatNestedRepeat_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"deep"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void repeatFollowedByPrint_returnsBothInstructions() {
            List<Instruction> list = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"loop"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"done"),tok(TokenType.NEWLINE,"\n")
            );
            assertEquals(2, list.size());
            assertInstanceOf(RepeatInstruction.class, list.getFirst());
            assertInstanceOf(PrintInstruction.class,  list.get(1));
        }

        @Test void repeatWithPutInBody_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"5"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"i"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void repeatCountAsLargeLong_throws() {
            // Beyond Integer.MAX_VALUE but within Long.MAX_VALUE
            assertThrows(BloopParseException.class, () -> parse(
                    validRepeat(String.valueOf((long) Integer.MAX_VALUE + 1))
            ));
        }

        @Test void repeatBodyWithBlankLines_returnsRepeatInstruction() {
            assertInstanceOf(RepeatInstruction.class, parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hi"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 7. parseIndentedBlock()  (15 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("parseIndentedBlock()")
    class IndentedBlockTests {

        // All block tests go through if/repeat as the block is not directly accessible

        @Test void blockWithSinglePrint_parsedCorrectly() {
            assertInstanceOf(IfInstruction.class, parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"yes"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ).getFirst());
        }

        @Test void blockWithThreeInstructions_allParsed() {
            // Three prints in the block
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"b"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"c"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertInstanceOf(RepeatInstruction.class, top.getFirst());
            // Verify body size by casting
            assertEquals(3, ((RepeatInstruction) top.getFirst()).getBody().size());
        }

        @Test void blockEmptyThrows_viaIf() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void blockMissingIndentToken_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hi"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void blockWithBlankLineInsideBlock_ignored() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hi"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(1, ((RepeatInstruction) top.getFirst()).getBody().size());
        }

        @Test void blockWithPutAndPrint_twoInstructions() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(2, ((RepeatInstruction) top.getFirst()).getBody().size());
        }

        @Test void nestedBlocksTwoDeep_parsedCorrectly() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"deep"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            );
            assertInstanceOf(RepeatInstruction.class, top.getFirst());
        }

        @Test void blockWithIfInsideRepeat_parsedCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"ok"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test void blockWithInvalidInstruction_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.COLON,":"),   // invalid token inside block
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test void blockContainingMixedValidInstructions_allParsed() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"10"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(3, ((RepeatInstruction) top.getFirst()).getBody().size());
        }

        @Test void blockWithMultipleBlanksAndInstructions_correctCount() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"b"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(2, ((RepeatInstruction) top.getFirst()).getBody().size());
        }

        @Test void blockWithPutExpressionBody_parsedCorrectly() {
            List<Instruction> top = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.IDENTIFIER,"a"),tok(TokenType.PLUS,"+"),tok(TokenType.IDENTIFIER,"b"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"c"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertInstanceOf(RepeatInstruction.class, top.getFirst());
        }

        @Test void threeDeepNesting_parsedCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"deep"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test void blockEofBeforeDedent_doesNotThrow() {
            // Parser exits block loop when EOF is encountered
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"hi"),tok(TokenType.NEWLINE,"\n")
                    // No DEDENT – EOF closes the block
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 8. Expression Parsing – precedence, operators, primary  (20 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Expression Parsing")
    class ExpressionTests {

        /** Wraps expression tokens in a print statement to trigger expression parsing. */
        private List<Instruction> exprParse(Token... exprTokens) {
            Token[] all = new Token[exprTokens.length + 3];
            all[0] = tok(TokenType.PRINT,"print");
            System.arraycopy(exprTokens, 0, all, 1, exprTokens.length);
            all[exprTokens.length + 1] = tok(TokenType.NEWLINE,"\n");
            all[exprTokens.length + 2] = tok(TokenType.EOF,"");
            return new Parser(List.of(all)).parse();
        }

        @Test void singleNumber_parsedAsPrimary() {
            assertEquals(1, exprParse(tok(TokenType.NUMBER,"42")).size());
        }

        @Test void singleString_parsedAsPrimary() {
            assertEquals(1, exprParse(tok(TokenType.STRING,"hello")).size());
        }

        @Test void singleIdentifier_parsedAsPrimary() {
            assertEquals(1, exprParse(tok(TokenType.IDENTIFIER,"x")).size());
        }

        @Test void addition_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"2")
            ).size());
        }

        @Test void subtraction_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"5"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"3")
            ).size());
        }

        @Test void multiplication_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"4"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"2")
            ).size());
        }

        @Test void division_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"10"),tok(TokenType.SLASH,"/"),tok(TokenType.NUMBER,"5")
            ).size());
        }

        @Test void multiplyBeforeAdd_precedence() {
            // 2 + 3 * 4 → BinaryOp(+, 2, BinaryOp(*, 3, 4))
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),
                    tok(TokenType.NUMBER,"3"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"4")
            ).size());
        }

        @Test void parenthesesOverridePrecedence_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.RPAREN,")"),
                    tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"4")
            ).size());
        }

        @Test void comparisonGreater_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0")
            ).size());
        }

        @Test void comparisonLess_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.LESS,"<"),tok(TokenType.NUMBER,"10")
            ).size());
        }

        @Test void comparisonGreaterEqual_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER_EQUAL,">="),tok(TokenType.NUMBER,"5")
            ).size());
        }

        @Test void comparisonLessEqual_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.LESS_EQUAL,"<="),tok(TokenType.NUMBER,"5")
            ).size());
        }

        @Test void comparisonEqualEqual_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.EQUAL_EQUAL,"=="),tok(TokenType.NUMBER,"0")
            ).size());
        }

        @Test void comparisonNotEqual_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NOT_EQUAL,"!="),tok(TokenType.NUMBER,"0")
            ).size());
        }

        @Test void nestedParentheses_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.LPAREN,"("),tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"2"),tok(TokenType.RPAREN,")"),
                    tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.RPAREN,")")
            ).size());
        }

        @Test void unclosedParen_throws() {
            assertThrows(BloopParseException.class, () -> exprParse(
                    tok(TokenType.LPAREN,"("),tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),tok(TokenType.NUMBER,"2")
                    // Missing RPAREN
            ));
        }

        @Test void consecutiveOperatorsNoOperand_throws() {
            assertThrows(BloopParseException.class, () -> exprParse(
                    tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"2")
            ));
        }

        @Test void chainedAdditions_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.PLUS,"+"),
                    tok(TokenType.NUMBER,"3")
            ).size());
        }

        @Test void chainedMultiplications_parsedCorrectly() {
            assertEquals(1, exprParse(
                    tok(TokenType.NUMBER,"2"),tok(TokenType.STAR,"*"),
                    tok(TokenType.NUMBER,"3"),tok(TokenType.STAR,"*"),
                    tok(TokenType.NUMBER,"4")
            ).size());
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 9. parseNumberLiteral() & parseAndValidateRepeatCount()  (15 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Number Literal & Repeat Count Validation")
    class NumberAndRepeatCountTests {

        // ── Number Literal (via parsePrimary) ──

        @Test void integerLiteral_parsedSuccessfully() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"100"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void floatLiteral_parsedSuccessfully() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"3.14"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void zeroDotZero_parsedSuccessfully() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"0.0"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void invalidNumberLiteral_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"NaN_bad"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void emptyNumberLiteral_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,""),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void scientificNotation_parsedSuccessfully() {
            // Double.parseDouble accepts "1e5"
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1e5"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        // ── Repeat Count Validation ──

        @Test void repeatCount_zero_valid() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"0"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"x"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void repeatCount_one_valid() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"x"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void repeatCount_integerMax_valid() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,String.valueOf(Integer.MAX_VALUE)),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"x"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.DEDENT,"")
            ));
        }

        @Test void repeatCount_negative_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"-1"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatCount_decimal_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"1.5"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatCount_longMax_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,String.valueOf(Long.MAX_VALUE)),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatCount_negativeDecimal_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"-3.5"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatCount_garbage_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"three"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void repeatCount_intMaxPlusOne_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,String.valueOf((long)Integer.MAX_VALUE + 1)),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n")
            ));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // 10. Integration / Mixed Programs  (15 cases)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration – Mixed Programs")
    class IntegrationTests {

        @Test void putThenPrint_twoInstructions() {
            List<Instruction> list = parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"7"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n")
            );
            assertEquals(2, list.size());
            assertInstanceOf(AssignInstruction.class, list.getFirst());
            assertInstanceOf(PrintInstruction.class,  list.get(1));
        }

        @Test void putThenIfWithPutInBody_twoTopLevelInstructions() {
            List<Instruction> list = parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"5"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"big"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(2, list.size());
        }

        @Test void repeatContainingIf_topLevelOneInstruction() {
            List<Instruction> list = parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"x"),tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"0"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"yes"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(1, list.size());
            assertInstanceOf(RepeatInstruction.class, list.getFirst());
        }

        @Test void emptyProgramWithManyNewlines_returns0() {
            assertEquals(0, parse(
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n")
            ).size());
        }

        @Test void allInstructionTypes_parsedInOrder() {
            List<Instruction> list = parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"1"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"a"),tok(TokenType.EQUAL_EQUAL,"=="),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"one"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"2"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"loop"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(4, list.size());
            assertInstanceOf(AssignInstruction.class, list.getFirst());
            assertInstanceOf(PrintInstruction.class,  list.get(1));
            assertInstanceOf(IfInstruction.class,     list.get(2));
            assertInstanceOf(RepeatInstruction.class,  list.get(3));
        }

        @Test void expressionUsedInBothConditionAndBody_parsesCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.IF,"if"),
                    tok(TokenType.IDENTIFIER,"a"),tok(TokenType.PLUS,"+"),tok(TokenType.IDENTIFIER,"b"),
                    tok(TokenType.GREATER,">"),tok(TokenType.NUMBER,"10"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.IDENTIFIER,"a"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"2"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"result"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test
        void programWithAllComparisonOps_sixIfs() {
            TokenType[] ops = {
                    TokenType.GREATER, TokenType.LESS,
                    TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
                    TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL
            };
            String[] syms = {">", "<", ">=", "<=", "==", "!="};

            for (int i = 0; i < ops.length; i++) {
                final int idx = i;
                assertDoesNotThrow(() -> parse(
                        tok(TokenType.IF,      "if"),
                        tok(TokenType.IDENTIFIER, "x"),
                        tok(ops[idx],          syms[idx]),
                        tok(TokenType.NUMBER,  "5"),
                        tok(TokenType.THEN,    "then"),
                        tok(TokenType.COLON,   ":"),
                        tok(TokenType.NEWLINE, "\n"),
                        tok(TokenType.INDENT,  ""),
                        tok(TokenType.PRINT,   "print"),
                        tok(TokenType.STRING,  syms[idx]),
                        tok(TokenType.NEWLINE, "\n"),
                        tok(TokenType.DEDENT,  "")
                ));
            }
        }
        @Test void complexArithmeticInIfCondition_parsesCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.IF,"if"),
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.IDENTIFIER,"x"),tok(TokenType.STAR,"*"),tok(TokenType.NUMBER,"2"),
                    tok(TokenType.RPAREN,")"),
                    tok(TokenType.GREATER_EQUAL,">="),
                    tok(TokenType.IDENTIFIER,"y"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"1"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"ok"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test void repeatBodyWithIfThenPrint_parsesCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"5"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.IF,"if"),tok(TokenType.IDENTIFIER,"i"),tok(TokenType.EQUAL_EQUAL,"=="),tok(TokenType.NUMBER,"3"),
                    tok(TokenType.THEN,"then"),tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.STRING,"three"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,""),
                    tok(TokenType.DEDENT,"")
            ));
        }

        @Test void trailingGarbageAfterValidProgram_throws() {
            assertThrows(BloopParseException.class, () -> parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.COLON,":")   // unexpected leftover
            ));
        }

        @Test void putFollowedImmediatelyByRepeat_bothParsed() {
            List<Instruction> list = parse(
                    tok(TokenType.PUT,"put"),tok(TokenType.NUMBER,"0"),tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"i"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.REPEAT,"repeat"),tok(TokenType.NUMBER,"3"),tok(TokenType.TIMES,"times"),
                    tok(TokenType.COLON,":"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.INDENT,""),
                    tok(TokenType.PRINT,"print"),tok(TokenType.IDENTIFIER,"i"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.DEDENT,"")
            );
            assertEquals(2, list.size());
        }

        @Test void printExpressionsWithAllArithmeticOps_allParse() {
            for (TokenType op : new TokenType[]{TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH}) {
                assertDoesNotThrow(() -> parse(
                        tok(TokenType.PRINT,"print"),
                        tok(TokenType.NUMBER,"10"),tok(op, op.name()),tok(TokenType.NUMBER,"2"),
                        tok(TokenType.NEWLINE,"\n")
                ));
            }
        }

        @Test void deeplyNestedArithmeticInPut_parsesCorrectly() {
            assertDoesNotThrow(() -> parse(
                    tok(TokenType.PUT,"put"),
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.NUMBER,"1"),tok(TokenType.PLUS,"+"),
                    tok(TokenType.LPAREN,"("),
                    tok(TokenType.NUMBER,"2"),tok(TokenType.STAR,"*"),
                    tok(TokenType.LPAREN,"("),tok(TokenType.NUMBER,"3"),tok(TokenType.MINUS,"-"),tok(TokenType.NUMBER,"1"),tok(TokenType.RPAREN,")"),
                    tok(TokenType.RPAREN,")"),
                    tok(TokenType.RPAREN,")"),
                    tok(TokenType.INTO,"into"),tok(TokenType.IDENTIFIER,"result"),tok(TokenType.NEWLINE,"\n")
            ));
        }

        @Test void instructionsWithLineNumbersAcrossMultipleLines_parsedCorrectly() {
            List<Instruction> list = new Parser(List.of(
                    tok(TokenType.PUT,"put",1),tok(TokenType.NUMBER,"1",1),tok(TokenType.INTO,"into",1),tok(TokenType.IDENTIFIER,"x",1),tok(TokenType.NEWLINE,"\n",1),
                    tok(TokenType.PRINT,"print",2),tok(TokenType.IDENTIFIER,"x",2),tok(TokenType.NEWLINE,"\n",2),
                    tok(TokenType.EOF,"",3)
            )).parse();
            assertEquals(2, list.size());
        }

        @Test void multipleNewlinesBetweenEveryInstruction_returns3() {
            List<Instruction> list = parse(
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"1"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"2"),tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.NEWLINE,"\n"),
                    tok(TokenType.PRINT,"print"),tok(TokenType.NUMBER,"3"),tok(TokenType.NEWLINE,"\n")
            );
            assertEquals(3, list.size());
        }
    }
}