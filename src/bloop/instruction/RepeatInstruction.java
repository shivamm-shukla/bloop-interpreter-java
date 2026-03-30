package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;

import java.util.List;

public class RepeatInstruction implements Instruction {
    private final Expression countExpression;
    private final List<Instruction> body;

    public RepeatInstruction(Expression countExpression, List<Instruction> body) {
        this.countExpression = countExpression;
        this.body = body;
    }

    @Override
    public void execute(Environment env) {
        Object countValue = countExpression.evaluate(env);
        if (!(countValue instanceof Number number)) {
            throw new RuntimeException("Repeat count must be a number");
        }
        int count = (int) Math.floor(number.doubleValue());
        for (int i = 0; i < count; i++) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}
