package org.printscript.linter.config;

import java.util.List;
import java.util.Set;
import org.printscript.config.RulesConfiguration;
import org.printscript.linter.rules.LintRule;

// Las reglas de lint activas, en el orden en que corren.
//
// No nombra ninguna regla: las conoce el registro (LintRules), así que agregar una no cambia
// esta clase.
public record LinterConfiguration(List<LintRule> rules) {

    // las claves que entiende el linter, para avisar de las que no usa ninguna regla
    public static final Set<String> KEYS = LintRules.standard().keys();

    public LinterConfiguration {
        rules = List.copyOf(rules);
    }

    // nada activado: solo se revisa la sintaxis
    public static LinterConfiguration none() {
        return new LinterConfiguration(List.of());
    }

    public static LinterConfiguration from(RulesConfiguration rules) {
        return LintRules.standard().read(rules);
    }
}
