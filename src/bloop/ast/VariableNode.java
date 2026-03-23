package bloop.ast;

import org.w3c.dom.Node;

public class VariableNode implements Expression {

    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(Object env) {
        return null; // will do it later, after Environment class
    }
}
