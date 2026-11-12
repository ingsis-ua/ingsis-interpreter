package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.NumberLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;

final class NumberLiteralRule implements ExpressionRule<NumberLiteral> {

    @Override
    public Result<Object> evaluate(NumberLiteral literal, Execution execution) {
        return new Success<>(literal.value());
    }

    @Override
    public List<Object> children(NumberLiteral literal) {
        return List.of();
    }
}
