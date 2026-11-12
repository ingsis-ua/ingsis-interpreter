package org.printscript.interpreter.rules.statements;

import java.util.List;
import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Result;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.StatementRule;
import org.printscript.interpreter.state.Values;

// println(expresión);
public final class PrintlnRule implements StatementRule<PrintlnStatement> {

    @Override
    public Result<Void> execute(PrintlnStatement println, Execution execution) {
        return execution
                .evaluate(println.expression())
                .map(
                        value -> {
                            execution.ports().printer().println(Values.asText(value));
                            return null;
                        });
    }

    @Override
    public List<Object> children(PrintlnStatement println) {
        return List.of(println.expression());
    }
}
