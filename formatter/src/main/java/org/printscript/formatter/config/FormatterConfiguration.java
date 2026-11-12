package org.printscript.formatter.config;

import java.util.List;
import java.util.Set;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.rules.FormatRule;

// Las reglas de formato activas, en el orden en que se consultan: gana la primera que opine.
// Todas son opcionales y ninguna tiene un valor por defecto.
//
// No nombra ninguna regla: las conoce el registro (FormatRules), así que agregar una no
// cambia esta clase.
public record FormatterConfiguration(List<FormatRule> rules) {

    // las claves que entiende el formatter, para avisar de las que no usa ninguna regla
    public static final Set<String> KEYS = FormatRules.standard().keys();

    public FormatterConfiguration {
        rules = List.copyOf(rules);
    }

    // nada activado: el archivo sale tal como entró
    public static FormatterConfiguration none() {
        return new FormatterConfiguration(List.of());
    }

    public static FormatterConfiguration from(RulesConfiguration rules) {
        return FormatRules.standard().read(rules);
    }
}
