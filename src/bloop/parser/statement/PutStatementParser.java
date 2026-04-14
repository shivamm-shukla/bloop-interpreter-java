package bloop.parser.statement;

import bloop.ast.Expression;
import bloop.instruction.AssignInstruction;
import bloop.instruction.Instruction;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.token.Token;
import bloop.token.TokenType;


public final class PutStatementParser implements StatementParser {

    private final ExpressionParser expressionParser;

    public PutStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public TokenType triggerToken() {
        return TokenType.PUT;
    }

    @Override
    public Instruction parse(TokenCursor cursor, BlockParser blockParser) {
        cursor.expect(TokenType.PUT, "Expected 'put'");

        Expression valueExpression = expressionParser.parse(cursor);

        cursor.expect(TokenType.INTO, "Expected 'into' after the value expression");

        Token variableNameToken = cursor.expect(
                TokenType.IDENTIFIER,
                "Expected a variable name after 'into'");

        cursor.consumeNewlineIfPresent();

        return new AssignInstruction(variableNameToken.value(), valueExpression);
    }
}
