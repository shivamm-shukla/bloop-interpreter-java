package bloop.ast;

import bloop.runtime.Environment;

public class VariableNode implements Expression {

    private final String name;

    public VariableNode(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("VariableNode: name cannot be null or empty");
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        if (env == null)
            throw new IllegalArgumentException("VariableNode: environment cannot be null");
        return env.get(name);
    }
}