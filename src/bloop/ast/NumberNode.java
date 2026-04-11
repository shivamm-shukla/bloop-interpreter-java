package bloop.ast;

import bloop.runtime.Environment;

public class NumberNode implements Expression {

    private final double value;

    public NumberNode(double value) {
        validate(value);
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return (value == 0.0) ? 0.0 : value;
    }

    // ── Validation ───────────────────────────────────

    private void validate(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("NumberNode: NaN is not allowed");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("NumberNode: Infinite values are not allowed");
        }
    }

    // for debugging

    public double getValue() {
        return value;
    }
}