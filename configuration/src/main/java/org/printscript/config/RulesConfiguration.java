package org.printscript.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RulesConfiguration {

    private final Map<String, Object> values;

    public RulesConfiguration(Map<String, Object> values) {
        this.values = Map.copyOf(values);
    }

    public Optional<String> getString(String key) {
        return valueOf(key, String.class, "texto");
    }

    public Optional<Boolean> getBoolean(String key) {
        return valueOf(key, Boolean.class, "boolean");
    }

    public Optional<Integer> getInt(String key) {
        return valueOf(key, Integer.class, "un número entero");
    }

    public Set<String> keys() {
        return values.keySet();
    }

    private <T> Optional<T> valueOf(String key, Class<T> type, String written) {
        Object value = values.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "La clave '" + key + "' tiene que ser " + written + " y vino: " + value);
        }
        return Optional.of(type.cast(value));
    }
}
