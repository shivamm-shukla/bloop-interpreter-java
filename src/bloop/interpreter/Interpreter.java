package bloop.interpreter;

import bloop.exceptions.BloopException;
import bloop.instructions.Instruction;
import bloop.parser.Parser;
import bloop.runtime.Environment;
import bloop.token.Token;
import bloop.token.Tokenizer;

import java.util.List;

public class Interpreter {
    public void run(String source) {
        if (source == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }

        if (source.trim().isEmpty()) {
            return;
        }

        try {
            Tokenizer tokenizer = new Tokenizer(source);
            List<Token> tokens = tokenizer.tokenize();

            Parser parser = new Parser(tokens);
            List<Instruction> program = parser.parse();

            Environment env = new Environment();
            for (Instruction instruction : program) {
                instruction.execute(env);
            }

        } catch (BloopException e) {
            System.err.println(formatError(e));
        }
    }

    // ─────────────── Error Formatting ───────────────

    private String formatError(BloopException e) {
        int line = e.getLine();

        if (line >= 0) {
            return "[Error at line " + line + "] " + e.getMessage();
        }
        return "[Error] " + e.getMessage();
    }
}
