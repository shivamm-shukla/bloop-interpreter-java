package bloop.ast;

import bloop.runtime.Environment;
import bloop.exceptions.BloopRuntimeException;

public class VariableNode implements Expression {

    private final String name;

    public VariableNode(String name) {
        validateName(name);
        this.name = name;
    }

    @Override
    public Object evaluate(Environment env) {
        // FIXED: Removed redundant validateEnvironment(env) for tight loop performance
        Object value = env.get(name);

        // FIXED: Safely handling undefined variables instead of propagating nulls
        if (value == null) {
            throw new BloopRuntimeException("Undefined variable: '" + name + "'");
        }
        return value;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("VariableNode: name cannot be null or empty");
        }
    }

    public String getName() {
        return name;
    }
}