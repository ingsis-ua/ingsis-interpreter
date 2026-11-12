package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.ReadInput;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;
import org.printscript.interpreter.state.Values;

// readInput("mensaje"): muestra el mensaje y lee una línea por el puerto de entrada
public final class ReadInputRule implements ExpressionRule<ReadInput> {

    @Override
    public Result<Object> evaluate(ReadInput readInput, Execution execution) {
        return execution
                .evaluate(readInput.prompt())
                .andThen(prompt -> readAfterPrompt(readInput, prompt, execution));
    }

    @Override
    public List<Object> children(ReadInput readInput) {
        return List.of(readInput.prompt());
    }

    // el prompt se escribe sin salto de línea, para que quede en la misma línea que lo
    // que el usuario tipea
    private static Result<Object> readAfterPrompt(
            ReadInput readInput, Object prompt, Execution execution) {
        execution.ports().printer().print(Values.asText(prompt));
        return execution
                .ports()
                .input()
                .readLine()
                .<Result<Object>>map(Success::new)
                .orElseGet(
                        () ->
                                new Failure<>(
                                        "No hay más entrada disponible para readInput",
                                        readInput.range()));
    }
}
