package org.printscript.interpreter;

import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Result;
import org.printscript.runtime.RuntimePorts;

// el estado de una ejecución que reciben las reglas: las variables vivas, los puertos
// y la forma de ejecutar o evaluar un nodo hijo sin saber qué regla le toca
final class Execution {

    private final RuntimePorts runtimePorts;
    private final Executor executor;
    private final ExpressionEvaluator evaluator;
    private Environment current;

    Execution(
            Environment environment,
            RuntimePorts ports,
            Executor executor,
            ExpressionEvaluator evaluator) {
        this.current = environment;
        this.runtimePorts = ports;
        this.executor = executor;
        this.evaluator = evaluator;
    }

    Environment environment() {
        return current;
    }

    // Environment es inmutable: declarar, asignar o abrir un scope devuelve uno nuevo
    void update(Environment updated) {
        this.current = updated;
    }

    RuntimePorts ports() {
        return runtimePorts;
    }

    Result<Void> execute(Statement statement) {
        return executor.execute(statement, this);
    }

    Result<Object> evaluate(Expression expression) {
        return evaluator.evaluate(expression, this);
    }
}
