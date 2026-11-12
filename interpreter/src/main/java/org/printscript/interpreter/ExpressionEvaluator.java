package org.printscript.interpreter;

import org.printscript.ast.Expression;
import org.printscript.common.Result;

// calcula cuánto vale una expresión delegando en la regla registrada para su clase
// no conoce ninguna expresión: agregar una es registrar su regla en InterpreterFactory,
// y esta clase no cambia
public final class ExpressionEvaluator {

    private final NodeRules rules;

    ExpressionEvaluator(NodeRules rules) {
        this.rules = rules;
    }

    Result<Object> evaluate(Expression expression, Execution execution) {
        return rules.expressionRuleFor(expression)
                .map(rule -> rule.evaluate(expression, execution))
                .orElseGet(() -> NodeRules.unsupported(expression, expression.range()));
    }
}
