package bloop.ast;

import bloop.runtime.Environment;

public class NumberNode implements Expression {

    private final double value;

    public NumberNode(double value) {
        if (Double.isNaN(value))
            throw new IllegalArgumentException("NumberNode: NaN not allowed");
        if (Double.isInfinite(value))
            throw new IllegalArgumentException("NumberNode: Infinite value not allowed");
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}