package bloop.ast;

public class StringNode implements Expression{

    private final String value;

    public StringNode(String value){
        this.value = value;
    }
    @Override
    public Object evaluate(Object env){
        return value;
    }
}
