package org.printscript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RulesConfigurationTest {

    private static final String FORMAT = "identifier_format";
    private static final String ENABLED = "enabled";
    private static final String CAMEL = "camel case";
    private static final String BREAKS = "line-breaks-before-println";

    @Test
    void readsAStringValueByKey() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(FORMAT, CAMEL));

        assertEquals(Optional.of(CAMEL), configuration.getString(FORMAT));
    }

    @Test
    void readsABooleanValueByKey() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(ENABLED, true));

        assertEquals(Optional.of(true), configuration.getBoolean(ENABLED));
    }

    @Test
    void aMissingKeyIsEmptyAndNotADefaultValue() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of());

        assertTrue(configuration.getString(FORMAT).isEmpty());
        assertTrue(configuration.getBoolean(ENABLED).isEmpty());
    }

    @Test
    void aKeyPresentAndFalseIsDistinguishableFromAMissingKey() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(ENABLED, false));

        assertEquals(Optional.of(false), configuration.getBoolean(ENABLED));
        assertFalse(configuration.getBoolean(ENABLED).isEmpty());
    }

    @Test
    void aValueOfTheWrongTypeFailsNamingTheKeyTheTypeAndTheValue() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(ENABLED, "si"));

        IllegalArgumentException thrown =
                assertThrows(
                        IllegalArgumentException.class, () -> configuration.getBoolean(ENABLED));

        assertEquals(
                "La clave '" + ENABLED + "' tiene que ser boolean y vino: si", thrown.getMessage());
    }

    @Test
    void theKeysOfTheFileAreAvailable() {
        RulesConfiguration configuration =
                new RulesConfiguration(Map.of(ENABLED, true, FORMAT, CAMEL));

        assertEquals(Set.of(ENABLED, FORMAT), configuration.keys());
    }

    @Test
    void theConfigurationDoesNotChangeWhenTheSourceMapChanges() {
        Map<String, Object> source = new HashMap<>();
        source.put(FORMAT, CAMEL);
        RulesConfiguration configuration = new RulesConfiguration(source);

        source.put(FORMAT, "otra cosa");

        assertEquals(Optional.of(CAMEL), configuration.getString(FORMAT));
    }

    @Test
    void readsAnIntegerValueByKey() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(BREAKS, 2));

        assertEquals(Optional.of(2), configuration.getInt(BREAKS));
    }

    @Test
    void aValueOfAnotherTypeIsNotReadAsAnInteger() {
        RulesConfiguration configuration = new RulesConfiguration(Map.of(BREAKS, CAMEL));

        assertThrows(IllegalArgumentException.class, () -> configuration.getInt(BREAKS));
    }
}
