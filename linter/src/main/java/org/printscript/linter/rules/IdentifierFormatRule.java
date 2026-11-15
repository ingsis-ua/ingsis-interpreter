package org.printscript.linter.rules;

import java.util.List;
import java.util.Set;
import org.printscript.ast.Identifier;
import org.printscript.ast.Statement;
import org.printscript.linter.Violation;
import org.printscript.linter.rules.format.IdentifierFormatChecker;
import org.printscript.linter.rules.format.IdentifierFormats;
import org.printscript.linter.traversal.IdentifierCollector;

// que los nombres cumplan un formato
public record IdentifierFormatRule(IdentifierFormatChecker checker) implements LintRule {

    private static final String KEY = "identifier_format";
    private static final IdentifierCollector COLLECTOR = new IdentifierCollector();

    public static final LintRuleDefinition DEFINITION = definition(IdentifierFormats.standard());

    // el formato se busca en el registro: uno nuevo se suma ahí, no acá
    public static LintRuleDefinition definition(IdentifierFormats formats) {
        return new LintRuleDefinition(
                Set.of(KEY),
                rules ->
                        rules.getString(KEY)
                                .map(value -> new IdentifierFormatRule(formatOf(value, formats))));
    }

    private static IdentifierFormatChecker formatOf(String value, IdentifierFormats formats) {
        return formats.find(value)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Formato de identificador no soportado: '"
                                                + value
                                                + "'. Soportados: "
                                                + formats.supported()));
    }

    @Override
    public List<Violation> check(Statement statement) {
        return COLLECTOR.collect(statement).stream()
                .filter(identifier -> !checker.isValid(identifier.name()))
                .map(this::violationFor)
                .toList();
    }

    private Violation violationFor(Identifier identifier) {
        return new Violation(
                "El identificador '"
                        + identifier.name()
                        + "' debe estar escrito en "
                        + checker.expectedFormat(),
                identifier.range());
    }
}
