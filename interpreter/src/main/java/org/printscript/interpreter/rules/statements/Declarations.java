package org.printscript.interpreter.rules.statements;

import java.util.List;
import java.util.Optional;
import org.printscript.ast.Expression;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.state.Conversions;
import org.printscript.interpreter.state.FailedDeclaration;

// lo que comparten let y const: evaluar el inicializador si lo hay y guardar la variable
final class Declarations {

    private Declarations() {}

    static Result<Void> declare(
            String name,
            String declaredType,
            Optional<Expression> initializer,
            Range range,
            boolean immutable,
            Execution execution) {
        if (initializer.isEmpty()) {
            return store(name, declaredType, null, range, immutable, execution);
        }
        Result<Object> value =
                execution
                        .evaluate(initializer.get())
                        .andThen(
                                evaluated ->
                                        Conversions.toDeclaredType(
                                                declaredType,
                                                evaluated,
                                                initializer.get().range()));
        if (value instanceof Failure<Object> failure) {
            // se declara igual, así un error no arrastra a las sentencias que la usan
            store(name, declaredType, FailedDeclaration.INSTANCE, range, immutable, execution);
            return new Failure<>(failure.message(), failure.range());
        }
        return store(
                name, declaredType, ((Success<Object>) value).value(), range, immutable, execution);
    }

    static List<Object> childrenOf(Optional<Expression> initializer) {
        return initializer.<List<Object>>map(List::of).orElseGet(List::of);
    }

    private static Result<Void> store(
            String name,
            String declaredType,
            Object value,
            Range range,
            boolean immutable,
            Execution execution) {
        return execution
                .environment()
                .declare(name, declaredType, value, immutable)
                .<Result<Void>>map(
                        updated -> {
                            execution.update(updated);
                            return new Success<>(null);
                        })
                .orElseGet(() -> new Failure<>("Variable ya declarada: " + name, range));
    }
}
