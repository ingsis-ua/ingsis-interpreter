package org.printscript.interpreter;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

// recibe el flujo de sentencias del parser y por cada una llama al Executor
public final class Interpreter implements Iterator<Result<?>> {

    private final Iterator<Result<Statement>> statements;
    private final Executor executor;
    private final Optional<NodeGate> gate;

    public Interpreter(Iterator<Result<Statement>> statements, Executor executor) {
        this.statements = statements;
        this.executor = executor;
        this.gate = Optional.empty();
    }

    public Interpreter(
            Iterator<Result<Statement>> statements,
            Executor executor,
            Set<Class<?>> supportedNodes) {
        this.statements = statements;
        this.executor = executor;
        this.gate = Optional.of(new NodeGate(supportedNodes));
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<?> next() {
        Result<Statement> next = statements.next();
        if (next instanceof Failure<Statement>) {
            return next;
        }
        Statement statement = ((Success<Statement>) next).value();
        Optional<Failure<Void>> rejected = gate.flatMap(nodes -> nodes.check(statement));
        if (rejected.isPresent()) {
            return rejected.get();
        }
        return statement.accept(executor);
    }
}
