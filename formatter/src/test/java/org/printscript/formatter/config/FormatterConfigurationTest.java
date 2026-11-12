package org.printscript.formatter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.config.RulesConfiguration;

// La configuración se mira desde afuera: dos archivos que piden lo mismo dan configuraciones
// iguales, y uno que no pide nada es igual a none().
class FormatterConfigurationTest {

    private static final String AROUND_EQUALS = "enforce-spacing-around-equals";
    private static final String NO_AROUND_EQUALS = "enforce-no-spacing-around-equals";
    private static final String BRACE_SAME_LINE = "if-brace-same-line";
    private static final String BRACE_BELOW_LINE = "if-brace-below-line";
    private static final String BEFORE_COLON = "enforce-spacing-before-colon-in-declaration";
    private static final String PRINTLN_BREAKS = "line-breaks-after-println";
    private static final String INDENT = "indent-inside-if";

    private static FormatterConfiguration from(Map<String, Object> values) {
        return FormatterConfiguration.from(new RulesConfiguration(values));
    }

    @Test
    void anEmptyConfigurationLeavesEveryRuleOff() {
        assertEquals(FormatterConfiguration.none(), from(Map.of()));
    }

    @Test
    void theTwoKeysAboutTheEqualsConfigureTheSameSpace() {
        FormatterConfiguration spaced = from(Map.of(AROUND_EQUALS, true));
        FormatterConfiguration glued = from(Map.of(NO_AROUND_EQUALS, true));

        assertNotEquals(spaced, glued);
        assertNotEquals(FormatterConfiguration.none(), spaced);
        assertNotEquals(FormatterConfiguration.none(), glued);
    }

    @Test
    void askingForSpaceWinsOverAskingForNoneWhenBothArePresent() {
        assertEquals(
                from(Map.of(AROUND_EQUALS, true)),
                from(Map.of(AROUND_EQUALS, true, NO_AROUND_EQUALS, true)));
    }

    @Test
    void theTwoKeysAboutTheBraceConfigureTheSamePlacement() {
        FormatterConfiguration sameLine = from(Map.of(BRACE_SAME_LINE, true));
        FormatterConfiguration below = from(Map.of(BRACE_BELOW_LINE, true));

        assertNotEquals(sameLine, below);
        assertNotEquals(FormatterConfiguration.none(), below);
        assertEquals(sameLine, from(Map.of(BRACE_SAME_LINE, true, BRACE_BELOW_LINE, true)));
    }

    @Test
    void readsTheBooleanRulesAndTheNumericOnes() {
        Map<String, Object> all =
                Map.of(
                        BEFORE_COLON,
                        true,
                        "enforce-spacing-after-colon-in-declaration",
                        true,
                        "mandatory-space-surrounding-operations",
                        true,
                        "mandatory-single-space-separation",
                        true,
                        "mandatory-line-break-after-statement",
                        true,
                        PRINTLN_BREAKS,
                        2,
                        INDENT,
                        4);
        FormatterConfiguration configuration = from(all);

        assertNotEquals(FormatterConfiguration.none(), configuration);
        // cada clave cuenta: sacar cualquiera da otra configuración
        for (String key : all.keySet()) {
            Map<String, Object> withoutIt = new HashMap<>(all);
            withoutIt.remove(key);
            assertNotEquals(configuration, from(withoutIt), key);
        }
        assertNotEquals(from(Map.of(PRINTLN_BREAKS, 2)), from(Map.of(PRINTLN_BREAKS, 3)));
        assertNotEquals(from(Map.of(INDENT, 4)), from(Map.of(INDENT, 2)));
    }

    @Test
    void aFalseBooleanIsTheSameAsAnAbsentRule() {
        assertEquals(FormatterConfiguration.none(), from(Map.of(BEFORE_COLON, false)));
        assertEquals(FormatterConfiguration.none(), from(Map.of(BRACE_SAME_LINE, false)));
    }

    @Test
    void negativeCountsAreRejected() {
        IllegalArgumentException println =
                assertThrows(
                        IllegalArgumentException.class, () -> from(Map.of(PRINTLN_BREAKS, -1)));
        IllegalArgumentException indent =
                assertThrows(IllegalArgumentException.class, () -> from(Map.of(INDENT, -2)));

        assertEquals("El valor no puede ser negativo: -1", println.getMessage());
        assertEquals("El valor no puede ser negativo: -2", indent.getMessage());
    }
}
