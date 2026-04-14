package bloop.runtime;

import bloop.exceptions.BloopRuntimeException;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Environment — exhaustive")
class EnvironmentTest {

    private Environment env;

    @BeforeEach
    void setUp() { env = new Environment(); }

    // ══════════════════════════════════════════════════════════════════════
    // SET & GET — basic types
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("set() and get()")
    class SetGet {
        @Test void setAndGetDouble()         { env.set("x", 42.0);    assertEquals(42.0,   (double)env.get("x")); }
        @Test void setAndGetNegative()       { env.set("x", -5.0);    assertEquals(-5.0,   (double)env.get("x")); }
        @Test void setAndGetZero()           { env.set("x", 0.0);     assertEquals(0.0,    (double)env.get("x")); }
        @Test void setAndGetFloat()          { env.set("x", 3.14);    assertEquals(3.14,   (double)env.get("x"), 1e-9); }
        @Test void setAndGetString()         { env.set("s", "hi");    assertEquals("hi",   env.get("s")); }
        @Test void setAndGetEmptyString()    { env.set("s", "");      assertEquals("",     env.get("s")); }
        @Test void setAndGetBoolean()        { env.set("b", true);    assertEquals(true,   env.get("b")); }
        @Test void setAndGetFalse()          { env.set("b", false);   assertEquals(false,  env.get("b")); }
        @Test void setAndGetNull()           { env.set("n", null);    assertNull(env.get("n")); }

        @Test void getUndefinedThrows()      {
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> env.get("missing"));
            assertTrue(ex.getMessage().contains("missing"));
        }
        @Test void getUndefinedMessageContainsName() {
            BloopRuntimeException ex = assertThrows(BloopRuntimeException.class, () -> env.get("myVar"));
            assertTrue(ex.getMessage().contains("myVar"));
        }
        @Test void getAfterSetReturnsSameObject() {
            Object obj = "hello";
            env.set("s", obj);
            assertSame(obj, env.get("s"));
        }
        @Test void setDoesNotReturnValue()   { env.set("x", 1.0); assertDoesNotThrow(() -> env.set("y", 2.0)); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // OVERWRITE
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Overwriting variables")
    class Overwrite {
        @Test void overwriteWithNewNumber()  { env.set("x", 1.0); env.set("x", 99.0); assertEquals(99.0, (double)env.get("x")); }
        @Test void overwriteNumberWithString(){ env.set("v", 1.0); env.set("v", "str"); assertEquals("str", env.get("v")); }
        @Test void overwriteStringWithNumber(){ env.set("v", "str"); env.set("v", 7.0); assertEquals(7.0, (double)env.get("v")); }
        @Test void overwriteManyTimes()      {
            for (int i = 0; i < 100; i++) env.set("x", (double)i);
            assertEquals(99.0, (double)env.get("x"));
        }
        @Test void overwriteDoesNotAffectOtherVars() {
            env.set("a", 1.0); env.set("b", 2.0);
            env.set("a", 99.0);
            assertEquals(2.0, (double)env.get("b"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // IS DEFINED
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("isDefined()")
    class IsDefined {
        @Test void falseForUnknown()         { assertFalse(env.isDefined("ghost")); }
        @Test void trueAfterSet()            { env.set("x", 1.0); assertTrue(env.isDefined("x")); }
        @Test void trueAfterOverwrite()      { env.set("x", 1.0); env.set("x", 2.0); assertTrue(env.isDefined("x")); }
        @Test void trueAfterNullSet()        { env.set("x", null); assertTrue(env.isDefined("x")); }
        @Test void falseForSimilarName()     { env.set("x", 1.0); assertFalse(env.isDefined("X")); }
        @Test void falseForPrefixOfName()    { env.set("hello", 1.0); assertFalse(env.isDefined("hell")); }
        @Test void trueForUnderscoreName()   { env.set("my_var", 1.0); assertTrue(env.isDefined("my_var")); }

        @Test
        @DisplayName("isDefined returns false before assignment")
        void isDefinedFalseBeforeSet() {
            assertFalse(env.isDefined("notSet"));
        }

        @Test
        @DisplayName("isDefined returns true after assignment")
        void isDefinedTrueAfterSet() {
            env.set("flag", true);
            assertTrue(env.isDefined("flag"));
        }

        @Test
        @DisplayName("isDefined still true after overwrite")
        void isDefinedAfterOverwrite() {
            env.set("x", 1.0);
            env.set("x", 2.0);
            assertTrue(env.isDefined("x"));
        }

    }

    // ── undefined variable ───────────────────────────────────

    @Test
    @DisplayName("get() on undefined variable throws BloopRuntimeException")
    void getUndefined_throws() {
        assertThrows(BloopRuntimeException.class, () -> env.get("z"));
    }

    @Test
    @DisplayName("Exception message mentions the variable name")
    void getUndefined_messageContainsName() {
        BloopRuntimeException ex = assertThrows(BloopRuntimeException.class,
                () -> env.get("missingVar"));
        assertTrue(ex.getMessage().contains("missingVar"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // SNAPSHOT
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("snapshot()")
    class Snapshot {
        @Test void emptySnapshot()           { assertTrue(env.snapshot().isEmpty()); }
        @Test void snapshotContainsVar()     { env.set("a", 1.0); assertEquals(1, env.snapshot().size()); }
        @Test void snapshotContainsAllVars() {
            env.set("a", 1.0); env.set("b", "hi"); env.set("c", true);
            Map<String, Object> snap = env.snapshot();
            assertEquals(3, snap.size());
            assertEquals(1.0, snap.get("a"));
            assertEquals("hi", snap.get("b"));
            assertEquals(true, snap.get("c"));
        }
        @Test void snapshotIsImmutable()     {
            env.set("x", 1.0);
            assertThrows(UnsupportedOperationException.class, () -> env.snapshot().put("x", 999.0));
        }
        @Test void snapshotRemoveIsImmutable() {
            env.set("x", 1.0);
            assertThrows(UnsupportedOperationException.class, () -> env.snapshot().remove("x"));
        }
        @Test void snapshotReflectsLatestValue() {
            env.set("x", 1.0); env.set("x", 2.0);
            assertEquals(2.0, env.snapshot().get("x"));
        }
        @Test void snapshotDoesNotReflectLaterChanges() {
            env.set("x", 1.0);
            Map<String, Object> snap = env.snapshot();
            env.set("x", 2.0);
            // Original snap captured 1.0 (Map.copyOf semantics)
            assertEquals(1.0, snap.get("x"));
        }
        @Test void manyVarsInSnapshot()      {
            for (int i = 0; i < 50; i++) env.set("v" + i, (double)i);
            assertEquals(50, env.snapshot().size());
        }


        @Test
        @DisplayName("snapshot() returns empty map on fresh environment")
        void snapshotEmpty() {
            assertTrue(env.snapshot().isEmpty());
        }


        @Test
        @DisplayName("snapshot() is unmodifiable (throws on mutation)")
        void snapshotIsUnmodifiable() {
            env.set("x", 1.0);
            Map<String, Object> snap = env.snapshot();
            assertThrows(UnsupportedOperationException.class, () -> snap.put("y", 2.0));
        }

        @Test
        @DisplayName("Modifying env after snapshot does not affect the snapshot")
        void snapshotIsIsolated() {
            env.set("x", 1.0);
            Map<String, Object> snap = env.snapshot();
            env.set("x", 99.0);
            assertEquals(1.0, snap.get("x"));
        }


    }

    // ══════════════════════════════════════════════════════════════════════
    // VARIABLE NAME EDGE CASES
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("Variable name edge cases")
    class NameEdgeCases {
        @Test void caseSensitiveLowerUpper() { env.set("x", 1.0); env.set("X", 2.0); assertEquals(1.0,(double)env.get("x")); assertEquals(2.0,(double)env.get("X")); }
        @Test void underscoreInName()        { env.set("my_var", 7.0); assertEquals(7.0, (double)env.get("my_var")); }
        @Test void numberSuffixInName()      { env.set("x1", 3.0); assertEquals(3.0, (double)env.get("x1")); }
        @Test void longName()                {
            String name = "a".repeat(100);
            env.set(name, 5.0);
            assertEquals(5.0, (double)env.get(name));
        }
        @Test void manyDistinctVariables()   {
            for (int i = 0; i < 100; i++) env.set("var" + i, (double)i);
            for (int i = 0; i < 100; i++) assertEquals((double)i, (double)env.get("var" + i));
        }
        @Test void twoInstancesAreIndependent() {
            Environment env2 = new Environment();
            env.set("x", 1.0); env2.set("x", 999.0);
            assertEquals(1.0, (double)env.get("x"));
            assertEquals(999.0, (double)env2.get("x"));
        }
        @Test void freshEnvDoesNotShareState() {
            env.set("x", 5.0);
            Environment env2 = new Environment();
            assertFalse(env2.isDefined("x"));
        }
    }

    // ── null value ───────────────────────────────────────────

    @Test
    @DisplayName("set(name, null) is stored; get returns null")
    void nullValueCanBeStored() {
        env.set("nothing", null);
        assertNull(env.get("nothing"));
    }

    @Test
    @DisplayName("isDefined is true even when value is null")
    void isDefinedTrueForNullValue() {
        env.set("nothing", null);
        assertTrue(env.isDefined("nothing"));
    }



    // TOSTRING
    // ══════════════════════════════════════════════════════════════════════
    @Nested @DisplayName("toString()")
    class ToStringTests {
        @Test void containsEnvironment()     { assertTrue(env.toString().contains("Environment")); }
        @Test void containsVarNameAfterSet() { env.set("myVar", 5.0); assertTrue(env.toString().contains("myVar")); }
        @Test void containsValueAfterSet()   { env.set("x", 42.0); assertTrue(env.toString().contains("42")); }
        @Test void emptyEnvToString()        { assertNotNull(env.toString()); }

        @Test
        @DisplayName("toString() is non-null and non-blank")
        void toStringNonBlank() {
            env.set("x", 1.0);
            assertNotNull(env.toString());
            assertFalse(env.toString().isBlank());
        }
    }
}
