package bloop.interpreter;

import bloop.instruction.Instruction;
import bloop.parser.Parser;
import bloop.runtime.Environment;
import bloop.token.Token;
import bloop.token.Tokenizer;

import java.util.List;

public class Interpreter {
    public void run(String sourceCode) {
        Tokenizer tokenizer = new Tokenizer(sourceCode);
        List<Token> tokens = tokenizer.tokenize();

        Parser parser = new Parser(tokens);
        List<Instruction> program = parser.parse();

        Environment env = new Environment();
        for (Instruction instruction : program) {
            instruction.execute(env);
        }
    }
}
