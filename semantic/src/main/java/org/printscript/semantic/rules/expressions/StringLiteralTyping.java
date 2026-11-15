package org.printscript.semantic.rules.expressions;

import org.printscript.ast.StringLiteral;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// un texto entre comillas
public final class StringLiteralTyping implements ExpressionTyping<StringLiteral> {

    @Override
    public Result<Type> type(StringLiteral literal, Checking checking) {
        return new Success<>(Type.STRING);
    }
}
