package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Result;

// println(expresión);
final class PrintlnRule implements StatementRule<PrintlnStatement> {

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
