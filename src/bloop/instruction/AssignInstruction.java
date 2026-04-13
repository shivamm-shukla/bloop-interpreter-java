package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;


public final class AssignInstruction implements Instruction {

    private final String     targetVariableName;
    private final Expression valueExpression;

    public AssignInstruction(String targetVariableName, Expression valueExpression) {
        this.targetVariableName = targetVariableName;
        this.valueExpression    = valueExpression;
    }

    @Override
    public void execute(Environment env) {
        Object computedValue = valueExpression.evaluate(env);
        env.set(targetVariableName, computedValue);
    }

    @Override
    public String toString() {
        return "AssignInstruction(put " + valueExpression + " into " + targetVariableName + ")";
    }
}
