package org.printscript.interpreter.rules.expressions;

import java.util.List;
import org.printscript.ast.Identifier;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.ExpressionRule;
import org.printscript.interpreter.state.FailedDeclaration;
import org.printscript.interpreter.state.Variable;

// el valor actual de una variable
public final class IdentifierRule implements ExpressionRule<Identifier> {

    @Override
    public Result<Object> evaluate(Identifier identifier, Execution execution) {
        return execution
                .environment()
                .read(identifier.name())
                .<Result<Object>>map(variable -> valueOf(identifier, variable))
                .orElseGet(
                        () ->
                                new Failure<>(
                                        "Variable no declarada: " + identifier.name(),
                                        identifier.range()));
    }

    @Override
    public List<Object> children(Identifier identifier) {
        return List.of();
    }

    private static Result<Object> valueOf(Identifier identifier, Variable variable) {
        if (variable.value() == FailedDeclaration.INSTANCE) {
            return new Failure<>(
                    "La variable "
                            + identifier.name()
                            + " no tiene valor porque su declaración falló",
                    identifier.range());
        }
        return new Success<>(variable.value());
    }
}
