package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.BooleanLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;

public final class BooleanLiteralRule implements ExpressionRule<BooleanLiteral> {

    @Override
    public Result<Object> evaluate(BooleanLiteral literal, Execution execution) {
        return new Success<>(literal.value());
    }

    @Override
    public List<Object> children(BooleanLiteral literal) {
        return List.of();
    }
}
