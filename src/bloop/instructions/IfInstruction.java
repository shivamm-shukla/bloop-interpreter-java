package bloop.instructions;

import bloop.ast.Expression;
import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;

import java.util.List;
import java.util.Objects;

public class IfInstruction implements Instruction {

    private final Expression condition;
    private final List<Instruction> body;

    public IfInstruction(Expression condition, List<Instruction> body) {
        this.condition = Objects.requireNonNull(condition, 
                "Condition expression cannot be null");
        this.body = Objects.requireNonNull(body, 
                "Body instructions cannot be null");
    }

    @Override
    public void execute(Environment env) {
        Objects.requireNonNull(env, "Execution environment cannot be null");

        Object result = condition.evaluate(env);

        if (!(result instanceof Boolean)) {
            throw new BloopRuntimeException(
                "Condition must evaluate to boolean, but got: " + typeOf(result)
            );
        }

        if ((Boolean) result) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }

    // ─────────────── Helper ───────────────

    private String typeOf(Object obj) {
        return (obj == null) ? "null" : obj.getClass().getSimpleName();
    }
}