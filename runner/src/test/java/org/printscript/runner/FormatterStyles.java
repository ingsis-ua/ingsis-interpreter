package org.printscript.runner;

import java.util.HashMap;
import java.util.Map;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.config.FormatterConfiguration;

// Los estilos que usan los tests de punta a punta, nombrados una sola vez.
//
// El formatter solo toca lo que alguna regla pide, así que "el estilo de la consigna" es
// la suma de las reglas que lo componen y no un valor por defecto escondido. Se arman con
// las mismas claves que escribiría el usuario en su archivo de reglas.
final class FormatterStyles {

    private FormatterStyles() {}

    static FormatterConfiguration consigna() {
        return from(consignaRules());
    }

    // la regla mínima: solo el espacio alrededor del '='
    static FormatterConfiguration soloElEspacioDelIgual() {
        return from(Map.of("enforce-spacing-around-equals", true));
    }

    static FormatterConfiguration conSaltosDespuesDelPrintln(int howMany) {
        Map<String, Object> rules = consignaRules();
        rules.put("line-breaks-after-println", howMany);
        return from(rules);
    }

    static FormatterConfiguration consignaConBloques(int indent, boolean braceOnSameLine) {
        Map<String, Object> rules = consignaRules();
        rules.put(braceOnSameLine ? "if-brace-same-line" : "if-brace-below-line", true);
        rules.put("indent-inside-if", indent);
        return from(rules);
    }

    private static Map<String, Object> consignaRules() {
        Map<String, Object> rules = new HashMap<>();
        rules.put("enforce-spacing-around-equals", true); // espacio alrededor del '='
        rules.put("enforce-spacing-after-colon-in-declaration", true); // espacio después del ':'
        rules.put("mandatory-space-surrounding-operations", true); // espacio alrededor de + - * /
        rules.put("mandatory-line-break-after-statement", true); // una sentencia por línea
        return rules;
    }

    private static FormatterConfiguration from(Map<String, Object> rules) {
        return FormatterConfiguration.from(new RulesConfiguration(rules));
    }
}
