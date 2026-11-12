package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.BooleanLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;

final class BooleanLiteralRule implements ExpressionRule<BooleanLiteral> {

    @Override
    public Result<Object> evaluate(BooleanLiteral literal, Execution execution) {
        return new Success<>(literal.value());
    }

    @Override
    public List<Object> children(BooleanLiteral literal) {
        return List.of();
    }
}
