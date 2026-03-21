package bloop.ast;

public interface Expression {

    Object evaluate(Object env);
}
