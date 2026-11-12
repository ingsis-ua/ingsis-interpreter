package org.printscript.formatter.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.rules.BracePlacement;
import org.printscript.formatter.rules.EqualsSpacing;
import org.printscript.formatter.rules.FormatRule;
import org.printscript.formatter.rules.IfIndent;
import org.printscript.formatter.rules.OperationsSpacing;
import org.printscript.formatter.rules.PrintlnBreaks;
import org.printscript.formatter.rules.RuleDefinition;
import org.printscript.formatter.rules.SingleSpace;
import org.printscript.formatter.rules.SpaceAfterColon;
import org.printscript.formatter.rules.SpaceBeforeColon;
import org.printscript.formatter.rules.StatementBreak;

// El registro de las reglas de formato. Agregar una regla es escribirla y sumar su
// definición acá; nada más cambia.
//
// Hay dos órdenes y no se mezclan:
// - el de esta lista, que es el orden en que se leen las claves (y por eso el del error que
//   ve el usuario si el archivo trae varios);
// - la prioridad de cada definición, que es el orden en que se consultan al formatear.
public final class FormatRules {

    private final List<RuleDefinition> inReadingOrder;

    public FormatRules(List<RuleDefinition> inReadingOrder) {
        this.inReadingOrder = List.copyOf(inReadingOrder);
    }

    public static FormatRules standard() {
        return new FormatRules(
                List.of(
                        EqualsSpacing.DEFINITION,
                        SpaceBeforeColon.DEFINITION,
                        SpaceAfterColon.DEFINITION,
                        OperationsSpacing.DEFINITION,
                        SingleSpace.DEFINITION,
                        StatementBreak.DEFINITION,
                        PrintlnBreaks.DEFINITION,
                        BracePlacement.DEFINITION,
                        IfIndent.DEFINITION));
    }

    // las reglas nuevas se leen después de las que ya estaban
    public FormatRules with(RuleDefinition extra) {
        List<RuleDefinition> definitions = new ArrayList<>(inReadingOrder);
        definitions.add(extra);
        return new FormatRules(definitions);
    }

    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        inReadingOrder.forEach(definition -> keys.addAll(definition.keys()));
        return Set.copyOf(keys);
    }

    // primero se leen todas las claves y recién después se validan, como siempre: con
    // varios errores en el archivo, el que se informa no cambia
    public FormatterConfiguration read(RulesConfiguration rules) {
        List<Read> read = new ArrayList<>();
        for (RuleDefinition definition : inReadingOrder) {
            definition
                    .read(rules)
                    .ifPresent(rule -> read.add(new Read(definition.priority(), rule)));
        }
        read.forEach(each -> each.rule().validate());
        return new FormatterConfiguration(
                read.stream()
                        .sorted(Comparator.comparingInt(Read::priority))
                        .map(Read::rule)
                        .toList());
    }

    private record Read(int priority, FormatRule rule) {}
}
