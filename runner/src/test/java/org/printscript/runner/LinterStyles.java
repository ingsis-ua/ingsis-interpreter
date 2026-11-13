package org.printscript.runner;

import java.util.Map;
import org.printscript.config.RulesConfiguration;
import org.printscript.linter.config.LinterConfiguration;

// Las configuraciones del linter que usan los tests de punta a punta, nombradas una sola vez
// y armadas con las mismas claves que escribiría el usuario en su archivo de reglas.
final class LinterStyles {

    private LinterStyles() {}

    // camel case para los identificadores y println solo con un identificador o un literal
    static LinterConfiguration camelCaseYPrintln() {
        return from(
                Map.of(
                        "identifier_format",
                        "camel case",
                        "mandatory-variable-or-literal-in-println",
                        true));
    }

    // ninguna regla prendida: solo se revisa la sintaxis
    static LinterConfiguration ninguna() {
        return from(Map.of());
    }

    private static LinterConfiguration from(Map<String, Object> rules) {
        return LinterConfiguration.from(new RulesConfiguration(rules));
    }
}
