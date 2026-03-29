package bloop.token;

import java.util.*;
// tokenizer class:
public class Tokenizer {

    private final String input;
    private int pos = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();

    public Tokenizer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        while (pos < input.length()) {
            char current = input.charAt(pos);

            // Whitespace
            if (current == ' ' || current == '\t') {
                pos++;
            }

            // Newline
            else if (current == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));
                line++;
                pos++;
            }

            // Numbers
            else if (Character.isDigit(current)) {
                tokenizeNumber();
            }

            
            else if (Character.isLetter(current)) {
                tokenizeWord();
            }

            // Strings
            else if (current == '"') {
                tokenizeString();
            }

            // Operators
            else {
                tokenizeOperator(current);
                pos++;
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ───────────── NUMBER ─────────────
    private void tokenizeNumber() {
        int start = pos;

        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }

        String number = input.substring(start, pos);
        tokens.add(new Token(TokenType.NUMBER, number, line));
    }

    // ───────────── WORD ─────────────
    private void tokenizeWord() {
        int start = pos;

        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            pos++;
        }

        String word = input.substring(start, pos);

        switch (word) {
            case "put":
                tokens.add(new Token(TokenType.PUT, word, line));
                break;
            case "into":
                tokens.add(new Token(TokenType.INTO, word, line));
                break;
            case "print":
                tokens.add(new Token(TokenType.PRINT, word, line));
                break;
            case "if":
                tokens.add(new Token(TokenType.IF, word, line));
                break;
            case "then":
                tokens.add(new Token(TokenType.THEN, word, line));
                break;
            case "repeat":
                tokens.add(new Token(TokenType.REPEAT, word, line));
                break;
            case "times":
                tokens.add(new Token(TokenType.TIMES, word, line));
                break;
            default:
                tokens.add(new Token(TokenType.IDENTIFIER, word, line));
        }
    }

    // ───────────── STRING ─────────────
    private void tokenizeString() {
        pos++; // skip opening "

        int start = pos;

        while (pos < input.length() && input.charAt(pos) != '"') {
            pos++;
        }

        String value = input.substring(start, pos);
        pos++; // skip closing "

        tokens.add(new Token(TokenType.STRING, value, line));
    }

    // ───────────── OPERATORS ─────────────
     // ───────────── OPERATORS ─────────────
private void tokenizeOperator(char current) {

    switch (current) {

        case '+':
            tokens.add(new Token(TokenType.PLUS, "+", line));
            break;

        case '-':
            tokens.add(new Token(TokenType.MINUS, "-", line));
            break;

        case '*':
            tokens.add(new Token(TokenType.STAR, "*", line));
            break;

        case '/':
            tokens.add(new Token(TokenType.SLASH, "/", line));
            break;

        case '>':
            if (peek() == '=') {
                pos++;
                tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", line));
            } else {
                tokens.add(new Token(TokenType.GREATER, ">", line));
            }
            break;

        case '<':
            if (peek() == '=') {
                pos++;
                tokens.add(new Token(TokenType.LESS_EQUAL, "<=", line));
            } else {
                tokens.add(new Token(TokenType.LESS, "<", line));
            }
            break;

        case '=':
            if (peek() == '=') {
                pos++;
                tokens.add(new Token(TokenType.EQUAL_EQUAL, "==", line));
            } else {
                throw new RuntimeException("Unexpected '=' at line " + line);
            }
            break;

        case '!':
            if (peek() == '=') {
                pos++;
                tokens.add(new Token(TokenType.NOT_EQUAL, "!=", line));
            } else {
                throw new RuntimeException("Unexpected '!' at line " + line);
            }
            break;

        case ':':
            tokens.add(new Token(TokenType.COLON, ":", line));
            break;

        default:
            throw new RuntimeException("Unexpected character: " + current + " at line " + line);
    }
}
    }

    private char peek() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }
}
