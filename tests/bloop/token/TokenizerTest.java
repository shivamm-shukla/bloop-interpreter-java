package bloop.token;

import bloop.exceptions.BloopLexerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static bloop.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Tokenizer — Full Exhaustive Test Suite")
class TokenizerTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<Token> tokenize(String src) {
        return new Tokenizer(src).tokenize();
    }

    // Assert exact sequence of TokenTypes (including EOF at the end)
    private void assertTypes(List<Token> tokens, TokenType... expected) {
        assertEquals(expected.length, tokens.size(),
                "Token count mismatch.\nExpected: " + java.util.Arrays.toString(expected)
                        + "\nActual  : " + tokens);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).type(),
                    "Wrong type at index " + i + ". Full list: " + tokens);
        }
    }

    private void assertTypeValue(Token t, TokenType type, String value) {
        assertAll(
                () -> assertEquals(type,  t.type(),  "type"),
                () -> assertEquals(value, t.value(), "value")
        );
    }

    private Token lastToken(List<Token> tokens) {
        return tokens.getLast();
    }

    private long countOf(List<Token> tokens, TokenType type) {
        return tokens.stream().filter(t -> t.type() == type).count();
    }

    private boolean has(List<Token> tokens, TokenType type) {
        return tokens.stream().anyMatch(t -> t.type() == type);
    }


    // =========================================================================
    // 1. CONSTRUCTOR / NULL-SAFETY
    // =========================================================================

    @Nested
    @DisplayName("1. Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("null source → IllegalArgumentException")
        void nullSource() {
            assertThrows(IllegalArgumentException.class, () -> new Tokenizer(null));
        }

        @Test
        @DisplayName("valid empty string — no exception")
        void emptyStringNoException() {
            assertDoesNotThrow(() -> new Tokenizer(""));
        }

        @Test
        @DisplayName("Tokenizer is re-usable — two calls to tokenize() produce equal results")
        void tokenizeIsIdempotent() {
            Tokenizer t = new Tokenizer("x");
            List<Token> first  = t.tokenize();
            List<Token> second = t.tokenize();
            assertEquals(first.size(), second.size());
            for (int i = 0; i < first.size(); i++) {
                assertEquals(first.get(i).type(), second.get(i).type());
            }
        }
    }


    // =========================================================================
    // 2. EMPTY & WHITESPACE-ONLY INPUTS
    // =========================================================================

    @Nested
    @DisplayName("2. Empty / whitespace-only inputs")
    class EmptyInputTests {

        @Test
        @DisplayName("empty string → [EOF]")
        void emptyString() {
            assertTypes(tokenize(""), EOF);
        }

        @Test
        @DisplayName("only spaces → [EOF]")
        void onlySpaces() {
            assertTypes(tokenize("     "), EOF);
        }

        @Test
        @DisplayName("only tabs → [EOF]")
        void onlyTabs() {
            assertTypes(tokenize("\t\t\t"), EOF);
        }

        @Test
        @DisplayName("only newlines → [EOF]")
        void onlyNewlines() {
            assertTypes(tokenize("\n\n\n"), EOF);
        }

        @Test
        @DisplayName("mixed whitespace only → [EOF]")
        void mixedWhitespace() {
            assertTypes(tokenize("  \t  \n  \t  \n"), EOF);
        }
    }


    // =========================================================================
    // 3. EOF GUARANTEES
    // =========================================================================

    @Nested
    @DisplayName("3. EOF guarantees")
    class EofTests {

        @Test
        @DisplayName("EOF is always the very last token")
        void eofIsLast() {
            for (String src : new String[]{"", "x", "42", "\"hi\"", "x + y"}) {
                assertEquals(EOF, lastToken(tokenize(src)).type(),
                        "Expected EOF as last token for: " + src);
            }
        }

        @Test
        @DisplayName("EOF value is always empty string")
        void eofValueEmpty() {
            assertTypeValue(lastToken(tokenize("")), EOF, "");
            assertTypeValue(lastToken(tokenize("x")), EOF, "");
        }

        @Test
        @DisplayName("exactly one EOF per tokenization")
        void exactlyOneEof() {
            assertEquals(1, countOf(tokenize("a + b"), EOF));
            assertEquals(1, countOf(tokenize(""), EOF));
        }
    }


    // =========================================================================
    // 4. INLINE WHITESPACE
    // =========================================================================

    @Nested
    @DisplayName("4. Inline whitespace (spaces & tabs)")
    class InlineWhitespaceTests {

        @Test
        @DisplayName("spaces between tokens are silently consumed")
        void spacesBetweenTokens() {
            assertTypes(tokenize("a + b"), IDENTIFIER, PLUS, IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("tabs between tokens are silently consumed")
        void tabsBetweenTokens() {
            assertTypes(tokenize("a\t+\tb"), IDENTIFIER, PLUS, IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("multiple spaces between tokens")
        void multipleSpaces() {
            assertTypes(tokenize("1    +    2"), NUMBER, PLUS, NUMBER, EOF);
        }

        @Test
        @DisplayName("leading spaces on first line are consumed silently — no INDENT")
        void leadingSpacesFirstLine() {
            // IndentationHandler is only invoked after a \n, never for the first line
            assertTypes(tokenize("   x"), IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("no dedicated whitespace token is ever produced")
        void noWhitespaceToken() {
            List<Token> tokens = tokenize("a   b   c");
            tokens.forEach(t -> assertNotEquals("WHITESPACE", t.type().name()));
        }
    }


    // =========================================================================
    // 5. NEWLINE EMISSION RULES
    // =========================================================================

    @Nested
    @DisplayName("5. NEWLINE emission")
    class NewlineTests {

        @Test
        @DisplayName("newline between two statements → NEWLINE token emitted")
        void newlineBetweenStatements() {
            assertTypes(tokenize("a\nb"), IDENTIFIER, NEWLINE, IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("NEWLINE value is empty string")
        void newlineValueEmpty() {
            assertTypeValue(tokenize("a\nb").get(1), NEWLINE, "");
        }

        @Test
        @DisplayName("NEWLINE carries line number of line that just ended")
        void newlineLineNumber() {
            assertEquals(1, tokenize("a\nb").get(1).line());
        }

        @Test
        @DisplayName("trailing newline at EOF — NEWLINE suppressed (blank next line)")
        void trailingNewlineNoToken() {
            // after \n cursor is exhausted → blank-line path → NEWLINE suppressed
            assertTypes(tokenize("a\n"), IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("three statements separated by newlines → two NEWLINEs")
        void threeStatements() {
            assertEquals(2, countOf(tokenize("a\nb\nc"), NEWLINE));
        }
    }


    // =========================================================================
    // 6. BLANK LINE HANDLING
    // =========================================================================

    @Nested
    @DisplayName("6. Blank lines")
    class BlankLineTests {

        @Test
        @DisplayName("consecutive newlines produce only one NEWLINE token")
        void consecutiveNewlines() {
            assertTypes(tokenize("a\n\nb"), IDENTIFIER, NEWLINE, IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("many blank lines between two statements — still one NEWLINE")
        void manyBlankLines() {
            assertEquals(1, countOf(tokenize("a\n\n\n\nb"), NEWLINE));
        }

        @Test
        @DisplayName("blank line inside indented block does not break structure")
        void blankLineInsideBlock() {
            String src = "a\n    b\n\n    c\nd";
            assertDoesNotThrow(() -> tokenize(src));
            List<Token> tokens = tokenize(src);
            assertEquals(1, countOf(tokens, INDENT));
            assertEquals(1, countOf(tokens, DEDENT));
        }
    }


    // =========================================================================
    // 7. NUMBER LITERALS
    // =========================================================================

    @Nested
    @DisplayName("7. Number literals")
    class NumberTests {

        @Test @DisplayName("single digit 0")
        void zero() { assertTypeValue(tokenize("0").getFirst(), NUMBER, "0"); }

        @Test @DisplayName("multi-digit integer")
        void multiDigit() { assertTypeValue(tokenize("1234").getFirst(), NUMBER, "1234"); }

        @Test @DisplayName("large integer")
        void largeInt() { assertTypeValue(tokenize("999999999").getFirst(), NUMBER, "999999999"); }

        @Test @DisplayName("simple float 3.14")
        void simpleFloat() { assertTypeValue(tokenize("3.14").getFirst(), NUMBER, "3.14"); }

        @Test @DisplayName("float 0.5")
        void floatZeroPoint() { assertTypeValue(tokenize("0.5").getFirst(), NUMBER, "0.5"); }

        @Test @DisplayName("number immediately followed by +")
        void numberFollowedByOp() {
            assertTypes(tokenize("10+5"), NUMBER, PLUS, NUMBER, EOF);
        }

        @Test @DisplayName("number inside parentheses")
        void numberInParens() {
            assertTypes(tokenize("(10)"), LEFT_PAREN, NUMBER, RIGHT_PAREN, EOF);
        }

        @Test @DisplayName("multiple decimal points → BloopLexerException")
        void multipleDecimals() {
            assertThrows(BloopLexerException.class, () -> tokenize("1.2.3"));
        }

        @Test @DisplayName("user guide example 3.14.15 → BloopLexerException")
        void userGuideExample() {
            assertThrows(BloopLexerException.class, () -> tokenize("3.14.15"));
        }

        @Test @DisplayName("trailing dot '1.' — dot not a valid operator → BloopLexerException")
        void trailingDot() {
            assertThrows(BloopLexerException.class, () -> tokenize("1."));
        }

        @Test @DisplayName("number value preserved exactly as in source")
        void valueExact() {
            assertEquals("42",   tokenize("42").getFirst().value());
            assertEquals("3.14", tokenize("3.14").getFirst().value());
        }
    }


    // =========================================================================
    // 8. STRING LITERALS
    // =========================================================================

    @Nested
    @DisplayName("8. String literals")
    class StringTests {

        @Test @DisplayName("simple string")
        void simple() { assertTypeValue(tokenize("\"hello\"").getFirst(), STRING, "hello"); }

        @Test @DisplayName("empty string")
        void empty() { assertTypeValue(tokenize("\"\"").getFirst(), STRING, ""); }

        @Test @DisplayName("string with spaces")
        void withSpaces() { assertTypeValue(tokenize("\"hello world\"").getFirst(), STRING, "hello world"); }

        @Test @DisplayName("string with digits")
        void withDigits() { assertTypeValue(tokenize("\"abc123\"").getFirst(), STRING, "abc123"); }

        @Test @DisplayName("string with operator symbols inside")
        void withOperators() { assertTypeValue(tokenize("\"a+b=c\"").getFirst(), STRING, "a+b=c"); }

        @Test @DisplayName("value does NOT include surrounding quotes")
        void noQuotesInValue() {
            String v = tokenize("\"bloop\"").getFirst().value();
            assertFalse(v.startsWith("\"") || v.endsWith("\""));
        }

        @Test @DisplayName("unterminated string (EOF) → BloopLexerException")
        void unterminatedEof() {
            assertThrows(BloopLexerException.class, () -> tokenize("\"hello"));
        }

        @Test @DisplayName("unterminated string (newline inside) → BloopLexerException")
        void unterminatedNewline() {
            assertThrows(BloopLexerException.class, () -> tokenize("\"hello\nworld\""));
        }

        @Test @DisplayName("two adjacent strings separated by space")
        void twoStrings() {
            assertTypes(tokenize("\"hi\" \"there\""), STRING, STRING, EOF);
        }

        @Test @DisplayName("string followed immediately by operator")
        void stringAndOperator() {
            assertTypes(tokenize("\"a\"+\"b\""), STRING, PLUS, STRING, EOF);
        }
    }


    // =========================================================================
    // 9. STRING ESCAPE SEQUENCES
    // =========================================================================

    @Nested
    @DisplayName("9. String escape sequences")
    class EscapeTests {

        @Test @DisplayName("\\n → newline char")
        void escN() { assertTypeValue(tokenize("\"a\\nb\"").getFirst(), STRING, "a\nb"); }

        @Test @DisplayName("\\t → tab char")
        void escT() { assertTypeValue(tokenize("\"a\\tb\"").getFirst(), STRING, "a\tb"); }

        @Test @DisplayName("\\\\ → single backslash")
        void escBackslash() { assertTypeValue(tokenize("\"a\\\\b\"").getFirst(), STRING, "a\\b"); }

        @Test @DisplayName("\\\" → double quote char")
        void escQuote() { assertTypeValue(tokenize("\"a\\\"b\"").getFirst(), STRING, "a\"b"); }

        @Test @DisplayName("multiple escapes in one string")
        void multipleEscapes() {
            assertTypeValue(tokenize("\"\\n\\t\\\\\"").getFirst(), STRING, "\n\t\\");
        }

        @Test @DisplayName("user guide: say \\\"hello\\\"")
        void userGuideEscapeExample() {
            assertTypeValue(tokenize("\"say \\\"hello\\\"\"").getFirst(), STRING, "say \"hello\"");
        }

        @ParameterizedTest(name = "unknown escape ''\\{0}'' → BloopLexerException")
        @ValueSource(strings = {"z", "a", "0", "r", "p"})
        @DisplayName("unknown escape sequences throw")
        void unknownEscapes(String ch) {
            assertThrows(BloopLexerException.class, () -> tokenize("\"\\" + ch + "\""));
        }

        @Test @DisplayName("incomplete escape at EOF → BloopLexerException")
        void incompleteEscapeEof() {
            assertThrows(BloopLexerException.class, () -> tokenize("\"\\"));
        }
    }


    // =========================================================================
    // 10. IDENTIFIERS
    // =========================================================================

    @Nested
    @DisplayName("10. Identifiers")
    class IdentifierTests {

        @Test @DisplayName("single letter")
        void single() { assertTypeValue(tokenize("x").getFirst(), IDENTIFIER, "x"); }

        @Test @DisplayName("all lowercase")
        void lowercase() { assertTypeValue(tokenize("abc").getFirst(), IDENTIFIER, "abc"); }

        @Test @DisplayName("all uppercase (not a keyword)")
        void uppercase() { assertTypeValue(tokenize("XYZ").getFirst(), IDENTIFIER, "XYZ"); }

        @Test @DisplayName("mixed case")
        void mixed() { assertTypeValue(tokenize("myVar").getFirst(), IDENTIFIER, "myVar"); }

        @Test @DisplayName("starts with underscore")
        void underscoreStart() { assertTypeValue(tokenize("_var").getFirst(), IDENTIFIER, "_var"); }

        @Test @DisplayName("only underscores")
        void onlyUnderscores() { assertTypeValue(tokenize("___").getFirst(), IDENTIFIER, "___"); }

        @Test @DisplayName("letters + digits")
        void lettersDigits() { assertTypeValue(tokenize("score1").getFirst(), IDENTIFIER, "score1"); }

        @Test @DisplayName("case-sensitive: 'score' ≠ 'Score'")
        void caseSensitive() {
            List<Token> tokens = tokenize("score Score");
            assertEquals(IDENTIFIER, tokens.getFirst().type());
            assertEquals(IDENTIFIER, tokens.get(1).type());
            assertNotEquals(tokens.getFirst().value(), tokens.get(1).value());
        }

        @Test @DisplayName("identifier stops at operator")
        void stopsAtOp() {
            assertTypes(tokenize("x+y"), IDENTIFIER, PLUS, IDENTIFIER, EOF);
        }

        @Test @DisplayName("identifier stops at paren")
        void stopsAtParen() {
            assertTypes(tokenize("f(x)"), IDENTIFIER, LEFT_PAREN, IDENTIFIER, RIGHT_PAREN, EOF);
        }
    }


    // =========================================================================
    // 11. KEYWORDS
    // =========================================================================

    @Nested
    @DisplayName("11. Keywords")
    class KeywordTests {

        @Test @DisplayName("put")    void kwPut()    { assertEquals(PUT,    tokenize("put").getFirst().type()); }
        @Test @DisplayName("into")   void kwInto()   { assertEquals(INTO,   tokenize("into").getFirst().type()); }
        @Test @DisplayName("print")  void kwPrint()  { assertEquals(PRINT,  tokenize("print").getFirst().type()); }
        @Test @DisplayName("if")     void kwIf()     { assertEquals(IF,     tokenize("if").getFirst().type()); }
        @Test @DisplayName("else")   void kwElse()   { assertEquals(ELSE,   tokenize("else").getFirst().type()); }
        @Test @DisplayName("then")   void kwThen()   { assertEquals(THEN,   tokenize("then").getFirst().type()); }
        @Test @DisplayName("repeat") void kwRepeat() { assertEquals(REPEAT, tokenize("repeat").getFirst().type()); }
        @Test @DisplayName("times")  void kwTimes()  { assertEquals(TIMES,  tokenize("times").getFirst().type()); }

        @Test @DisplayName("keyword value matches source text")
        void valuePreserved() {
            assertTypeValue(tokenize("repeat").getFirst(), REPEAT, "repeat");
        }

        @ParameterizedTest(name = "''{0}'' is IDENTIFIER (case-sensitive)")
        @ValueSource(strings = {
                "PUT", "Put", "PRINT", "Print", "IF", "If",
                "ELSE", "Else", "THEN", "Then",
                "REPEAT", "Repeat", "TIMES", "Times", "INTO", "Into"
        })
        @DisplayName("keywords are case-sensitive — uppercase variants are IDENTIFIERs")
        void caseSensitive(String word) {
            assertEquals(IDENTIFIER, tokenize(word).getFirst().type());
        }

        @ParameterizedTest(name = "''{0}'' is IDENTIFIER (not a keyword)")
        @ValueSource(strings = {"putting", "printf", "iffy", "elsewhere",
                "repeating", "timeout", "into2", "puts", "then2"})
        @DisplayName("keyword substrings are IDENTIFIERs")
        void keywordSubstrings(String word) {
            assertEquals(IDENTIFIER, tokenize(word).getFirst().type());
        }
    }


    // =========================================================================
    // 12. ARITHMETIC OPERATORS
    // =========================================================================

    @Nested
    @DisplayName("12. Arithmetic operators")
    class ArithmeticTests {

        @Test @DisplayName("+ → PLUS")  void plus()  { assertTypeValue(tokenize("+").getFirst(), PLUS,  "+"); }
        @Test @DisplayName("- → MINUS") void minus() { assertTypeValue(tokenize("-").getFirst(), MINUS, "-"); }
        @Test @DisplayName("* → STAR")  void star()  { assertTypeValue(tokenize("*").getFirst(), STAR,  "*"); }
        @Test @DisplayName("/ → SLASH") void slash() { assertTypeValue(tokenize("/").getFirst(), SLASH, "/"); }

        @Test @DisplayName("all four in one expression")
        void allFour() {
            assertTypes(tokenize("a + b - c * d / e"),
                    IDENTIFIER, PLUS, IDENTIFIER, MINUS,
                    IDENTIFIER, STAR, IDENTIFIER, SLASH,
                    IDENTIFIER, EOF);
        }

        @Test @DisplayName("no spaces between operands and operator")
        void noSpaces() {
            assertTypes(tokenize("a+b"), IDENTIFIER, PLUS, IDENTIFIER, EOF);
        }
    }


    // =========================================================================
    // 13. COMPARISON OPERATORS
    // =========================================================================

    @Nested
    @DisplayName("13. Comparison operators")
    class ComparisonTests {

        @Test @DisplayName("== → EQUAL_EQUAL")    void eq()  { assertTypeValue(tokenize("==").getFirst(), EQUAL_EQUAL,   "=="); }
        @Test @DisplayName("!= → NOT_EQUAL")       void ne()  { assertTypeValue(tokenize("!=").getFirst(), NOT_EQUAL,    "!="); }
        @Test @DisplayName(">  → GREATER")         void gt()  { assertTypeValue(tokenize(">").getFirst(),  GREATER,      ">"); }
        @Test @DisplayName(">= → GREATER_EQUAL")   void gte() { assertTypeValue(tokenize(">=").getFirst(), GREATER_EQUAL,">="); }
        @Test @DisplayName("<  → LESS")            void lt()  { assertTypeValue(tokenize("<").getFirst(),  LESS,         "<"); }
        @Test @DisplayName("<= → LESS_EQUAL")      void lte() { assertTypeValue(tokenize("<=").getFirst(), LESS_EQUAL,   "<="); }

        @Test @DisplayName("bare '=' → BloopLexerException")
        void bareEqual() { assertThrows(BloopLexerException.class, () -> tokenize("=")); }

        @Test @DisplayName("bare '!' → BloopLexerException")
        void bareNot()   { assertThrows(BloopLexerException.class, () -> tokenize("!")); }

        @Test @DisplayName("'=' followed by non-'=' → BloopLexerException")
        void equalThenOther() { assertThrows(BloopLexerException.class, () -> tokenize("=x")); }

        @Test @DisplayName("'!' followed by non-'=' → BloopLexerException")
        void bangThenOther()  { assertThrows(BloopLexerException.class, () -> tokenize("!x")); }

        @Test @DisplayName("'>' not followed by '=' → plain GREATER")
        void greaterOnly() { assertTypes(tokenize(">1"), GREATER, NUMBER, EOF); }

        @Test @DisplayName("'<' not followed by '=' → plain LESS")
        void lessOnly()    { assertTypes(tokenize("<1"), LESS, NUMBER, EOF); }

        @Test @DisplayName("all six operators in one expression")
        void allSix() {
            assertTypes(tokenize("a == b != c > d >= e < f <= g"),
                    IDENTIFIER, EQUAL_EQUAL,
                    IDENTIFIER, NOT_EQUAL,
                    IDENTIFIER, GREATER,
                    IDENTIFIER, GREATER_EQUAL,
                    IDENTIFIER, LESS,
                    IDENTIFIER, LESS_EQUAL,
                    IDENTIFIER, EOF);
        }
    }


    // =========================================================================
    // 14. SYMBOL TOKENS
    // =========================================================================

    @Nested
    @DisplayName("14. Symbol tokens")
    class SymbolTests {

        @Test @DisplayName("( → LEFT_PAREN")  void lp() { assertTypeValue(tokenize("(").getFirst(), LEFT_PAREN,  "("); }
        @Test @DisplayName(") → RIGHT_PAREN") void rp() { assertTypeValue(tokenize(")").getFirst(), RIGHT_PAREN, ")"); }
        @Test @DisplayName(", → COMMA")       void cm() { assertTypeValue(tokenize(",").getFirst(), COMMA,       ","); }
        @Test @DisplayName(": → COLON")       void cl() { assertTypeValue(tokenize(":").getFirst(), COLON,       ":"); }

        @Test @DisplayName("colon at end of if-then header")
        void colonAfterThen() {
            assertTypes(tokenize("if x then:"), IF, IDENTIFIER, THEN, COLON, EOF);
        }

        @Test @DisplayName("comma-separated args: f(a, b, c)")
        void commaArgs() {
            assertTypes(tokenize("f(a, b, c)"),
                    IDENTIFIER, LEFT_PAREN,
                    IDENTIFIER, COMMA,
                    IDENTIFIER, COMMA,
                    IDENTIFIER, RIGHT_PAREN, EOF);
        }
    }


    // =========================================================================
    // 15. UNKNOWN / ILLEGAL CHARACTERS
    // =========================================================================

    @Nested
    @DisplayName("15. Unknown / illegal characters")
    class UnknownCharTests {

        @ParameterizedTest(name = "''{0}'' → BloopLexerException")
        @ValueSource(strings = {"@", "#", "$", "%", "^", "&", "~", "`", "?", ";", "|", "\\"})
        @DisplayName("each illegal character throws BloopLexerException")
        void illegalCharsThrow(String ch) {
            assertThrows(BloopLexerException.class, () -> tokenize(ch));
        }

        @Test @DisplayName("lone '.' → BloopLexerException")
        void loneDot() {
            assertThrows(BloopLexerException.class, () -> tokenize("."));
        }

        @Test @DisplayName("exception message contains the offending character")
        void msgContainsChar() {
            BloopLexerException ex =
                    assertThrows(BloopLexerException.class, () -> tokenize("@"));
            assertTrue(ex.getMessage().contains("@"));
        }

        @Test @DisplayName("illegal char after valid tokens still throws")
        void afterValidTokens() {
            assertThrows(BloopLexerException.class, () -> tokenize("x + @"));
        }
    }


    // =========================================================================
    // 16. INDENTATION — INDENT
    // =========================================================================

    @Nested
    @DisplayName("16. INDENT token")
    class IndentTests {

        @Test @DisplayName("4-space indent after newline → INDENT emitted")
        void fourSpaceIndent() {
            assertTrue(has(tokenize("a\n    b"), INDENT));
        }

        @Test @DisplayName("INDENT value is empty string")
        void indentValue() {
            tokenize("a\n    b").stream()
                    .filter(t -> t.type() == INDENT)
                    .forEach(t -> assertEquals("", t.value()));
        }

        @Test @DisplayName("INDENT appears after NEWLINE: IDENTIFIER NEWLINE INDENT IDENTIFIER  DEDENT, EOF")
        void indentAfterNewline() {
            assertTypes(tokenize("a\n    b"), IDENTIFIER, NEWLINE, INDENT, IDENTIFIER, DEDENT, EOF);
        }

        @Test @DisplayName("first line — leading spaces never produce INDENT")
        void noIndentFirstLine() {
            assertFalse(has(tokenize("    x"), INDENT));
        }

        @Test @DisplayName("same indentation level — no INDENT emitted")
        void sameLevel() {
            assertFalse(has(tokenize("a\nb"), INDENT));
        }
    }


    // =========================================================================
    // 17. INDENTATION — DEDENT
    // =========================================================================

    @Nested
    @DisplayName("17. DEDENT token")
    class DedentTests {

        @Test @DisplayName("return to base level → DEDENT emitted")
        void dedentToBase() {
            assertTrue(has(tokenize("a\n    b\nc"), DEDENT));
        }

        @Test @DisplayName("DEDENT value is empty string")
        void dedentValue() {
            tokenize("a\n    b\nc").stream()
                    .filter(t -> t.type() == DEDENT)
                    .forEach(t -> assertEquals("", t.value()));
        }

        @Test @DisplayName("unclosed block at EOF → DEDENT auto-emitted")
        void dedentAtEof() {
            assertTrue(has(tokenize("a\n    b"), DEDENT));
        }

        @Test @DisplayName("INDENT and DEDENT counts are always equal")
        void balanced() {
            for (String src : new String[]{
                    "a\n    b",
                    "a\n    b\nc",
                    "a\n    b\n        c\n    d\ne"
            }) {
                List<Token> tokens = tokenize(src);
                assertEquals(countOf(tokens, INDENT), countOf(tokens, DEDENT),
                        "Unbalanced for: " + src);
            }
        }
    }


    // =========================================================================
    // 18. INDENTATION — NESTED BLOCKS
    // =========================================================================

    @Nested
    @DisplayName("18. Nested indentation")
    class NestedIndentTests {

        @Test @DisplayName("2-level nesting → 2 INDENTs, 2 DEDENTs")
        void twoLevel() {
            List<Token> t = tokenize("a\n    b\n        c\n    d\ne");
            assertEquals(2, countOf(t, INDENT));
            assertEquals(2, countOf(t, DEDENT));
        }

        @Test @DisplayName("3-level nesting → 3 INDENTs, 3 DEDENTs")
        void threeLevel() {
            List<Token> t = tokenize("a\n    b\n        c\n            d\ne");
            assertEquals(3, countOf(t, INDENT));
            assertEquals(3, countOf(t, DEDENT));
        }

        @Test @DisplayName("user guide nested repeat → 2 INDENTs, 2 DEDENTs")
        void userGuideNestedRepeat() {
            String src =
                    """
                            repeat 3 times:
                                repeat 2 times:
                                    print "inner"
                                print "outer\"""";
            List<Token> tokens = tokenize(src);
            assertEquals(2, countOf(tokens, INDENT));
            assertEquals(2, countOf(tokens, DEDENT));
        }

        @Test @DisplayName("nested if-else parses without error")
        void userGuideNestedIf() {
            String src =
                    """
                            put 85 into marks
                            if marks >= 90 then:
                                print "Grade A"
                            else:
                                if marks >= 75 then:
                                    print "Grade B"
                                else:
                                    print "Grade C\"""";
            assertDoesNotThrow(() -> tokenize(src));
        }

        @Test @DisplayName("multi-level dedent at once emits multiple DEDENTs")
        void multiLevelDedentAtOnce() {
            // 3 levels in, then straight to base
            List<Token> t = tokenize("a\n    b\n        c\n            d\ne");
            assertEquals(3, countOf(t, DEDENT));
        }
    }


    // =========================================================================
    // 19. INDENTATION — TAB EQUIVALENCE (1 tab = 4 spaces)
    // =========================================================================

    @Nested
    @DisplayName("19. Tab indentation (1 tab = 4 spaces)")
    class TabIndentTests {

        @Test @DisplayName("single tab indent → INDENT emitted")
        void singleTab() {
            assertTrue(has(tokenize("a\n\tb"), INDENT));
        }

        @Test @DisplayName("tab produces same structure as 4 spaces")
        void tabEqualsSpaces() {
            List<Token> tab   = tokenize("a\n\tb\nc");
            List<Token> space = tokenize("a\n    b\nc");
            assertEquals(countOf(tab, INDENT),  countOf(space, INDENT));
            assertEquals(countOf(tab, DEDENT),  countOf(space, DEDENT));
            assertEquals(countOf(tab, NEWLINE), countOf(space, NEWLINE));
        }

        @Test @DisplayName("two tabs = 8-space indent level — nested structure OK")
        void twoTabs() {
            List<Token> t = tokenize("a\n\tb\n\t\tc\n\td\ne");
            assertEquals(2, countOf(t, INDENT));
            assertEquals(2, countOf(t, DEDENT));
        }
    }


    // =========================================================================
    // 20. INDENTATION — ERROR CASES
    // =========================================================================

    @Nested
    @DisplayName("20. Indentation errors")
    class IndentErrorTests {

        @Test @DisplayName("dedent to level never opened → BloopLexerException")
        void dedentUnknownLevel() {
            // opened at 4, dedent to 2 (never opened)
            assertThrows(BloopLexerException.class, () -> tokenize("a\n    b\n  c"));
        }

        @Test @DisplayName("user guide invalid indent example → BloopLexerException")
        void userGuideInvalidExample() {
            String src =
                    """
                            if age >= 18 then:
                                print "adult"
                              print "oops\"""";        // 2 spaces — was never opened
            assertThrows(BloopLexerException.class, () -> tokenize(src));
        }

        @Test @DisplayName("exception message mentions 'indent' (case-insensitive)")
        void msgMentionsIndent() {
            BloopLexerException ex = assertThrows(BloopLexerException.class,
                    () -> tokenize("a\n    b\n  c"));
            assertTrue(ex.getMessage().toLowerCase().contains("indent"));
        }

        @Test @DisplayName("dedent to level 3 when only 0 and 4 were opened → error")
        void dedentToThreeSpaces() {
            assertThrows(BloopLexerException.class, () -> tokenize("a\n    b\n   c"));
        }
    }


    // =========================================================================
    // 21. LINE-NUMBER TRACKING
    // =========================================================================

    @Nested
    @DisplayName("21. Line-number tracking")
    class LineNumberTests {

        @Test @DisplayName("single-line source — all tokens on line 1")
        void allOnLine1() {
            tokenize("put 1 into x").forEach(t ->
                    assertEquals(1, t.line(), "Token not on line 1: " + t));
        }

        @Test @DisplayName("identifier on line 2 has line = 2")
        void line2() {
            assertEquals(2, tokenize("a\nb").get(2).line()); // 'b'
        }

        @Test @DisplayName("identifier on line 3 has line = 3")
        void line3() {
            // a NEWLINE b NEWLINE c EOF
            assertEquals(3, tokenize("a\nb\nc").get(4).line()); // 'c'
        }

        @Test @DisplayName("NEWLINE token carries line number of ended line")
        void newlineLineNum() {
            Token nl = tokenize("a\nb").get(1);
            assertEquals(NEWLINE, nl.type());
            assertEquals(1, nl.line());
        }

        @Test @DisplayName("EOF line number equals last source line")
        void eofLineNum() {
            assertEquals(2, lastToken(tokenize("a\nb")).line());
        }

        @Test @DisplayName("string token on line 2 has line = 2")
        void stringLine2() {
            Token str = tokenize("a\n\"hi\"").stream()
                    .filter(t -> t.type() == STRING).findFirst().orElseThrow();
            assertEquals(2, str.line());
        }
    }


    // =========================================================================
    // 22. IMMUTABILITY
    // =========================================================================

    @Nested
    @DisplayName("22. Result list immutability")
    class ImmutabilityTests {

        @Test @DisplayName("add() on result → UnsupportedOperationException")
        void addThrows() {
            assertThrows(UnsupportedOperationException.class,
                    () -> tokenize("x").add(new Token(EOF, "", 1)));
        }

        @Test @DisplayName("remove() on result → UnsupportedOperationException")
        void removeThrows() {
            assertThrows(UnsupportedOperationException.class,
                    () -> tokenize("x").removeFirst());
        }
    }


    // =========================================================================
    // 23. FULL BLOOP PROGRAMS
    // =========================================================================

    @Nested
    @DisplayName("23. Full program integration")
    class IntegrationTests {

        @Test @DisplayName("Hello World: print \"Hello, World!\"")
        void helloWorld() {
            List<Token> t = tokenize("print \"Hello, World!\"");
            assertTypes(t, PRINT, STRING, EOF);
            assertEquals("Hello, World!", t.get(1).value());
        }

        @Test @DisplayName("put integer: put 42 into age")
        void putInt() {
            assertTypes(tokenize("put 42 into age"), PUT, NUMBER, INTO, IDENTIFIER, EOF);
        }

        @Test @DisplayName("put float: put 3.14 into pi")
        void putFloat() {
            List<Token> t = tokenize("put 3.14 into pi");
            assertTypes(t, PUT, NUMBER, INTO, IDENTIFIER, EOF);
            assertEquals("3.14", t.get(1).value());
        }

        @Test @DisplayName("put string: put \"Alice\" into name")
        void putString() {
            List<Token> t = tokenize("put \"Alice\" into name");
            assertTypes(t, PUT, STRING, INTO, IDENTIFIER, EOF);
            assertEquals("Alice", t.get(1).value());
        }

        @Test @DisplayName("put identifier: put age into backup")
        void putIdentifier() {
            assertTypes(tokenize("put age into backup"), PUT, IDENTIFIER, INTO, IDENTIFIER, EOF);
        }

        @Test @DisplayName("arithmetic: put a + b into sum")
        void arithmetic() {
            assertTypes(tokenize("put a + b into sum"),
                    PUT, IDENTIFIER, PLUS, IDENTIFIER, INTO, IDENTIFIER, EOF);
        }

        @Test @DisplayName("grouping: put (2 + 3) * 4 into result")
        void grouping() {
            assertTypes(tokenize("put (2 + 3) * 4 into result"),
                    PUT, LEFT_PAREN, NUMBER, PLUS, NUMBER, RIGHT_PAREN,
                    STAR, NUMBER, INTO, IDENTIFIER, EOF);
        }

        @Test
        @DisplayName("if-then (no else) — full token structure")
        void ifThen() {
            String src = "if age >= 18 then:\n    print \"You are an adult\"";
            List<Token> t = tokenize(src);

            assertEquals(IF,      t.get(0).type());
            assertEquals(THEN,    t.get(4).type());
            assertEquals(COLON,   t.get(5).type());
            assertEquals(NEWLINE, t.get(6).type());
            assertEquals(INDENT,  t.get(7).type());
            assertEquals(PRINT,   t.get(8).type());
            assertEquals(STRING,  t.get(9).type());

            assertTrue(has(t, DEDENT));
        }
        @Test @DisplayName("if-then-else block")
        void ifThenElse() {
            String src =
                    "if age >= 18 then:\n" +
                            "    print \"adult\"\n" +
                            "else:\n" +
                            "    print \"minor\"";
            List<Token> t = tokenize(src);
            assertTrue(has(t, IF));
            assertTrue(has(t, THEN));
            assertTrue(has(t, ELSE));
            assertEquals(EOF, lastToken(t).type());
        }

        @Test @DisplayName("repeat-times block")
        void repeatTimes() {
            String src = "repeat 5 times:\n    print \"Hello!\"";
            List<Token> t = tokenize(src);
            assertEquals(REPEAT, t.getFirst().type());
            assertEquals(NUMBER, t.get(1).type());
            assertEquals(TIMES,  t.get(2).type());
            assertEquals(COLON,  t.get(3).type());
            assertTrue(has(t, INDENT));
            assertTrue(has(t, DEDENT));
        }

        @Test @DisplayName("repeat with variable: repeat count times:")
        void repeatVariable() {
            assertTypes(tokenize("repeat count times:"), REPEAT, IDENTIFIER, TIMES, COLON, EOF);
        }

        @Test @DisplayName("multi-statement program — correct keyword/NEWLINE counts")
        void multiStatement() {
            String src =
                    "put 10 into x\n" +
                            "put 20 into y\n" +
                            "put x + y into z\n" +
                            "print z";
            List<Token> t = tokenize(src);
            assertEquals(3, countOf(t, PUT));
            assertEquals(3, countOf(t, INTO));
            assertEquals(1, countOf(t, PRINT));
            assertEquals(3, countOf(t, NEWLINE));
        }

        @Test @DisplayName("multi-line program — indent/dedent structurally correct")
        void multiLineStructure() {
            String src =
                    "put 10 into x\n" +
                            "if x > 5 then:\n" +
                            "    print \"big\"\n" +
                            "print \"done\"";
            List<Token> t = tokenize(src);
            assertEquals(1, countOf(t, INDENT));
            assertEquals(1, countOf(t, DEDENT));
            assertEquals(EOF, lastToken(t).type());
        }

        @Test @DisplayName("print with \\n escape: \"Hello\\nWorld\"")
        void printNewlineEscape() {
            List<Token> t = tokenize("print \"Hello\\nWorld\"");
            assertTypes(t, PRINT, STRING, EOF);
            assertEquals("Hello\nWorld", t.get(1).value());
        }

        @Test @DisplayName("print with \\t escape: \"Name:\\tAlice\"")
        void printTabEscape() {
            assertEquals("Name:\tAlice", tokenize("print \"Name:\\tAlice\"").get(1).value());
        }

        @Test @DisplayName("INDENT/DEDENT always balanced across valid programs")
        void alwaysBalanced() {
            String[] programs = {
                    "a\n    b",
                    "a\n    b\nc",
                    "a\n    b\n    c\nd",
                    "a\n    b\n        c\n    d\ne",
                    "repeat 3 times:\n    print \"hi\""
            };
            for (String src : programs) {
                List<Token> t = tokenize(src);
                assertEquals(countOf(t, INDENT), countOf(t, DEDENT),
                        "Unbalanced for: " + src);
            }
        }

        @Test @DisplayName("comparison operators in if condition")
        void comparisonInIf() {
            String src = "if x != y then:\n    print \"different\"";
            List<Token> t = tokenize(src);
            assertEquals(IF,       t.getFirst().type());
            assertEquals(NOT_EQUAL,t.get(2).type());
        }

        @Test @DisplayName("full program — EOF is always last, regardless of trailing newline")
        void eofAlwaysLast() {
            String[] programs = {
                    "print \"hi\"",
                    "print \"hi\"\n",
                    "a\n    b\n",
                    ""
            };
            for (String src : programs) {
                assertEquals(EOF, lastToken(tokenize(src)).type(),
                        "EOF not last for: " + src.replace("\n", "\\n"));
            }
        }
    }
}