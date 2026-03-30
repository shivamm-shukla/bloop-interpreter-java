package bloop.ast;

import bloop.runtime.Environment;

public class VariableNode implements Expression {

    private final String name;

    public VariableNode(String name) {
        validateName(name);
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        validateEnvironment(env);
        return env.get(name);
    }

    // ── Validation ───────────────────────────────────

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("VariableNode: name cannot be null or empty");
        }
    }

    private void validateEnvironment(Environment env) {
        if (env == null) {
            throw new IllegalArgumentException("VariableNode: environment cannot be null");
        }
    }

    // for debugging

    public String getName() {
        return name;
    }
}