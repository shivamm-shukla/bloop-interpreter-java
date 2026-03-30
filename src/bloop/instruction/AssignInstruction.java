package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;

public class AssignInstruction implements Instruction {
    private final String variableName;
    private final Expression expression;

    public AssignInstruction(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        env.set(variableName, expression.evaluate(env));
    }
}
