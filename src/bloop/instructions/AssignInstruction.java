package bloop.instructions;

import bloop.ast.Expression;
import bloop.runtime.Environment;

import java.util.Objects;

public class AssignInstruction implements Instruction {

    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = validateName(variableName);
        this.expression = Objects.requireNonNull(expression, 
                "Expression cannot be null");
    }

    @Override
    public void execute(Environment env) {
        Objects.requireNonNull(env, "Execution environment cannot be null");

        Object value = expression.evaluate(env);
        env.set(variableName, value);
    }

    // ─────────────── Helper ───────────────

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        return name;
    }
}