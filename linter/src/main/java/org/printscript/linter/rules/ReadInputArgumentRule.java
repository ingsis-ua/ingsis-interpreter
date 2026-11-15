package org.printscript.linter.rules;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.ast.ReadInput;
import org.printscript.ast.Statement;
import org.printscript.linter.Violation;
import org.printscript.linter.traversal.ReadInputCollector;

// un identificador o un literal, nunca una expresión armada
public record ReadInputArgumentRule() implements LintRule {

    public static final LintRuleDefinition DEFINITION =
            LintRuleDefinition.flag(
                    "mandatory-variable-or-literal-in-readInput", new ReadInputArgumentRule());

    private static final ReadInputCollector COLLECTOR = new ReadInputCollector();

    @Override
    public List<Violation> check(Statement statement) {
        return COLLECTOR.collect(statement).stream()
                .map(ReadInput::prompt)
                .filter(argument -> !SimpleArgument.test(argument))
                .map(ReadInputArgumentRule::violationFor)
                .toList();
    }

    private static Violation violationFor(Expression argument) {
        return new Violation(
                "readInput solo puede llamarse con un identificador o un literal",
                argument.range());
    }
}
