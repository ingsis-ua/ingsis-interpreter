package org.printscript.semantic.rules.expressions;

import org.printscript.ast.ReadInput;
import org.printscript.common.Result;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// readInput("mensaje") vale string
public final class ReadInputTyping implements ExpressionTyping<ReadInput> {

    @Override
    public Result<Type> type(ReadInput readInput, Checking checking) {
        return StringArgument.of(readInput.prompt(), "readInput", checking);
    }
}
