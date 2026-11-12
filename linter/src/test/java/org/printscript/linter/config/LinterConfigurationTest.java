package org.printscript.linter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.config.RulesConfiguration;

// La configuración se mira desde afuera: dos archivos que piden lo mismo dan configuraciones
// iguales, y uno que no pide nada es igual al archivo vacío.
class LinterConfigurationTest {

    private static final String FORMAT_KEY = "identifier_format";
    private static final String PRINTLN_KEY = "mandatory-variable-or-literal-in-println";
    private static final String READ_INPUT_KEY = "mandatory-variable-or-literal-in-readInput";
    private static final String CAMEL = "camel case";
    private static final String SNAKE = "snake case";

    private static LinterConfiguration from(Map<String, Object> values) {
        return LinterConfiguration.from(new RulesConfiguration(values));
    }

    private static LinterConfiguration nothing() {
        return from(Map.of());
    }

    @Test
    void anEmptyFileLeavesEveryRuleOff() {
        assertEquals(nothing(), from(Map.of(PRINTLN_KEY, false, READ_INPUT_KEY, false)));
    }

    @Test
    void theIdentifierFormatIsReadFromTheFile() {
        LinterConfiguration camel = from(Map.of(FORMAT_KEY, CAMEL));

        assertNotEquals(nothing(), camel);
        assertNotEquals(from(Map.of(FORMAT_KEY, SNAKE)), camel);
    }

    @Test
    void theIdentifierFormatIsAcceptedInItsUsualSpellings() {
        LinterConfiguration camel = from(Map.of(FORMAT_KEY, CAMEL));

        assertEquals(camel, from(Map.of(FORMAT_KEY, "camelCase")));
        assertEquals(camel, from(Map.of(FORMAT_KEY, "camel_case")));
        assertEquals(camel, from(Map.of(FORMAT_KEY, "CAMEL CASE")));
    }

    @Test
    void thePrintlnRuleIsReadFromTheFile() {
        assertNotEquals(nothing(), from(Map.of(PRINTLN_KEY, true)));
        assertEquals(nothing(), from(Map.of(PRINTLN_KEY, false)));
    }

    @Test
    void anUnknownIdentifierFormatFailsInsteadOfTurningTheRuleOffInSilence() {
        Map<String, Object> values = Map.of(FORMAT_KEY, "kebab case");

        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> from(values));

        assertTrue(thrown.getMessage().contains("kebab case"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains(CAMEL), thrown.getMessage());
    }

    // el TCK trae casos con las dos convenciones, así que las dos tienen que resolverse
    @Test
    void snakeCaseIsSupportedInItsThreeSpellings() {
        LinterConfiguration snake = from(Map.of(FORMAT_KEY, SNAKE));

        assertNotEquals(nothing(), snake);
        for (String spelling : new String[] {"snakecase", "snake_case"}) {
            assertEquals(snake, from(Map.of(FORMAT_KEY, spelling)), spelling);
        }
    }

    @Test
    void theReadInputRuleIsOffUnlessItsKeyIsPresent() {
        assertNotEquals(nothing(), from(Map.of(READ_INPUT_KEY, true)));
        assertNotEquals(from(Map.of(PRINTLN_KEY, true)), from(Map.of(READ_INPUT_KEY, true)));
    }
}
