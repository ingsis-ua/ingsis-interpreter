package org.printscript.semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

// valida que un programa sintácticamente correcto además tenga sentido
public final class SemanticAnalyzer implements Iterator<Result<Statement>> {

    private final Iterator<Result<Statement>> statements;
    private final StatementChecker checker;
    private final Deque<Result<Statement>> pending = new ArrayDeque<>();

    public SemanticAnalyzer(Iterator<Result<Statement>> statements, StatementChecker checker) {
        this.statements = statements;
        this.checker = checker;
    }

    @Override
    public boolean hasNext() {
        return !pending.isEmpty() || statements.hasNext();
    }

    @Override
    public Result<Statement> next() {
        if (!pending.isEmpty()) {
            return pending.removeFirst();
        }
        Result<Statement> result = statements.next();
        // los errores de sintaxis viajan tal cual: no se re-empaquetan ni pierden su rango
        if (!(result instanceof Success<Statement> success)) {
            return result;
        }
        List<Failure<Void>> failures = checker.failuresOf(success.value());
        if (failures.isEmpty()) {
            return result;
        }
        failures.forEach(failure -> pending.add(new Failure<>(failure.message(), failure.range())));
        return pending.removeFirst();
    }
}
