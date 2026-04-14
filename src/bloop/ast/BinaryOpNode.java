package bloop.ast;

import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;


public final class BinaryOpNode implements Expression {

    private final Expression leftOperand;
    private final String     operator;
    private final Expression rightOperand;

    public BinaryOpNode(Expression leftOperand, String operator, Expression rightOperand) {
        this.leftOperand  = leftOperand;
        this.operator     = operator;
        this.rightOperand = rightOperand;
    }


    @Override
    public Object evaluate(Environment env) {
        Object leftValue  = leftOperand.evaluate(env);
        Object rightValue = rightOperand.evaluate(env);

        return switch (operator) {
            case "+"  -> applyArithmetic(leftValue, rightValue, operator);
            case "-"  -> applyArithmetic(leftValue, rightValue, operator);
            case "*"  -> applyArithmetic(leftValue, rightValue, operator);
            case "/"  -> applyArithmetic(leftValue, rightValue, operator);
            case ">"  -> applyComparison(leftValue, rightValue, operator);
            case ">=" -> applyComparison(leftValue, rightValue, operator);
            case "<"  -> applyComparison(leftValue, rightValue, operator);
            case "<=" -> applyComparison(leftValue, rightValue, operator);
            case "==" -> applyEquality(leftValue, rightValue);
            case "!=" -> !(boolean) applyEquality(leftValue, rightValue);
            default   -> throw new BloopRuntimeException("Unknown operator: '" + operator + "'");
        };
    }

    // ── private helpers ───────────────────────────────────────────────────

    private Object applyArithmetic(Object left, Object right, String op) {
        double leftNum  = requireDouble(left,  "Left operand of '" + op + "'");
        double rightNum = requireDouble(right, "Right operand of '" + op + "'");

        return switch (op) {
            case "+" -> leftNum + rightNum;
            case "-" -> leftNum - rightNum;
            case "*" -> leftNum * rightNum;
            case "/" -> {
                if (rightNum == 0) throw new BloopRuntimeException("Division by zero");
                yield leftNum / rightNum;
            }
            default -> throw new BloopRuntimeException("Unknown arithmetic operator: '" + op + "'");
        };
    }

    private Object applyComparison(Object left, Object right, String op) {
        double leftNum  = requireDouble(left,  "Left operand of '" + op + "'");
        double rightNum = requireDouble(right, "Right operand of '" + op + "'");

        return switch (op) {
            case ">"  -> leftNum >  rightNum;
            case ">=" -> leftNum >= rightNum;
            case "<"  -> leftNum <  rightNum;
            case "<=" -> leftNum <= rightNum;
            default   -> throw new BloopRuntimeException("Unknown comparison operator: '" + op + "'");
        };
    }

    private Object applyEquality(Object left, Object right) {
        if (left instanceof Double leftNum && right instanceof Double rightNum) {
            return leftNum.equals(rightNum);
        }
        if (left instanceof String && right instanceof String) {
            return left.equals(right);
        }
        // Comparing different types (e.g. number == string) is always false
        return false;
    }

    private double requireDouble(Object value, String context) {
        if (value instanceof Double d) return d;
        throw new BloopRuntimeException(
                context + " must be a number, but got: \"" + value + "\"");
    }

    @Override
    public String toString() {
        return "BinaryOpNode(" + leftOperand + " " + operator + " " + rightOperand + ")";
    }
}
