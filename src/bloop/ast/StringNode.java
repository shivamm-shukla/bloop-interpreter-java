package bloop.ast;

import bloop.runtime.Environment;

public class StringNode implements Expression {

    private final String value;

    public StringNode(String value) {
        if (value == null)
            throw new IllegalArgumentException("StringNode: value cannot be null");
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}