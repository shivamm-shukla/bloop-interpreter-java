package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;

import java.util.List;

public class IfInstruction implements Instruction {
    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        Object result = condition.evaluate(env);
        if (!(result instanceof Boolean)) {
            throw new RuntimeException("If condition must evaluate to a boolean");
        }
        if ((Boolean) result) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
