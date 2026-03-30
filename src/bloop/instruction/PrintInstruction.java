package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;

public class PrintInstruction implements Instruction {
    private final Expression expression;

    public PrintInstruction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(Environment env) {
        Object value = expression.evaluate(env);
        if (value instanceof Double d) {
            if (d == Math.rint(d)) {
                System.out.println((long) d.doubleValue());
            } else {
                System.out.println(d);
            }
        } else {
            System.out.println(value);
        }
    }
}
