package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Operator;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;
import org.printscript.interpreter.state.Values;

// una cuenta o una concatenación: primero la izquierda, después la derecha
public final class BinaryExpressionRule implements ExpressionRule<BinaryExpression> {

    @Override
    public Result<Object> evaluate(BinaryExpression expression, Execution execution) {
        return execution
                .evaluate(expression.left())
                .andThen(left -> evaluateRight(expression, left, execution));
    }

    @Override
    public List<Object> children(BinaryExpression expression) {
        return List.of(expression.left(), expression.right());
    }

    private static Result<Object> evaluateRight(
            BinaryExpression expression, Object left, Execution execution) {
        return execution
                .evaluate(expression.right())
                .andThen(right -> combine(left, expression.operator(), right, expression.range()));
    }

    private static Result<Object> combine(
            Object left, Operator operator, Object right, Range range) {
        if (left instanceof Double leftNumber && right instanceof Double rightNumber) {
            if (operator == Operator.DIVIDE && rightNumber == 0) {
                return new Failure<>("División por cero", range);
            }
            return new Success<>(arithmetic(leftNumber, operator, rightNumber));
        }
        if (operator == Operator.PLUS && (left instanceof String || right instanceof String)) {
            return new Success<>(Values.asText(left) + Values.asText(right));
        }
        return new Failure<>(
                "No se puede aplicar "
                        + operator.symbol()
                        + " entre "
                        + Values.typeOf(left)
                        + " y "
                        + Values.typeOf(right),
                range);
    }

    private static double arithmetic(double left, Operator operator, double right) {
        return switch (operator) {
            case PLUS -> left + right;
            case MINUS -> left - right;
            case MULTIPLY -> left * right;
            case DIVIDE -> left / right;
        };
    }
}
