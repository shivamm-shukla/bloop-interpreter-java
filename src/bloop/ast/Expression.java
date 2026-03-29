package bloop.ast;

import bloop.runtime.Environment;

public interface Expression {

    Object evaluate(Environment env);
}
