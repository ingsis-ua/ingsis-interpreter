package org.printscript.linter.rules;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.Statement;
import org.printscript.linter.Violation;
import org.printscript.linter.traversal.PrintlnCollector;

// println recibe un literal o un identificador, no una expresión
public record PrintlnArgumentRule() implements LintRule {

    public static final LintRuleDefinition DEFINITION =
            LintRuleDefinition.flag(
                    "mandatory-variable-or-literal-in-println", new PrintlnArgumentRule());

    private static final PrintlnCollector COLLECTOR = new PrintlnCollector();

    // recorre para encontrar también los println de adentro de un bloque, y reporta
    // todos los incumplimientos
    @Override
    public List<Violation> check(Statement statement) {
        return COLLECTOR.collect(statement).stream()
                .map(PrintlnStatement::expression)
                .filter(argument -> !SimpleArgument.test(argument))
                .map(PrintlnArgumentRule::violationFor)
                .toList();
    }

    private static Violation violationFor(Expression argument) {
        return new Violation(
                "println solo puede llamarse con un identificador o un literal", argument.range());
    }
}
