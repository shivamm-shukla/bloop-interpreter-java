package bloop.parser.statement;

import bloop.ast.Expression;
import bloop.exceptions.BloopParseException;
import bloop.instruction.IfInstruction;
import bloop.instruction.Instruction;
import bloop.parser.cursor.TokenCursor;
import bloop.parser.expression.ExpressionParser;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.List;


public final class IfStatementParser implements StatementParser {

    private final ExpressionParser expressionParser;

    public IfStatementParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public TokenType triggerToken() {
        return TokenType.IF;
    }

    @Override
    public Instruction parse(TokenCursor cursor, BlockParser blockParser) {
        Token ifToken = cursor.expect(TokenType.IF, "Expected 'if'");

        Expression conditionExpression = expressionParser.parse(cursor);

        cursor.expect(TokenType.THEN,  "Expected 'then' after the condition");
        cursor.expect(TokenType.COLON, "Expected ':' after 'then'");
        cursor.consumeNewlineIfPresent();

        List<Instruction> thenBody = blockParser.parseBlock();

        if (thenBody.isEmpty()) {
            throw new BloopParseException(
                    "The 'if' body must contain at least one instruction",
                    ifToken.line());
        }

        // After then-block, skip any blank lines before checking for else
        cursor.skipNewlines();

        // Optional else block
        List<Instruction> elseBody = tryParseElseBlock(cursor, blockParser);

        return new IfInstruction(conditionExpression, thenBody, elseBody);
    }


    // Returns an empty list if no else block is present.
    private List<Instruction> tryParseElseBlock(TokenCursor cursor, BlockParser blockParser) {

        if (cursor.check(TokenType.ELSE)) {

            cursor.consume(); // consume 'else'
            cursor.expect(TokenType.COLON, "Expected ':' after 'else'");
            cursor.consumeNewlineIfPresent();

            return blockParser.parseBlock();
        }
        return List.of();
    }
}