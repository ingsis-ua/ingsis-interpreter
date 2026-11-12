package org.printscript.interpreter.engine;

import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Result;
import org.printscript.interpreter.state.Environment;
import org.printscript.runtime.RuntimePorts;

// el estado de una ejecución que reciben las reglas: las variables vivas, los puertos
// y la forma de ejecutar o evaluar un nodo hijo sin saber qué regla le toca
public final class Execution {

    private final RuntimePorts runtimePorts;
    private final Executor executor;
    private final ExpressionEvaluator evaluator;
    private Environment current;

    public Execution(
            Environment environment,
            RuntimePorts ports,
            Executor executor,
            ExpressionEvaluator evaluator) {
        this.current = environment;
        this.runtimePorts = ports;
        this.executor = executor;
        this.evaluator = evaluator;
    }

    public Environment environment() {
        return current;
    }

    // Environment es inmutable: declarar, asignar o abrir un scope devuelve uno nuevo
    public void update(Environment updated) {
        this.current = updated;
    }

    public RuntimePorts ports() {
        return runtimePorts;
    }

    public Result<Void> execute(Statement statement) {
        return executor.execute(statement, this);
    }

    public Result<Object> evaluate(Expression expression) {
        return evaluator.evaluate(expression, this);
    }
}
