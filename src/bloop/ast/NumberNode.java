package bloop.ast;

public class NumberNode implements Expression {

    private final double value;

    public NumberNode(double value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Object env){
        return value;
    }
}
