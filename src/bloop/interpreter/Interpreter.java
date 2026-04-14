package bloop.interpreter;

import bloop.exceptions.BloopException;
import bloop.instruction.Instruction;
import bloop.parser.Parser;
import bloop.runtime.Environment;
import bloop.token.Token;
import bloop.token.Tokenizer;

import java.util.List;


public final class Interpreter {

    private final Parser parser;

    // constructor — uses the default built-in parser configuration
    public Interpreter() {
        this.parser = Parser.createDefault();
    }


    public void run(String sourceCode) {
        try {
            // Stage 1: Tokenize
            Tokenizer         tokenizer    = new Tokenizer(sourceCode);
            List<Token>       tokens       = tokenizer.tokenize();

            // Stage 2: Parse
            List<Instruction> instructions = parser.parse(tokens);

            // Stage 3: Execute
            Environment sharedEnvironment = new Environment();
            for (Instruction instruction : instructions) {
                instruction.execute(sharedEnvironment);
            }

        } catch (BloopException e) {
            System.err.println(e.getMessage());
        }
    }
}