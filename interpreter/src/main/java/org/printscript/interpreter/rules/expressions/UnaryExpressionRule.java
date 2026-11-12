package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.UnaryExpression;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;
import org.printscript.interpreter.state.Values;

// -x: solo tiene sentido sobre un número
public final class UnaryExpressionRule implements ExpressionRule<UnaryExpression> {

    @Override
    public Result<Object> evaluate(UnaryExpression expression, Execution execution) {
        return execution.evaluate(expression.operand()).andThen(value -> negate(value, expression));
    }

    @Override
    public List<Object> children(UnaryExpression expression) {
        return List.of(expression.operand());
    }

    private static Result<Object> negate(Object value, UnaryExpression expression) {
        if (value instanceof Double number) {
            return new Success<>(-number);
        }
        return new Failure<>(
                "No se puede aplicar "
                        + expression.operator().symbol()
                        + " a "
                        + Values.typeOf(value),
                expression.range());
    }
}
