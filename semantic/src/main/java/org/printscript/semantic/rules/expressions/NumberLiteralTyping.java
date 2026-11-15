package org.printscript.semantic.rules.expressions;

import org.printscript.ast.NumberLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// un número escrito tal cual
public final class NumberLiteralTyping implements ExpressionTyping<NumberLiteral> {

    @Override
    public Result<Type> type(NumberLiteral literal, Checking checking) {
        return new Success<>(Type.NUMBER);
    }
}
