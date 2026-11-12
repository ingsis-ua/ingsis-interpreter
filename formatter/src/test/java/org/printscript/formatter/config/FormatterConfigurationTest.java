package org.printscript.formatter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String BOOLEAN = "boolean";
    private static final String AFTER_COLON = "enforce-spacing-after-colon-in-declaration";
    private static final String OPERATIONS = "mandatory-space-surrounding-operations";
    private static final String SINGLE_SPACE = "mandatory-single-space-separation";
    private static final String STATEMENT_BREAK = "mandatory-line-break-after-statement";

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
                        AFTER_COLON,
                        true,
                        OPERATIONS,
                        true,
                        SINGLE_SPACE,
                        true,
                        STATEMENT_BREAK,
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

    @Test
    void theKeysAreExactlyTheElevenOfTheRulesFile() {
        assertEquals(
                Set.of(
                        AROUND_EQUALS,
                        NO_AROUND_EQUALS,
                        BEFORE_COLON,
                        AFTER_COLON,
                        OPERATIONS,
                        SINGLE_SPACE,
                        STATEMENT_BREAK,
                        PRINTLN_BREAKS,
                        BRACE_SAME_LINE,
                        BRACE_BELOW_LINE,
                        INDENT),
                FormatterConfiguration.KEYS);
    }

    // el CLI avisa "no hay reglas activas" comparando contra none()
    @Test
    void everyBooleanKeyInFalseIsTheSameAsNoRules() {
        Map<String, Object> allFalse = new HashMap<>();
        for (String key :
                List.of(
                        AROUND_EQUALS,
                        NO_AROUND_EQUALS,
                        BEFORE_COLON,
                        AFTER_COLON,
                        OPERATIONS,
                        SINGLE_SPACE,
                        STATEMENT_BREAK,
                        BRACE_SAME_LINE,
                        BRACE_BELOW_LINE)) {
            allFalse.put(key, false);
        }

        assertEquals(FormatterConfiguration.none(), from(allFalse));
    }

    @Test
    void zeroAndTheOppositeKeysAreRulesToo() {
        assertNotEquals(FormatterConfiguration.none(), from(Map.of(PRINTLN_BREAKS, 0)));
        assertNotEquals(FormatterConfiguration.none(), from(Map.of(INDENT, 0)));
        assertNotEquals(FormatterConfiguration.none(), from(Map.of(NO_AROUND_EQUALS, true)));
        assertNotEquals(FormatterConfiguration.none(), from(Map.of(BRACE_BELOW_LINE, true)));
    }

    // con varios errores en el mismo archivo, el mensaje que ve el usuario no puede cambiar:
    // primero se leen todas las claves y recién después se validan los negativos
    private static String typeError(String key, String type, String value) {
        return "La clave '" + key + "' tiene que ser " + type + " y vino: " + value;
    }

    private static void assertRejected(String message, Map<String, Object> values) {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> from(values));
        assertEquals(message, error.getMessage());
    }

    @Test
    void aTypeErrorIsReportedBeforeANegative() {
        assertRejected(
                typeError(BEFORE_COLON, BOOLEAN, "x"),
                Map.of(PRINTLN_BREAKS, -1, BEFORE_COLON, "x"));
    }

    @Test
    void withTwoTypeErrorsTheEqualsIsReported() {
        assertRejected(
                typeError(AROUND_EQUALS, BOOLEAN, "1"),
                Map.of(SINGLE_SPACE, "x", AROUND_EQUALS, 1));
    }

    @Test
    void withTwoNegativesThePrintlnIsReported() {
        assertRejected(
                "El valor no puede ser negativo: -1", Map.of(INDENT, -2, PRINTLN_BREAKS, -1));
    }

    @Test
    void aTypeErrorInTheIndentWinsOverANegativePrintln() {
        assertRejected(
                typeError(INDENT, "un número entero", "x"),
                Map.of(INDENT, "x", PRINTLN_BREAKS, -1));
    }

    // la clave opuesta solo se lee si la primera no está en true
    @Test
    void theOppositeEqualsKeyIsReadOnlyWhenNeeded() {
        assertEquals(
                from(Map.of(AROUND_EQUALS, true)),
                from(Map.of(AROUND_EQUALS, true, NO_AROUND_EQUALS, "x")));
        assertRejected(
                typeError(NO_AROUND_EQUALS, BOOLEAN, "x"),
                Map.of(AROUND_EQUALS, false, NO_AROUND_EQUALS, "x"));
    }

    @Test
    void theBelowBraceKeyIsReadOnlyWhenNeeded() {
        assertEquals(
                from(Map.of(BRACE_SAME_LINE, true)),
                from(Map.of(BRACE_SAME_LINE, true, BRACE_BELOW_LINE, "x")));
        assertRejected(
                typeError(BRACE_BELOW_LINE, BOOLEAN, "x"),
                Map.of(BRACE_SAME_LINE, false, BRACE_BELOW_LINE, "x"));
    }
}
