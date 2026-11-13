package org.printscript.linter.rules;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.printscript.config.RulesConfiguration;

// Cómo se lee una regla de lint del archivo de reglas: qué claves son suyas y cómo arma la
// regla a partir de ellas. Vacío quiere decir que la regla no está prendida.
public record LintRuleDefinition(
        Set<String> keys, Function<RulesConfiguration, Optional<LintRule>> reader) {

    public LintRuleDefinition {
        keys = Set.copyOf(keys);
    }

    public Optional<LintRule> read(RulesConfiguration rules) {
        return reader.apply(rules);
    }

    // una clave booleana: la regla está si la clave vale true
    public static LintRuleDefinition flag(String key, LintRule rule) {
        return new LintRuleDefinition(
                Set.of(key),
                rules ->
                        rules.getBoolean(key).orElse(false) ? Optional.of(rule) : Optional.empty());
    }
}
