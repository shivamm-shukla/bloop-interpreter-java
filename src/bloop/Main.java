package bloop;

import bloop.interpreter.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            exit("Usage: java bloop.Main <file.bloop>");
        }

        Path filePath = Path.of(args[0]);

        if (!filePath.toString().endsWith(".bloop")) {
            exit("Error: File must have a .bloop extension");
        }

        try {
            String source = Files.readString(filePath);
            new Interpreter().run(source);

        } catch (IOException e) {
            if (!Files.exists(filePath)) {
                exit("Error: File not found: '" + filePath + "'");
            }
            exit("Error: Could not read file: '" + filePath + "'");
        }
    }

    // helper

    private static void exit(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
