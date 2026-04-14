package bloop.parser;

import bloop.exceptions.BloopParseException;
import bloop.instruction.Instruction;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.parser.statement.*;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.ArrayList;
import java.util.List;


public final class Parser {

    private final StatementParserRegistry statementRegistry;

    // Construction

    public static Parser createDefault() {
        ExpressionParser sharedExpressionParser = new ExpressionParser();

        List<StatementParser> builtInParsers = List.of(
                new PutStatementParser(sharedExpressionParser),
                new PrintStatementParser(sharedExpressionParser),
                new IfStatementParser(sharedExpressionParser),
                new RepeatStatementParser(sharedExpressionParser)  // fixed: pass expressionParser
        );

        return new Parser(new StatementParserRegistry(builtInParsers));
    }


    public Parser(StatementParserRegistry statementRegistry) {
        if (statementRegistry == null) {
            throw new IllegalArgumentException("Statement registry must not be null");
        }
        this.statementRegistry = statementRegistry;
    }

    // Public entry point

    public List<Instruction> parse(List<Token> tokens) {
        if (tokens == null) {
            throw new BloopParseException("Token list must not be null", -1);
        }

        TokenCursor       cursor       = new TokenCursor(tokens);
        List<Instruction> instructions = new ArrayList<>();

        while (!cursor.isAtEnd()) {
            cursor.skipNewlines();
            if (cursor.isAtEnd()) break;
            instructions.add(parseNextStatement(cursor));
        }

        return List.copyOf(instructions);
    }

    // Statement dispatch

    private Instruction parseNextStatement(TokenCursor cursor) {
        Token currentToken = cursor.current();
        StatementParser handler = statementRegistry.resolve(currentToken);
        return handler.parse(cursor, () -> parseIndentedBlock(cursor));
    }

    // Block parsing

    private List<Instruction> parseIndentedBlock(TokenCursor cursor) {
        cursor.expect(TokenType.INDENT, "Expected an indented block after ':'");

        List<Instruction> blockInstructions = new ArrayList<>();

        while (!cursor.isAtEnd() && !cursor.check(TokenType.DEDENT)) {
            cursor.skipNewlines();
            if (!cursor.isAtEnd() && !cursor.check(TokenType.DEDENT)) {
                blockInstructions.add(parseNextStatement(cursor));
            }
        }

        if (!cursor.isAtEnd()) {
            cursor.expect(TokenType.DEDENT, "Expected end of indented block");
        }

        return blockInstructions;
    }
}