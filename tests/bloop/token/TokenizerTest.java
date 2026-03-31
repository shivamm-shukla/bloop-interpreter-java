package bloop.token;

import java.util.*;

public class TokenizerTest {

    public static void main(String[] args) {

        String code = "put 10 into x\nprint x";

        Tokenizer tokenizer = new Tokenizer(code);
        List<Token> tokens = tokenizer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}