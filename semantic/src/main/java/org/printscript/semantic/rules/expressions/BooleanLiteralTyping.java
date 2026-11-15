package org.printscript.semantic.rules.expressions;

import org.printscript.ast.BooleanLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// true o false
public final class BooleanLiteralTyping implements ExpressionTyping<BooleanLiteral> {

    @Override
    public Result<Type> type(BooleanLiteral literal, Checking checking) {
        return new Success<>(Type.BOOLEAN);
    }
}
