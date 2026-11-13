package org.printscript.linter.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.printscript.config.RulesConfiguration;
import org.printscript.linter.rules.IdentifierFormatRule;
import org.printscript.linter.rules.LintRule;
import org.printscript.linter.rules.LintRuleDefinition;
import org.printscript.linter.rules.PrintlnArgumentRule;
import org.printscript.linter.rules.ReadInputArgumentRule;

// El registro de las reglas de lint. Agregar una regla es escribirla y sumar su definición
// acá; nada más cambia.
//
// El orden de esta lista es a la vez el orden en que se leen las claves (y por eso el del
// error que ve el usuario si el archivo trae varios) y el orden en que corren las reglas (y
// por eso el del desempate entre violaciones de la misma posición).
public final class LintRules {

    private final List<LintRuleDefinition> definitions;

    public LintRules(List<LintRuleDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    public static LintRules standard() {
        return new LintRules(
                List.of(
                        IdentifierFormatRule.DEFINITION,
                        PrintlnArgumentRule.DEFINITION,
                        ReadInputArgumentRule.DEFINITION));
    }

    // las reglas nuevas se leen y corren después de las que ya estaban
    public LintRules with(LintRuleDefinition extra) {
        List<LintRuleDefinition> all = new ArrayList<>(definitions);
        all.add(extra);
        return new LintRules(all);
    }

    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        definitions.forEach(definition -> keys.addAll(definition.keys()));
        return Set.copyOf(keys);
    }

    public LinterConfiguration read(RulesConfiguration rules) {
        List<LintRule> active = new ArrayList<>();
        for (LintRuleDefinition definition : definitions) {
            definition.read(rules).ifPresent(active::add);
        }
        return new LinterConfiguration(active);
    }
}
