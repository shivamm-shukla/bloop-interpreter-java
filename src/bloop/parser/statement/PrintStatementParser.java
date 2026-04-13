package bloop.parser.statement;

import bloop.ast.Expression;
import bloop.exceptions.BloopParseException;
import bloop.instruction.Instruction;
import bloop.instruction.PrintInstruction;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.token.Token;
import bloop.token.TokenType;


public final class PrintStatementParser implements StatementParser {

    private final ExpressionParser expressionParser;

    public PrintStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public TokenType triggerToken() {
        return TokenType.PRINT;
    }

    @Override
    public Instruction parse(TokenCursor cursor, BlockParser blockParser) {
        Token printToken = cursor.expect(TokenType.PRINT, "Expected 'print'");

        // Catch "print" with nothing after it before attempting expression parse
        if (cursor.isAtEnd() || cursor.check(TokenType.NEWLINE)) {
            throw new BloopParseException(
                    "Expected an expression after 'print'",
                    printToken.line());
        }

        Expression expressionToPrint = expressionParser.parse(cursor);
        cursor.consumeNewlineIfPresent();

        return new PrintInstruction(expressionToPrint);
    }
}
