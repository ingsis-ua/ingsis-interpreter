package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.ReadEnv;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;
import org.printscript.interpreter.state.Values;

// readEnv("NOMBRE"): lee una variable de entorno por el puerto de entorno
public final class ReadEnvRule implements ExpressionRule<ReadEnv> {

    @Override
    public Result<Object> evaluate(ReadEnv readEnv, Execution execution) {
        return execution
                .evaluate(readEnv.name())
                .andThen(name -> read(readEnv, Values.asText(name), execution));
    }

    @Override
    public List<Object> children(ReadEnv readEnv) {
        return List.of(readEnv.name());
    }

    private static Result<Object> read(ReadEnv readEnv, String variable, Execution execution) {
        return execution
                .ports()
                .env()
                .read(variable)
                .<Result<Object>>map(Success::new)
                .orElseGet(
                        () ->
                                new Failure<>(
                                        "La variable de entorno " + variable + " no está definida",
                                        readEnv.range()));
    }
}
