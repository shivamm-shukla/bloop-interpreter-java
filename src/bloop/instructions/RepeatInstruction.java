package bloop.instructions;

import bloop.runtime.Environment;

import java.util.List;
import java.util.Objects;

public class RepeatInstruction implements Instruction {

    private final int times;
    private final List<Instruction> body;

    public RepeatInstruction(int times, List<Instruction> body) {
        if (times < 0) {
            throw new IllegalArgumentException(
                "Repeat count cannot be negative: " + times
            );
        }

        this.times = times;
        this.body = Objects.requireNonNull(body, 
                "Body instructions cannot be null");
    }

    @Override
    public void execute(Environment env) {
        Objects.requireNonNull(env, "Execution environment cannot be null");

        for (int i = 0; i < times; i++) {
            for (Instruction instruction : body) {
                instruction.execute(env);
            }
        }
    }
}