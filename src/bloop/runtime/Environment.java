package bloop.runtime;

import bloop.exceptions.BloopRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> variables = new HashMap<>();

    // ─────────────── Public API ───────────────

    public void set(String name, Object value) {
        validateName(name);
        variables.put(name, value);
    }

    public Object get(String name) {
        validateName(name);

        if (!variables.containsKey(name)) {
            throw new BloopRuntimeException("Undefined variable: '" + name + "'");
        }

        return variables.get(name);
    }

    public boolean isDefined(String name) {
        validateName(name);
        return variables.containsKey(name);
    }

    // ─────────────── Internal Helpers ───────────────

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BloopRuntimeException("Variable name cannot be null or empty");
        }
    }
}