package bloop.ast;

import bloop.runtime.Environment;


public final class StringNode implements Expression {

    private final String stringValue;

    public StringNode(String stringValue) {
        this.stringValue = stringValue;
    }


    @Override
    public Object evaluate(Environment env) {
        return stringValue;
    }

    @Override
    public String toString() {
        return "StringNode(\"" + stringValue + "\")";
    }
}
