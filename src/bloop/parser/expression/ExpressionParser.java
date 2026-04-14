package bloop.parser.expression;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.parser.cursor.TokenCursor;
import bloop.token.Token;
import bloop.token.TokenType;


public final class ExpressionParser {

    // Public entry point

    public Expression parse(TokenCursor cursor) {
        return parseComparison(cursor);
    }

    // Layer 1: Comparison (lowest precedence, intentionally non-chaining)

    private Expression parseComparison(TokenCursor cursor) {
        Expression leftSide = parseAddition(cursor);

        // tryConsume returns the matched token directly
        Token operatorToken = cursor.tryConsume(
                TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL,
                TokenType.GREATER,     TokenType.GREATER_EQUAL,
                TokenType.LESS,        TokenType.LESS_EQUAL
        );

        if (operatorToken != null) {
            Expression rightSide = parseAddition(cursor);
            return new BinaryOpNode(leftSide, operatorToken.value(), rightSide);
        }

        return leftSide;
    }

    // Layer 2: Addition and Subtraction

    private Expression parseAddition(TokenCursor cursor) {
        Expression result = parseMultiplication(cursor);

        Token operatorToken;
        while ((operatorToken = cursor.tryConsume(TokenType.PLUS, TokenType.MINUS)) != null) {
            Expression rightSide = parseMultiplication(cursor);
            result = new BinaryOpNode(result, operatorToken.value(), rightSide);
        }

        return result;
    }

    // Layer 3: Multiplication and Division

    private Expression parseMultiplication(TokenCursor cursor) {
        Expression result = parsePrimary(cursor);

        Token operatorToken;
        while ((operatorToken = cursor.tryConsume(TokenType.STAR, TokenType.SLASH)) != null) {
            Expression rightSide = parsePrimary(cursor);
            result = new BinaryOpNode(result, operatorToken.value(), rightSide);
        }

        return result;
    }

    // Layer 4: Primary (highest precedence)

    private Expression parsePrimary(TokenCursor cursor) {

        // Unary minus: -5  -(a + b)
        if (cursor.check(TokenType.MINUS)) {
            cursor.consume();
            Expression operand = parsePrimary(cursor);
            return new BinaryOpNode(new NumberNode(0), "-", operand);
        }

        // Parenthesised expression: (expr)
        if (cursor.check(TokenType.LEFT_PAREN)) {
            cursor.consume(); // consume '('
            Expression innerExpression = parse(cursor);
            cursor.expect(TokenType.RIGHT_PAREN, "Expected ')' to close the opening '('");
            return innerExpression;
        }

        // Numeric literal: 42  3.14
        if (cursor.check(TokenType.NUMBER)) {
            Token numberToken = cursor.consume();
            return parseNumericLiteral(numberToken);
        }

        // String literal: "hello"
        if (cursor.check(TokenType.STRING)) {
            Token stringToken = cursor.consume();
            return new StringNode(stringToken.value());
        }

        // Variable reference: x  total  score
        if (cursor.check(TokenType.IDENTIFIER)) {
            Token identifierToken = cursor.consume();
            return new VariableNode(identifierToken.value());
        }

        // Nothing matched — unexpected token in expression position
        Token unexpectedToken = cursor.current();
        throw new BloopParseException(
                "Expected a number, string, variable, or '(' but got '" +
                        unexpectedToken.value() + "'",
                unexpectedToken.line());
    }

    // helpers

    private Expression parseNumericLiteral(Token token) {
        try {
            return new NumberNode(Double.parseDouble(token.value()));
        } catch (NumberFormatException e) {
            throw new BloopParseException(
                    "Invalid number literal: '" + token.value() + "'",
                    token.line());
        }
    }
}