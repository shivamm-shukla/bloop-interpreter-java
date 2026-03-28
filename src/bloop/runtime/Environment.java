package bloop.runtime;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> store = new HashMap<>();

    public void set(String name, Object value) {
        store.put(name, value);
    }

    public Object get(String name) {
        if (!store.containsKey(name)) {
            throw new RuntimeException("Variable not defined: " + name);
        }
        return store.get(name);
    }
}
