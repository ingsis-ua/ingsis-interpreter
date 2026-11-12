package org.printscript.interpreter;

import java.util.Set;
import java.util.function.Predicate;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Result;
import org.printscript.common.Version;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.Executor;
import org.printscript.interpreter.engine.ExpressionEvaluator;
import org.printscript.interpreter.engine.NodeRules;
import org.printscript.interpreter.state.Environment;
import org.printscript.runtime.RuntimePorts;

// arma ejecuciones para los tests sin pasar por el pipeline entero
public final class Run {

    private Run() {}

    public static Execution execution(Environment environment, RuntimePorts ports) {
        return execution(InterpreterFactory.rules(Version.V11), environment, ports);
    }

    public static Execution execution(
            NodeRules rules, Environment environment, RuntimePorts ports) {
        return new Execution(
                environment, ports, new Executor(rules), new ExpressionEvaluator(rules));
    }

    public static Result<Void> statement(Statement statement, RuntimePorts ports) {
        return execution(new Environment(), ports).execute(statement);
    }

    public static Result<Object> expression(
            Expression expression, Environment environment, RuntimePorts ports) {
        return execution(environment, ports).evaluate(expression);
    }

    // todas las reglas de 1.1 salvo las de estos nodos
    public static NodeRules rulesWithout(Class<?>... excluded) {
        Set<Class<?>> skipped = Set.of(excluded);
        return rules(type -> !skipped.contains(type));
    }

    // solo las reglas de estos nodos
    public static NodeRules rulesOnly(Class<?>... kept) {
        Set<Class<?>> included = Set.of(kept);
        return rules(included::contains);
    }

    // se filtra el registro real de la factory: un nodo nuevo registrado ahí aparece acá
    // sin tocar este helper
    private static NodeRules rules(Predicate<Class<?>> keep) {
        return InterpreterFactory.rules(Version.V11).only(keep);
    }
}
