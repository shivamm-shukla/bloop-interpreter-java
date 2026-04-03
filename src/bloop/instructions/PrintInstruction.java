package bloop.instructions;

import bloop.ast.Expression;
import bloop.runtime.Environment;

import java.util.Objects;

public class PrintInstruction implements Instruction {

    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = Objects.requireNonNull(expression, 
                "Expression to print cannot be null");
    }

    @Override
    public void execute(Environment env) {
        Objects.requireNonNull(env, "Execution environment cannot be null");

        Object value = expression.evaluate(env);
        System.out.println(format(value));
    }

    // ─────────────── Helper ───────────────

    private String format(Object value) {
        if (value == null) return "null";

        if (value instanceof Double d) {
            if (isWholeNumber(d)) {
                return String.valueOf(d.intValue());
            }
            return d.toString();
        }

        return value.toString();
    }

    private boolean isWholeNumber(double d) {
        return d == Math.floor(d) && !Double.isInfinite(d);
    }
}