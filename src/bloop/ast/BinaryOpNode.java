package bloop.ast;

import bloop.runtime.Environment;

public class BinaryOpNode implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right) {
        if (left == null)     throw new IllegalArgumentException("Left expression cannot be null");
        if (operator == null) throw new IllegalArgumentException("Operator cannot be null");
        if (right == null)    throw new IllegalArgumentException("Right expression cannot be null");
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftValue  = left.evaluate(env);
        Object rightValue = right.evaluate(env);

        switch (operator) {

            // ── Arithmetic ──────────────────────────────
            case "+":
                // String + String  →  concatenation
                if (leftValue instanceof String || rightValue instanceof String) {
                    return stringify(leftValue) + stringify(rightValue);
                }
                return toDouble(leftValue, "+") + toDouble(rightValue, "+");

            case "-":
                return toDouble(leftValue, "-") - toDouble(rightValue, "-");

            case "*":
                return toDouble(leftValue, "*") * toDouble(rightValue, "*");

            case "/":
                double divisor = toDouble(rightValue, "/");
                if (divisor == 0)
                    throw new RuntimeException("Division by zero");
                return toDouble(leftValue, "/") / divisor;

            // ── Comparisons ─────────────────────────────
            case ">":
                return toDouble(leftValue, ">") > toDouble(rightValue, ">");

            case "<":
                return toDouble(leftValue, "<") < toDouble(rightValue, "<");

            case ">=":
                return toDouble(leftValue, ">=") >= toDouble(rightValue, ">=");

            case "<=":
                return toDouble(leftValue, "<=") <= toDouble(rightValue, "<=");

            case "==":
                if (leftValue instanceof Double && rightValue instanceof Double)
                    return toDouble(leftValue, "==") == toDouble(rightValue, "==");
                if (leftValue == null) return rightValue == null;
                return leftValue.equals(rightValue);

            case "!=":
                if (leftValue instanceof Double && rightValue instanceof Double)
                    return toDouble(leftValue, "!=") != toDouble(rightValue, "!=");
                if (leftValue == null) return rightValue != null;
                return !leftValue.equals(rightValue);

            default:
                throw new RuntimeException("Unknown operator: '" + operator + "'");
        }
    }

    // ── Helpers ─────────────────────────────────────

    private double toDouble(Object value, String op) {
        if (value instanceof Double)
            return (Double) value;
        throw new RuntimeException(
                "Operator '" + op + "' requires a number, got: " +
                        (value == null ? "null" : value.getClass().getSimpleName())
        );
    }

    private String stringify(Object value) {
        if (value == null)       return "null";
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d))
                return String.valueOf((int) d);
            return String.valueOf(d);
        }
        return value.toString();
    }
}