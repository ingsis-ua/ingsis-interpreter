package org.printscript.interpreter;

import java.util.Iterator;
import java.util.Optional;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.runtime.RuntimePorts;

// recibe el flujo de sentencias del parser y ejecuta una por vez
public final class Interpreter implements Iterator<Result<?>> {

    private final Iterator<Result<Statement>> statements;
    private final Execution execution;
    private final NodeGate gate;

    Interpreter(Iterator<Result<Statement>> statements, NodeRules rules, RuntimePorts ports) {
        this.statements = statements;
        this.execution =
                new Execution(
                        new Environment(),
                        ports,
                        new Executor(rules),
                        new ExpressionEvaluator(rules));
        this.gate = new NodeGate(rules);
    }

    @Override
    public boolean hasNext() {
        return statements.hasNext();
    }

    @Override
    public Result<?> next() {
        Result<Statement> next = statements.next(); // SemanticAnalyzer.next()
        if (next instanceof Failure<Statement>) {
            return next;
        }
        Statement statement = ((Success<Statement>) next).value();
        Optional<Failure<Void>> rejected = gate.check(statement);
        if (rejected.isPresent()) {
            return rejected.get();
        }
        return execution.execute(statement);
    }
}
