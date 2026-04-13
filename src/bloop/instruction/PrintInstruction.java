package bloop.instruction;

import bloop.ast.Expression;
import bloop.runtime.Environment;


public final class PrintInstruction implements Instruction {

    private final Expression expressionToPrint;

    public PrintInstruction(Expression expressionToPrint) {
        this.expressionToPrint = expressionToPrint;
    }

    @Override
    public void execute(Environment env) {
        Object value = expressionToPrint.evaluate(env);
        System.out.println(formatForOutput(value));
    }

    private String formatForOutput(Object value) {
        if (value instanceof Double d && d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf(d.longValue());
        }
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        return "PrintInstruction(print " + expressionToPrint + ")";
    }
}
