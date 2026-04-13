package bloop.ast;

import bloop.runtime.Environment;


public final class NumberNode implements Expression {

    private final double numericValue;

    public NumberNode(double numericValue) {
        this.numericValue = numericValue;
    }

    @Override
    public Object evaluate(Environment env) {
        return numericValue;
    }

    @Override
    public String toString() {
        return "NumberNode(" + numericValue + ")";
    }
}
