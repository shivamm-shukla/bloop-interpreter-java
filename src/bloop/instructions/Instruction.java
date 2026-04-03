package bloop.instructions;

import bloop.runtime.Environment;

public interface Instruction {
    void execute(Environment env);
}
