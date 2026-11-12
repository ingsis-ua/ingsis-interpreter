package org.printscript.interpreter.engine;

import org.printscript.ast.Statement;
import org.printscript.common.Result;

// ejecuta una sentencia delegando en la regla registrada para su clase
// no conoce ninguna sentencia: agregar una es registrar su regla en InterpreterFactory,
// y esta clase no cambia
public final class Executor {

    private final NodeRules rules;

    public Executor(NodeRules rules) {
        this.rules = rules;
    }

    public Result<Void> execute(Statement statement, Execution execution) {
        return rules.statementRuleFor(statement)
                .map(rule -> rule.execute(statement, execution))
                .orElseGet(() -> NodeRules.unsupported(statement, statement.range()));
    }
}
