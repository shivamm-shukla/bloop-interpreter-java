package bloop.ast;

import bloop.runtime.Environment;
import bloop.exceptions.BloopRuntimeException;

public class BinaryOpNode implements Expression {

    private final Expression left;
    private final String op;
    private final Expression right;

    public BinaryOpNode(Expression left, String op, Expression right) {
        if (left == null)  throw new IllegalArgumentException("Left expression cannot be null");
        if (op == null)    throw new IllegalArgumentException("Operator cannot be null");
        if (right == null) throw new IllegalArgumentException("Right expression cannot be null");

        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftVal  = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        switch (op) {
            // ── Arithmetic ──────────────────────────────
            case "+":
                return handleAddition(leftVal, rightVal);
            case "-":
                return requireNumber(leftVal, "-") - requireNumber(rightVal, "-");
            case "*":
                return requireNumber(leftVal, "*") * requireNumber(rightVal, "*");
            case "/":
                double divisor = requireNumber(rightVal, "/");
                if (divisor == 0) {
                    throw new BloopRuntimeException("Division by zero");
                }
                return requireNumber(leftVal, "/") / divisor;

            // ── Comparisons ─────────────────────────────
            case ">":
                return requireNumber(leftVal, ">") > requireNumber(rightVal, ">");
            case "<":
                return requireNumber(leftVal, "<") < requireNumber(rightVal, "<");
            case ">=":
                return requireNumber(leftVal, ">=") >= requireNumber(rightVal, ">=");
            case "<=":
                return requireNumber(leftVal, "<=") <= requireNumber(rightVal, "<=");
            case "==":
                return isEqual(leftVal, rightVal);
            case "!=":
                return !isEqual(leftVal, rightVal);
            default:
                throw new BloopRuntimeException("Unknown operator: '" + op + "'");
        }
    }

    // ── Operation Helpers ────────────────────────────

    private Object handleAddition(Object leftVal, Object rightVal) {
        if (leftVal instanceof String || rightVal instanceof String) {
            return stringify(leftVal) + stringify(rightVal);
        }
        return requireNumber(leftVal, "+") + requireNumber(rightVal, "+");
    }

    private boolean isEqual(Object leftVal, Object rightVal) {
        // FIXED: standard equals() handles Double edge cases perfectly
        if (leftVal == null) return rightVal == null;
        return leftVal.equals(rightVal);
    }

    // ── Utility Helpers ──────────────────────────────

    private double requireNumber(Object value, String op) {
        // FIXED: Checks against Number instead of strict Double
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new BloopRuntimeException(
                "Operator '" + op + "' requires a number, got: " +
                        (value == null ? "null" : value.getClass().getSimpleName())
        );
    }

    private String stringify(Object value) {
        if (value == null) return "null";

        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                //Casted to long to prevent overflow for large numbers
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }

        return value.toString();
    }
}