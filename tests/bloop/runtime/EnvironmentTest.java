package bloop.runtime;

import bloop.exceptions.BloopRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Environment — the runtime variable store.
 */
@DisplayName("Environment")
class EnvironmentTest {

    private Environment env;

    @BeforeEach
    void freshEnvironment() {
        env = new Environment();
    }

    // ── set and get ───────────────────────────────────────────────────────

    @Test
    @DisplayName("stores and retrieves a numeric value")
    void storesAndRetrievesNumericValue() {
        env.set("x", 42.0);
        assertEquals(42.0, (Double) env.get("x"));
    }

    @Test
    @DisplayName("stores and retrieves a string value")
    void storesAndRetrievesStringValue() {
        env.set("name", "Sitare");
        assertEquals("Sitare", env.get("name"));
    }

    @Test
    @DisplayName("overwriting a variable reflects the latest value")
    void overwritingVariableReflectsLatestValue() {
        env.set("counter", 1.0);
        env.set("counter", 2.0);
        assertEquals(2.0, (Double) env.get("counter"));
    }

    @Test
    @DisplayName("different variables are stored independently")
    void differentVariablesAreIndependent() {
        env.set("a", 1.0);
        env.set("b", 2.0);
        assertEquals(1.0, (Double) env.get("a"));
        assertEquals(2.0, (Double) env.get("b"));
    }

    // ── undefined variable ────────────────────────────────────────────────

    @Test
    @DisplayName("throws BloopRuntimeException for undefined variable")
    void throwsForUndefinedVariable() {
        assertThrows(BloopRuntimeException.class, () -> env.get("notDefined"));
    }

    @Test
    @DisplayName("error message for undefined variable includes the variable name")
    void errorMessageIncludesVariableName() {
        BloopRuntimeException ex = assertThrows(
                BloopRuntimeException.class, () -> env.get("missingVar"));
        assertTrue(ex.getMessage().contains("missingVar"),
                "Error message should include the variable name");
    }

    // ── isDefined ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isDefined returns false for unset variable")
    void isDefinedReturnsFalseForUnset() {
        assertFalse(env.isDefined("ghost"));
    }

    @Test
    @DisplayName("isDefined returns true after variable is set")
    void isDefinedReturnsTrueAfterSet() {
        env.set("x", 10.0);
        assertTrue(env.isDefined("x"));
    }

    // ── snapshot ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("snapshot returns all currently set variables")
    void snapshotContainsAllVariables() {
        env.set("x", 1.0);
        env.set("y", 2.0);
        var snap = env.snapshot();
        assertTrue(snap.containsKey("x"));
        assertTrue(snap.containsKey("y"));
    }

    @Test
    @DisplayName("snapshot is immutable")
    void snapshotIsImmutable() {
        env.set("x", 1.0);
        var snap = env.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snap.put("z", 3.0));
    }

    @Test
    @DisplayName("mutating environment after snapshot does not affect snapshot")
    void mutatingEnvironmentDoesNotAffectSnapshot() {
        env.set("x", 1.0);
        var snap = env.snapshot();
        env.set("x", 99.0);
        assertEquals(1.0, (Double) snap.get("x"));
    }
}
