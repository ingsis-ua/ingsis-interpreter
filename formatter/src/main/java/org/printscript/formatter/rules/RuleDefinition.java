package org.printscript.formatter.rules;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.printscript.config.RulesConfiguration;

// Cómo se lee una regla del archivo de reglas: qué claves son suyas, en qué lugar se
// consulta (menor prioridad = antes) y cómo arma la regla a partir de esas claves.
//
// Lee sin validar: los negativos se rechazan después de leer todas las claves, así el
// error que ve el usuario no depende del orden en que se consultan las reglas.
public record RuleDefinition(
        Set<String> keys, int priority, Function<RulesConfiguration, Optional<FormatRule>> reader) {

    public RuleDefinition {
        keys = Set.copyOf(keys);
    }

    public Optional<FormatRule> read(RulesConfiguration rules) {
        return reader.apply(rules);
    }

    // una clave booleana: la regla está si la clave vale true
    public static RuleDefinition flag(String key, int priority, FormatRule rule) {
        return new RuleDefinition(
                Set.of(key),
                priority,
                rules ->
                        rules.getBoolean(key).orElse(false) ? Optional.of(rule) : Optional.empty());
    }

    // un número: la regla está si la clave aparece, aunque valga 0
    public static RuleDefinition count(String key, int priority, IntFunction<FormatRule> rule) {
        return new RuleDefinition(
                Set.of(key), priority, rules -> rules.getInt(key).map(rule::apply));
    }

    // dos claves opuestas sobre lo mismo: si están las dos, manda la primera, y la segunda
    // ni se lee
    public static RuleDefinition either(
            String yesKey, String noKey, int priority, Function<Boolean, FormatRule> rule) {
        return new RuleDefinition(
                Set.of(yesKey, noKey),
                priority,
                rules -> {
                    if (rules.getBoolean(yesKey).orElse(false)) {
                        return Optional.of(rule.apply(true));
                    }
                    if (rules.getBoolean(noKey).orElse(false)) {
                        return Optional.of(rule.apply(false));
                    }
                    return Optional.empty();
                });
    }
}
