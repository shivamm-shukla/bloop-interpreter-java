package bloop.instruction;

import bloop.ast.Expression;
import bloop.exceptions.BloopRuntimeException;
import bloop.runtime.Environment;

import java.util.List;


public final class RepeatInstruction implements Instruction {

    private final Expression countExpression;
    private final List<Instruction> body;

    public RepeatInstruction(Expression countExpression, List<Instruction> body) {
        this.countExpression = countExpression;
        this.body            = List.copyOf(body);
    }

    @Override
    public void execute(Environment env) {
        Object countValue = countExpression.evaluate(env);

        if (!(countValue instanceof Double d)) {
            throw new BloopRuntimeException(
                    "Repeat count must be a number, but got: \"" + countValue + "\"");
        }

        if (d != Math.floor(d) || d < 0) {
            throw new BloopRuntimeException(
                    "Repeat count must be a non-negative whole number, but got: " + d);
        }

        if (d > Integer.MAX_VALUE) {
            throw new BloopRuntimeException(
                    "Repeat count " + d.longValue() + " is too large to execute");
        }

        int count = d.intValue();
        for (int i = 0; i < count; i++) {
            body.forEach(instruction -> instruction.execute(env));
        }
    }

    @Override
    public String toString() {
        return "RepeatInstruction(repeat " + countExpression +
                " times, body=" + body.size() + " instruction(s))";
    }
}