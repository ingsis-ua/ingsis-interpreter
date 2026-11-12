package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.StringLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;

public final class StringLiteralRule implements ExpressionRule<StringLiteral> {

    @Override
    public Result<Object> evaluate(StringLiteral literal, Execution execution) {
        return new Success<>(literal.value());
    }

    @Override
    public List<Object> children(StringLiteral literal) {
        return List.of();
    }
}
