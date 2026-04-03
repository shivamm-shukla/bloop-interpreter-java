package bloop.parser;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.instructions.*;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int currentIndex = 0;


    // Constructor

    public Parser(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            throw new BloopParseException("Token list cannot be null or empty", 0);
        }
        this.tokens = tokens;
    }


    // Entry Point

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();

        while (!hasReachedEnd()) {
            skipBlankLines();
            if (hasReachedEnd()) break;
            instructions.add(parseInstruction());
        }

        if (!hasReachedEnd()) {
            throw createError(currentToken(),
                    "Unexpected token '" + currentToken().getValue() + "' at end of input");
        }

        return instructions;
    }


    // Instruction Parsing

    private Instruction parseInstruction() {
        Token token = currentToken();

        switch (token.getType()) {
            case PUT:    return parsePutInstruction();
            case PRINT:  return parsePrintInstruction();
            case IF:     return parseIfInstruction();
            case REPEAT: return parseRepeatInstruction();
            default:
                throw createError(token, "Unexpected token '" + token.getValue() + "'");
        }
    }

    private Instruction parsePutInstruction() {
        consumeExpected(TokenType.PUT, "Expected 'put'");

        Expression valueExpression = parseExpression();

        consumeExpected(TokenType.INTO, "Expected 'into' after expression");

        Token variableToken = consumeExpected(TokenType.IDENTIFIER, "Expected variable name after 'into'");

        consumeNewlineIfPresent();

        return new AssignInstruction(variableToken.getValue(), valueExpression);
    }

    private Instruction parsePrintInstruction() {
        Token printToken = consumeExpected(TokenType.PRINT, "Expected 'print'");

        if (hasReachedEnd() || isCurrentToken(TokenType.NEWLINE)) {
            throw createError(printToken, "Expected expression after 'print'");
        }

        Expression expression = parseExpression();
        consumeNewlineIfPresent();

        return new PrintInstruction(expression);
    }

    private Instruction parseIfInstruction() {
        Token ifToken = consumeExpected(TokenType.IF, "Expected 'if'");

        Expression condition = parseExpression();

        consumeExpected(TokenType.THEN, "Expected 'then' after condition");
        consumeExpected(TokenType.COLON, "Expected ':' after 'then'");

        consumeNewlineIfPresent();

        List<Instruction> body = parseIndentedBlock();

        if (body.isEmpty()) {
            throw createError(ifToken, "Expected at least one instruction in 'if' body");
        }

        return new IfInstruction(condition, body);
    }

    private Instruction parseRepeatInstruction() {
        Token repeatToken = consumeExpected(TokenType.REPEAT, "Expected 'repeat'");

        Token numberToken = consumeExpected(TokenType.NUMBER, "Expected a number after 'repeat'");
        int repeatCount = parseAndValidateRepeatCount(numberToken);

        consumeExpected(TokenType.TIMES, "Expected 'times' after repeat count");
        consumeExpected(TokenType.COLON, "Expected ':' after 'times'");

        consumeNewlineIfPresent();

        List<Instruction> body = parseIndentedBlock();

        if (body.isEmpty()) {
            throw createError(repeatToken, "Expected at least one instruction in 'repeat' body");
        }

        return new RepeatInstruction(repeatCount, body);
    }


    // Block Parsing

    private List<Instruction> parseIndentedBlock() {
        List<Instruction> instructions = new ArrayList<>();

        consumeExpected(TokenType.INDENT, "Expected indented block after ':'");

        while (!hasReachedEnd() && !isCurrentToken(TokenType.DEDENT)) {
            skipBlankLines();
            if (!hasReachedEnd() && !isCurrentToken(TokenType.DEDENT)) {
                instructions.add(parseInstruction());
            }
        }

        if (!hasReachedEnd()) {
            consumeExpected(TokenType.DEDENT, "Expected end of block");
        }

        return instructions;
    }


    // Expression Parsing

    // Layer 1: Comparisons & Equality (Lowest precedence)
    // 'if' use kiya hai 'while' ki jagah — chained comparisons blocked
    private Expression parseExpression() {
        Expression left = parseAddition();

        if (matchAndConsume(
                TokenType.GREATER, TokenType.LESS,
                TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
                TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL
        )) {
            String operator = lastConsumedToken().getValue();
            Expression right = parseAddition();
            return new BinaryOpNode(left, operator, right);
        }

        return left;
    }

    // Layer 2: Addition & Subtraction
    private Expression parseAddition() {
        Expression left = parseTerm();

        while (matchAndConsume(TokenType.PLUS, TokenType.MINUS)) {
            String operator = lastConsumedToken().getValue();
            Expression right = parseTerm();
            left = new BinaryOpNode(left, operator, right);
        }

        return left;
    }

    // Layer 3: Multiplication & Division
    private Expression parseTerm() {
        Expression left = parsePrimary();

        while (matchAndConsume(TokenType.STAR, TokenType.SLASH)) {
            String operator = lastConsumedToken().getValue();
            Expression right = parsePrimary();
            left = new BinaryOpNode(left, operator, right);
        }

        return left;
    }

    // Layer 4: Base Values
    private Expression parsePrimary() {

        // Parenthesized expressions — (2 + 3) * 4
        if (isCurrentToken(TokenType.LPAREN)) {
            consumeToken();
            Expression expr = parseExpression();
            consumeExpected(TokenType.RPAREN, "Expected ')' to close '('");
            return expr;
        }

        if (isCurrentToken(TokenType.NUMBER)) {
            Token t = consumeToken();
            return parseNumberLiteral(t);
        }

        if (isCurrentToken(TokenType.STRING)) {
            Token t = consumeToken();
            return new StringNode(t.getValue());
        }

        if (isCurrentToken(TokenType.IDENTIFIER)) {
            Token t = consumeToken();
            return new VariableNode(t.getValue());
        }

        Token token = currentToken();
        throw createError(token,
                "Expected a number, string, or variable but got '" + token.getValue() + "'");
    }


    // Helpers

    private Expression parseNumberLiteral(Token token) {
        try {
            double value = Double.parseDouble(token.getValue());
            return new NumberNode(value);
        } catch (NumberFormatException e) {
            throw createError(token, "Invalid number format: '" + token.getValue() + "'");
        }
    }

    private int parseAndValidateRepeatCount(Token token) {
        try {

            long value = Long.parseLong(token.getValue());

            if (value < 0) {
                throw createError(token,
                        "Repeat count cannot be negative: '" + token.getValue() + "'");
            }

            if (value > Integer.MAX_VALUE) {
                throw createError(token,
                        "Repeat count is too large to execute: '" + token.getValue() + "'");
            }

            return (int) value; // safe — upar dono checks ho chuke hain

        } catch (NumberFormatException e) {
            // Decimals, floats, garbage
            throw createError(token,
                    "Repeat count must be a whole number: '" + token.getValue() + "'");
        }
    }

    private void consumeNewlineIfPresent() {
        if (isCurrentToken(TokenType.NEWLINE)) {
            consumeToken();
        }
    }

    private void skipBlankLines() {
        while (!hasReachedEnd() && isCurrentToken(TokenType.NEWLINE)) {
            consumeToken();
        }
    }


    // Token Utilities

    private boolean matchAndConsume(TokenType... types) {
        for (TokenType type : types) {
            if (isCurrentToken(type)) {
                consumeToken();
                return true;
            }
        }
        return false;
    }

    private Token consumeToken() {
        if (!hasReachedEnd()) currentIndex++;
        return lastConsumedToken();
    }

    private Token consumeExpected(TokenType type, String message) {
        if (isCurrentToken(type)) return consumeToken();
        throw createError(currentToken(), message);
    }

    private boolean isCurrentToken(TokenType type) {
        if (hasReachedEnd()) return false;
        return currentToken().getType() == type;
    }

    private boolean hasReachedEnd() {
        return currentToken().getType() == TokenType.EOF;
    }

    private Token currentToken() {
        if (currentIndex >= tokens.size()) {
            return new Token(TokenType.EOF, "EOF", -1);
        }
        return tokens.get(currentIndex);
    }

    private Token lastConsumedToken() {
        if (currentIndex == 0) {
            throw new BloopParseException("Internal parser error: no token has been consumed yet", -1);
        }
        return tokens.get(currentIndex - 1);
    }

    private BloopParseException createError(Token token, String message) {
        return new BloopParseException(message, token.getLine());
    }
}