package bloop.runtime;

import bloop.exceptions.BloopRuntimeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    private Environment env;

    @BeforeEach
    void setup() {
        env = new Environment();
    }

    @Test
    void storeVariable_validNameAndNumber_storedSuccessfully() {
        env.set("x", 42.0);
        assertEquals(42.0, env.get("x"));
    }

    @Test
    void storeVariable_validNameAndString_storedSuccessfully() {
        env.set("name", "Sitare");
        assertEquals("Sitare", env.get("name"));
    }

    @Test
    void storeVariable_nullVariableName_throwsRuntimeException() {
        assertThrows(BloopRuntimeException.class,
                () -> env.set(null, 10.0));
    }

    @Test
    void storeVariable_emptyVariableName_throwsRuntimeException() {
        assertThrows(BloopRuntimeException.class,
                () -> env.set("", 10.0));
    }

    @Test
    void storeVariable_existingVariable_updatesValue() {
        env.set("x", 10.0);
        env.set("x", 99.0);
        assertEquals(99.0, env.get("x"));
    }

    @Test
    void retrieveVariable_definedVariable_returnsCorrectValue() {
        env.set("score", 85.0);
        assertEquals(85.0, env.get("score"));
    }

    @Test
    void retrieveVariable_undefinedVariable_throwsRuntimeException() {
        assertThrows(BloopRuntimeException.class,
                () -> env.get("undefinedVar"));
    }

    @Test
    void isVariableDefined_definedVariable_returnsTrue() {
        env.set("x", 10.0);
        assertTrue(env.isDefined("x"));
    }

    @Test
    void isVariableDefined_undefinedVariable_returnsFalse() {
        assertFalse(env.isDefined("undefinedVar"));
    }
}