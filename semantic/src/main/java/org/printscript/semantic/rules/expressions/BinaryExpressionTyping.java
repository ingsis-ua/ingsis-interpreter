package org.printscript.semantic.rules.expressions;

import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Operator;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// una cuenta: se tipan los dos lados, izquierdo primero, y se combinan
public final class BinaryExpressionTyping implements ExpressionTyping<BinaryExpression> {

    @Override
    public Result<Type> type(BinaryExpression expression, Checking checking) {
        return checking.typeOf(expression.left())
                .andThen(left -> typeRight(expression, left, checking));
    }

    private static Result<Type> typeRight(
            BinaryExpression expression, Type left, Checking checking) {
        return checking.typeOf(expression.right())
                .andThen(right -> combine(left, expression.operator(), right, expression));
    }

    // la concatenación es el único caso donde los dos lados pueden tener tipos distintos
    private static Result<Type> combine(
            Type left, Operator operator, Type right, BinaryExpression expression) {
        boolean anyString = left == Type.STRING || right == Type.STRING;
        if (operator == Operator.PLUS && anyString) {
            return new Success<>(Type.STRING);
        }
        if (left == Type.BOOLEAN || right == Type.BOOLEAN) {
            return arithmeticFailure(operator, left, right, expression.range());
        }
        if (operator == Operator.PLUS) {
            return new Success<>(Type.NUMBER);
        }
        if (left != Type.NUMBER || right != Type.NUMBER) {
            return arithmeticFailure(operator, left, right, expression.range());
        }
        return new Success<>(Type.NUMBER);
    }

    // mismo mensaje y mismo rango que usaba el intérprete para este error: la etapa
    // cambia, pero lo que ve el usuario tiene que seguir siendo lo mismo
    private static Result<Type> arithmeticFailure(
            Operator operator, Type left, Type right, Range range) {
        return new Failure<>(
                "No se puede aplicar "
                        + operator.symbol()
                        + " entre "
                        + left.writtenName()
                        + " y "
                        + right.writtenName(),
                range);
    }
}
