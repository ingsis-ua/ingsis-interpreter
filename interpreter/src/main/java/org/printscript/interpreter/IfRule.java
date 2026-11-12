package org.printscript.interpreter;

import java.util.ArrayList;
import java.util.List;
import org.printscript.ast.IfStatement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

// if (condición) { ... } con su else opcional
final class IfRule implements StatementRule<IfStatement> {

    @Override
    public Result<Void> execute(IfStatement ifStatement, Execution execution) {
        return execution
                .evaluate(ifStatement.condition())
                .andThen(value -> branch(ifStatement, value, execution));
    }

    @Override
    public List<Object> children(IfStatement ifStatement) {
        List<Object> children = new ArrayList<>();
        children.add(ifStatement.condition());
        children.add(ifStatement.thenBlock());
        ifStatement.elseBranch().ifPresent(children::add);
        return List.copyOf(children);
    }

    private static Result<Void> branch(IfStatement ifStatement, Object value, Execution execution) {
        // semantic ya garantizó que es booleana; si igual no lo es, se falla de forma
        // explícita en vez de elegir una rama por defecto y seguir como si nada
        if (!(value instanceof Boolean taken)) {
            return new Failure<>(
                    "La condición de un if tiene que ser boolean", ifStatement.condition().range());
        }
        if (taken) {
            return execution.execute(ifStatement.thenBlock());
        }
        return ifStatement
                .elseBranch()
                .map(execution::execute)
                .orElseGet(() -> new Success<>(null));
    }
}
