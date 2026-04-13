package bloop.parser.statement;

import bloop.exceptions.BloopParseException;
import bloop.instruction.*;
import bloop.parser.cursor.TokenCursor;
import bloop.token.Token;
import bloop.token.TokenType;

import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit › StatementParserRegistry")
class StatementParserRegistryTest {

    //  Fake implementation
    private static class FakeParser implements StatementParser {
        private final TokenType type;

        FakeParser(TokenType type) {
            this.type = type;
        }

        @Override
        public TokenType triggerToken() {
            return type;
        }

        @Override
        public Instruction parse(TokenCursor cursor, BlockParser blockParser) {
            return null;
        }
    }

    private StatementParser parserFor(TokenType type) {
        return new FakeParser(type);
    }

    private Token tokenOf(TokenType type, String value, int line) {
        return new Token(type, value, line);
    }

    @Test
    @DisplayName("Empty registry → canHandle returns false for all types")
    void emptyRegistry() {
        StatementParserRegistry reg = new StatementParserRegistry(Collections.emptyList());
        for (TokenType t : TokenType.values()) {
            assertFalse(reg.canHandle(t));
        }
    }

    @Test
    @DisplayName("Registered parser can be resolved")
    void resolveRegisteredParser() {
        StatementParser sp = parserFor(TokenType.IF);
        StatementParserRegistry reg = new StatementParserRegistry(List.of(sp));
        Token t = tokenOf(TokenType.IF, "if", 1);
        assertSame(sp, reg.resolve(t));
    }

    @Test
    @DisplayName("canHandle() true for registered, false for unregistered")
    void canHandle() {
        StatementParserRegistry reg = new StatementParserRegistry(List.of(parserFor(TokenType.IF)));
        assertTrue(reg.canHandle(TokenType.IF));
        assertFalse(reg.canHandle(TokenType.PRINT));
    }

    @Test
    @DisplayName("resolve() on unregistered token throws BloopParseException")
    void resolveUnregistered() {
        StatementParserRegistry reg = new StatementParserRegistry(Collections.emptyList());
        Token t = tokenOf(TokenType.PRINT, "print", 3);
        assertThrows(BloopParseException.class, () -> reg.resolve(t));
    }

    @Test
    @DisplayName("Exception from resolve() contains the token value")
    void resolveExceptionContainsValue() {
        StatementParserRegistry reg = new StatementParserRegistry(Collections.emptyList());
        Token t = tokenOf(TokenType.PRINT, "print", 1);
        BloopParseException ex = assertThrows(BloopParseException.class, () -> reg.resolve(t));
        assertTrue(ex.getMessage().contains("print"));
    }

    @Test
    @DisplayName("Exception carries correct line number")
    void resolveExceptionLine() {
        StatementParserRegistry reg = new StatementParserRegistry(Collections.emptyList());
        Token t = tokenOf(TokenType.PRINT, "print", 99);
        BloopParseException ex = assertThrows(BloopParseException.class, () -> reg.resolve(t));
        assertEquals(99, ex.getSourceLine());
    }

    @Test
    @DisplayName("Duplicate trigger tokens — last registration wins")
    void duplicateLastWins() {
        StatementParser first = parserFor(TokenType.IF);
        StatementParser second = parserFor(TokenType.IF);
        StatementParserRegistry reg = new StatementParserRegistry(List.of(first, second));
        Token t = tokenOf(TokenType.IF, "if", 1);
        assertSame(second, reg.resolve(t));
    }

    @Test
    @DisplayName("canHandle() consistent with resolve(): canHandle true ↔ no exception")
    void consistency() {
        StatementParser sp = parserFor(TokenType.PUT);
        StatementParserRegistry reg = new StatementParserRegistry(List.of(sp));
        for (TokenType type : TokenType.values()) {
            Token t = tokenOf(type, type.name().toLowerCase(), 1);
            if (reg.canHandle(type)) {
                assertDoesNotThrow(() -> reg.resolve(t));
            } else {
                assertThrows(BloopParseException.class, () -> reg.resolve(t));
            }
        }
    }

    @Test
    @DisplayName("Mutating source list after construction does not affect registry")
    void sourceListMutationIsolated() {
        StatementParser sp = parserFor(TokenType.IF);
        java.util.List<StatementParser> mutable = new java.util.ArrayList<>();
        mutable.add(sp);
        StatementParserRegistry reg = new StatementParserRegistry(mutable);

        mutable.add(parserFor(TokenType.PRINT));   // mutate after construction

        assertFalse(reg.canHandle(TokenType.PRINT),
                "Registry should not reflect post-construction mutations");
    }
}