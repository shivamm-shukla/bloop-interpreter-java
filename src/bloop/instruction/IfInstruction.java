package bloop.instruction;

import bloop.ast.Expression;
import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;

import java.util.List;


public final class IfInstruction implements Instruction {

    private final Expression conditionExpression;
    private final List<Instruction> thenBody;
    private final List<Instruction> elseBody; // empty list when no else block

    // Constructor without an else block
    public IfInstruction(Expression conditionExpression, List<Instruction> thenBody) {
        this(conditionExpression, thenBody, List.of());
    }

    // Constructor with an else block (bonus feature)
    public IfInstruction(Expression conditionExpression,
                         List<Instruction> thenBody,
                         List<Instruction> elseBody) {
        this.conditionExpression = conditionExpression;
        this.thenBody            = List.copyOf(thenBody);
        this.elseBody            = List.copyOf(elseBody);
    }

    @Override
    public void execute(Environment env) {
        Object conditionResult = conditionExpression.evaluate(env);

        if (!(conditionResult instanceof Boolean)) {
            throw new BloopRuntimeException(
                    "Condition in 'if' must be a boolean expression, " +
                            "but got: " + conditionResult);
        }

        if ((Boolean) conditionResult) {
            thenBody.forEach(instruction -> instruction.execute(env));
        } else {
            elseBody.forEach(instruction -> instruction.execute(env));
        }
    }

    public boolean hasElseBlock() {
        return !elseBody.isEmpty();
    }

    @Override
    public String toString() {
        return "IfInstruction(condition=" + conditionExpression +
                ", thenBody=" + thenBody.size() + " instruction(s)" +
                (hasElseBlock() ? ", elseBody=" + elseBody.size() + " instruction(s)" : "") + ")";
    }
}
