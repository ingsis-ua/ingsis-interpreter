package org.printscript.semantic.rules.expressions;

import org.printscript.ast.ReadEnv;
import org.printscript.common.Result;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// readEnv("NOMBRE") vale string
public final class ReadEnvTyping implements ExpressionTyping<ReadEnv> {

    @Override
    public Result<Type> type(ReadEnv readEnv, Checking checking) {
        return StringArgument.of(readEnv.name(), "readEnv", checking);
    }
}
