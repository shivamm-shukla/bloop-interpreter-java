package bloop.parser;

import bloop.ast.*;
import bloop.exceptions.BloopParseException;
import bloop.instructions.*;
import bloop.token.Token;
import bloop.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokenList;
    private int currentIndex = 0;

    // ════════════════════════════════════════════
    //  Constructor
    // ════════════════════════════════════════════

    public Parser(List<Token> tokenList) {
        if (tokenList == null || tokenList.isEmpty())
            throw new BloopParseException("Token list cannot be null or empty", 0);
        this.tokenList = tokenList;
    }

    // ════════════════════════════════════════════
    //  Entry Point
    // ════════════════════════════════════════════

    public List<Instruction> parse() {
        List<Instruction> instructionList = new ArrayList<>();

        while (!hasReachedEnd()) {
            skipBlankLines();
            if (hasReachedEnd()) break;
            instructionList.add(parseInstruction());
        }

        return instructionList;
    }

    // ════════════════════════════════════════════
    //  Instruction Parsers
    // ════════════════════════════════════════════

    private Instruction parseInstruction() {
        Token token = currentToken();

        switch (token.getType()) {
            case PUT:    return parsePutInstruction();
            case PRINT:  return parsePrintInstruction();
            case IF:     return parseIfInstruction();
            case REPEAT: return parseRepeatInstruction();
            default:
                throw new BloopParseException(
                        "Unexpected token '" + token.getValue() + "'",
                        token.getLine()
                );
        }
    }

    // ── put <expr> into <variable> ───────────────────────
    private Instruction parsePutInstruction() {
        Token putToken = expectAndConsume(TokenType.PUT);

        Expression valueExpression = parseExpression();

        if (!currentTokenIs(TokenType.INTO)) {
            throw new BloopParseException(
                    "Expected 'into' after expression in 'put' statement",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.INTO);

        if (!currentTokenIs(TokenType.IDENTIFIER)) {
            throw new BloopParseException(
                    "Expected variable name after 'into'",
                    currentToken().getLine()
            );
        }
        String targetVariableName = expectAndConsume(TokenType.IDENTIFIER).getValue();

        consumeNewlineIfPresent();

        return new AssignInstruction(targetVariableName, valueExpression);
    }

    // ── print <expr> ─────────────────────────────────────
    private Instruction parsePrintInstruction() {
        expectAndConsume(TokenType.PRINT);

        if (hasReachedEnd() || currentTokenIs(TokenType.NEWLINE)) {
            throw new BloopParseException(
                    "Expected expression after 'print'",
                    currentToken().getLine()
            );
        }

        Expression expressionToPrint = parseExpression();
        consumeNewlineIfPresent();

        return new PrintInstruction(expressionToPrint);
    }

    // ── if <expr> then: <block> ──────────────────────────
    private Instruction parseIfInstruction() {
        Token ifToken = expectAndConsume(TokenType.IF);

        Expression conditionExpression = parseExpression();

        if (!currentTokenIs(TokenType.THEN)) {
            throw new BloopParseException(
                    "Expected 'then' after condition in 'if' statement",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.THEN);

        if (!currentTokenIs(TokenType.COLON)) {
            throw new BloopParseException(
                    "Expected ':' after 'then'",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.COLON);

        consumeNewlineIfPresent();

        List<Instruction> bodyInstructions = parseIndentedBlock();

        if (bodyInstructions.isEmpty()) {
            throw new BloopParseException(
                    "Expected at least one instruction in 'if' body",
                    ifToken.getLine()
            );
        }

        return new IfInstruction(conditionExpression, bodyInstructions);
    }

    // ── repeat <n> times: <block> ────────────────────────
    private Instruction parseRepeatInstruction() {
        Token repeatToken = expectAndConsume(TokenType.REPEAT);

        if (!currentTokenIs(TokenType.NUMBER)) {
            throw new BloopParseException(
                    "Expected a number after 'repeat'",
                    currentToken().getLine()
            );
        }

        Token repeatCountToken = expectAndConsume(TokenType.NUMBER);
        int repeatCount = parseAndValidateRepeatCount(repeatCountToken);

        if (!currentTokenIs(TokenType.TIMES)) {
            throw new BloopParseException(
                    "Expected 'times' after repeat count",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.TIMES);

        if (!currentTokenIs(TokenType.COLON)) {
            throw new BloopParseException(
                    "Expected ':' after 'times'",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.COLON);

        consumeNewlineIfPresent();

        List<Instruction> bodyInstructions = parseIndentedBlock();

        if (bodyInstructions.isEmpty()) {
            throw new BloopParseException(
                    "Expected at least one instruction in 'repeat' body",
                    repeatToken.getLine()
            );
        }

        return new RepeatInstruction(repeatCount, bodyInstructions);
    }


    //  Indented Block Parser

    private List<Instruction> parseIndentedBlock() {
        List<Instruction> blockInstructions = new ArrayList<>();

        if (!currentTokenIs(TokenType.INDENT)) {
            throw new BloopParseException(
                    "Expected indented block after ':'",
                    currentToken().getLine()
            );
        }
        expectAndConsume(TokenType.INDENT);

        while (!hasReachedEnd() && !currentTokenIs(TokenType.DEDENT)) {
            skipBlankLines();
            if (!hasReachedEnd() && !currentTokenIs(TokenType.DEDENT)) {
                blockInstructions.add(parseInstruction());
            }
        }

        if (!hasReachedEnd()) {
            expectAndConsume(TokenType.DEDENT);
        }

        return blockInstructions;
    }

    // ════════════════════════════════════════════
    //  Expression Parsers
    //  Precedence chain (low → high):
    //  parseExpression → parseTermExpression → parsePrimaryExpression
    // ════════════════════════════════════════════

    // ── Handles: + - and all comparisons ────────────────
    private Expression parseExpression() {
        Expression leftExpression = parseTermExpression();

        while (currentTokenIs(TokenType.PLUS)          ||
                currentTokenIs(TokenType.MINUS)         ||
                currentTokenIs(TokenType.GREATER)       ||
                currentTokenIs(TokenType.LESS)          ||
                currentTokenIs(TokenType.GREATER_EQUAL) ||
                currentTokenIs(TokenType.LESS_EQUAL)    ||
                currentTokenIs(TokenType.EQUAL_EQUAL)   ||
                currentTokenIs(TokenType.NOT_EQUAL)) {

            String operator = consumeCurrentToken().getValue();
            Expression rightExpression = parseTermExpression();
            leftExpression = new BinaryOpNode(leftExpression, operator, rightExpression);
        }

        return leftExpression;
    }

    // ── Handles: * / ─────────────────────────────────────
    private Expression parseTermExpression() {
        Expression leftExpression = parsePrimaryExpression();

        while (currentTokenIs(TokenType.STAR) || currentTokenIs(TokenType.SLASH)) {
            String operator = consumeCurrentToken().getValue();
            Expression rightExpression = parsePrimaryExpression();
            leftExpression = new BinaryOpNode(leftExpression, operator, rightExpression);
        }

        return leftExpression;
    }

    // ── Handles: number, string, variable ───────────────
    private Expression parsePrimaryExpression() {
        Token token = currentToken();

        if (currentTokenIs(TokenType.NUMBER)) {
            consumeCurrentToken();
            return parseNumberLiteral(token);
        }

        if (currentTokenIs(TokenType.STRING)) {
            consumeCurrentToken();
            return new StringNode(token.getValue());
        }

        if (currentTokenIs(TokenType.IDENTIFIER)) {
            consumeCurrentToken();
            return new VariableNode(token.getValue());
        }

        throw new BloopParseException(
                "Expected a number, string, or variable but got '" + token.getValue() + "'",
                token.getLine()
        );
    }

    // ════════════════════════════════════════════
    //  Helper Methods
    // ════════════════════════════════════════════

    private Expression parseNumberLiteral(Token numberToken) {
        try {
            double numericValue = Double.parseDouble(numberToken.getValue());
            return new NumberNode(numericValue);
        } catch (NumberFormatException e) {
            throw new BloopParseException(
                    "Invalid number format: '" + numberToken.getValue() + "'",
                    numberToken.getLine()
            );
        }
    }

    private int parseAndValidateRepeatCount(Token repeatCountToken) {
        try {
            double numericValue = Double.parseDouble(repeatCountToken.getValue());

            if (numericValue != Math.floor(numericValue)) {
                throw new BloopParseException(
                        "Repeat count must be a whole number, got: '" + repeatCountToken.getValue() + "'",
                        repeatCountToken.getLine()
                );
            }
            if (numericValue < 0) {
                throw new BloopParseException(
                        "Repeat count cannot be negative: '" + repeatCountToken.getValue() + "'",
                        repeatCountToken.getLine()
                );
            }

            return (int) numericValue;

        } catch (NumberFormatException e) {
            throw new BloopParseException(
                    "Invalid repeat count: '" + repeatCountToken.getValue() + "'",
                    repeatCountToken.getLine()
            );
        }
    }

    private void consumeNewlineIfPresent() {
        if (currentTokenIs(TokenType.NEWLINE)) {
            consumeCurrentToken();
        }
    }

    private void skipBlankLines() {
        while (!hasReachedEnd() && currentTokenIs(TokenType.NEWLINE)) {
            consumeCurrentToken();
        }
    }

    private Token expectAndConsume(TokenType expectedType) {
        if (currentTokenIs(expectedType)) {
            return consumeCurrentToken();
        }
        throw new BloopParseException(
                "Expected '" + expectedType + "' but got '" + currentToken().getValue() + "'",
                currentToken().getLine()
        );
    }

    private boolean currentTokenIs(TokenType expectedType) {
        if (hasReachedEnd()) return false;
        return currentToken().getType() == expectedType;
    }

    private Token consumeCurrentToken() {
        if (!hasReachedEnd()) currentIndex++;
        return lastConsumedToken();
    }

    private Token currentToken() {
        return tokenList.get(currentIndex);
    }

    private Token lastConsumedToken() {
        return tokenList.get(currentIndex - 1);
    }

    private boolean hasReachedEnd() {
        return currentToken().getType() == TokenType.EOF;
    }
}