package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.NumberLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;

public final class NumberLiteralRule implements ExpressionRule<NumberLiteral> {

    @Override
    public Result<Object> evaluate(NumberLiteral literal, Execution execution) {
        return new Success<>(literal.value());
    }

    @Override
    public List<Object> children(NumberLiteral literal) {
        return List.of();
    }
}
