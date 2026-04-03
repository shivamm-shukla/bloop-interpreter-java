package bloop.interpreter;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

class InterpreterTest {

    private Interpreter interpreter;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setup() {
        interpreter = new Interpreter();
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();

        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    private String run(String code) {
        out.reset();
        interpreter.run(code);
        return out.toString().replace("\r","").trim();
    }

    private String runErr(String code) {
        err.reset();
        interpreter.run(code);
        return err.toString();
    }

    @Test
    void run_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> interpreter.run(null));
    }

    @Test
    void simple_program() {
        assertEquals("42", run("put 42 into x\nprint x"));
    }

    @Test
    void arithmetic() {
        assertEquals("7", run("put 3 + 4 into x\nprint x"));
    }

    @Test
    void conditional() {
        String code = """
            put 5 into x
            if x > 3 then:
                print "ok"
            """;
        assertEquals("ok", run(code));
    }

    @Test
    void loop() {
        String code = """
            put 1 into i
            repeat 3 times:
                print i
                put i + 1 into i
            """;
        assertEquals("1\n2\n3", run(code));
    }

    @Test
    void runtime_error() {
        String errOut = runErr("print z");
        assertTrue(errOut.contains("Error"));
    }
}