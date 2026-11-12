package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.UnaryExpression;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

// -x: solo tiene sentido sobre un número
final class UnaryExpressionRule implements ExpressionRule<UnaryExpression> {

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
