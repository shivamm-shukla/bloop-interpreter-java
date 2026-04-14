package bloop.runtime;

import bloop.exceptions.BloopRuntimeException;

import java.util.HashMap;
import java.util.Map;

public final class Environment {

    private final Map<String, Object> variableStore = new HashMap<>();

    public void set(String name, Object value) {
        variableStore.put(name, value);
    }


    public Object get(String name) {
        if (!variableStore.containsKey(name)) {
            throw new BloopRuntimeException(
                    "Variable '" + name + "' is used before it was assigned");
        }
        return variableStore.get(name);
    }

    // True if the variable has been assigned at least once
    public boolean isDefined(String name) {
        return variableStore.containsKey(name);
    }

    // Returns a read-only snapshot of all variables (for debugging)
    public Map<String, Object> snapshot() {
        return Map.copyOf(variableStore);
    }

    @Override
    public String toString() {
        return "Environment" + variableStore;
    }
}
