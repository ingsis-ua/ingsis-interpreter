package org.printscript.semantic.rules.expressions;

import org.printscript.ast.UnaryExpression;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// -x solo tiene sentido sobre un número
public final class UnaryExpressionTyping implements ExpressionTyping<UnaryExpression> {

    @Override
    public Result<Type> type(UnaryExpression unary, Checking checking) {
        return checking.typeOf(unary.operand())
                .andThen(
                        type ->
                                type == Type.NUMBER
                                        ? new Success<>(Type.NUMBER)
                                        : new Failure<>(
                                                "No se puede aplicar "
                                                        + unary.operator().symbol()
                                                        + " a "
                                                        + type.writtenName(),
                                                unary.range()));
    }
}
