package bloop.ast;

import bloop.runtime.Environment;


public final class VariableNode implements Expression {

    private final String variableName;

    public VariableNode(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() { return variableName; }

    @Override
    public Object evaluate(Environment env) {
        return env.get(variableName);
    }

    @Override
    public String toString() {
        return "VariableNode(" + variableName + ")";
    }
}
