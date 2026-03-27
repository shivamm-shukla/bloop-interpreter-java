package bloop.runtime;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void set(String name, Object value) {
        values.put(name, value);
    }

    public Object get(String name) {
        if (!values.containsKey(name)) {
            throw new RuntimeException("Variable not defined: " + name);
        }
        return values.get(name);
    }
}
