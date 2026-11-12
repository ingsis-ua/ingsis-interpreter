package org.printscript.interpreter.rules.statements;

import java.util.List;
import java.util.Optional;
import org.printscript.ast.AssignmentStatement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.StatementRule;
import org.printscript.interpreter.state.Conversions;
import org.printscript.interpreter.state.Variable;

// x = 5;
public final class AssignmentRule implements StatementRule<AssignmentStatement> {

    @Override
    public Result<Void> execute(AssignmentStatement assignment, Execution execution) {
        return execution
                .evaluate(assignment.expression())
                .andThen(value -> assign(value, assignment, execution));
    }

    @Override
    public List<Object> children(AssignmentStatement assignment) {
        return List.of(assignment.expression());
    }

    private static Result<Void> assign(
            Object value, AssignmentStatement assignment, Execution execution) {
        String name = assignment.name().name();
        Optional<Variable> existing = execution.environment().read(name);
        if (existing.isEmpty()) {
            return new Failure<>("Variable no declarada: " + name, assignment.range());
        }
        // la conversión se aplica siempre; para cualquier inicializador que no sea una
        // lectura externa es un no-op, porque semantic ya garantizó que el tipo coincide
        return Conversions.toDeclaredType(
                        existing.get().declaredType(), value, assignment.expression().range())
                .andThen(converted -> store(name, converted, assignment, execution));
    }

    private static Result<Void> store(
            String name, Object value, AssignmentStatement assignment, Execution execution) {
        return execution
                .environment()
                .assign(name, value)
                .<Result<Void>>map(
                        updated -> {
                            execution.update(updated);
                            return new Success<>(null);
                        })
                .orElseGet(() -> rejected(assignment, execution));
    }

    // assign devuelve vacío por dos motivos distintos y el mensaje tiene que decir cuál
    private static Result<Void> rejected(AssignmentStatement assignment, Execution execution) {
        String name = assignment.name().name();
        if (execution.environment().read(name).isPresent()) {
            return new Failure<>("No se puede reasignar la constante " + name, assignment.range());
        }
        return new Failure<>("Variable no declarada: " + name, assignment.range());
    }
}
