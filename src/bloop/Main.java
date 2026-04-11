package bloop;

import bloop.exceptions.BloopException;
import bloop.interpreter.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class Main {

    private Main() {} // utility class — no instances

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsageError();
            System.exit(1);
        }

        Path sourceFilePath = Paths.get(args[0]);
        String sourceCode   = readSourceFile(sourceFilePath);

        runProgram(sourceCode);
    }

    // private helpers

    private static String readSourceFile(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            System.err.println("Error: Cannot read file '" + filePath + "'");
            System.err.println("       " + e.getMessage());
            System.exit(2);
            return null; // unreachable, satisfies compiler
        }
    }

    private static void runProgram(String sourceCode) {
        try {
            new Interpreter().run(sourceCode);
        } catch (BloopException e) {
            System.err.println("BLOOP Error: " + e.getMessage());
            System.exit(3);
        }
    }

    private static void printUsageError() {
        System.err.println("Usage:   java -cp out bloop.Main <file.bloop>");
        System.err.println("Example: java -cp out bloop.Main examples/program1.bloop");
    }
}
