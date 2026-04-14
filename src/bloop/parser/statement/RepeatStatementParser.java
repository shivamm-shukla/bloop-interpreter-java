package bloop.parser.statement;

import bloop.ast.Expression;
import bloop.exceptions.BloopParseException;
import bloop.instruction.Instruction;
import bloop.instruction.RepeatInstruction;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.List;


public final class RepeatStatementParser implements StatementParser {

    private final ExpressionParser expressionParser;

    public RepeatStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public TokenType triggerToken() {
        return TokenType.REPEAT;
    }

    @Override
    public Instruction parse(TokenCursor cursor, BlockParser blockParser) {
        Token repeatToken = cursor.expect(TokenType.REPEAT, "Expected 'repeat'");

        // Now accepts any expression: literal number OR variable
        Expression countExpression = expressionParser.parse(cursor);

        cursor.expect(TokenType.TIMES, "Expected 'times' after the repeat count");
        cursor.expect(TokenType.COLON, "Expected ':' after 'times'");
        cursor.consumeNewlineIfPresent();

        List<Instruction> body = blockParser.parseBlock();

        if (body.isEmpty()) {
            throw new BloopParseException(
                    "The 'repeat' body must contain at least one instruction",
                    repeatToken.line());
        }

        return new RepeatInstruction(countExpression, body);
    }
}