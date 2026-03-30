package bloop.ast;

import bloop.runtime.Environment;

public class StringNode implements Expression {

    private final String value;

    public StringNode(String value) {
        validate(value);
        this.value = value;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }

    // ── Validation ───────────────────────────────────

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("StringNode: value cannot be null");
        }
    }

    // for debugging

    public String getValue() {
        return value;
    }
}