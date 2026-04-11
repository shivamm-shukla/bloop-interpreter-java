package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;

/**
 * Instruction: evaluate an expression and store the result in a variable.
 *
 * BLOOP syntax:
 *   put 10 into x
 *   put x + y * 2 into result
 *
 * Execution steps:
 *   1. Evaluate the value expression (produces a Double or String).
 *   2. Store the result under the target variable name in the Environment.
 *
 * Immutability: all fields are final, no setters.
 */
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
