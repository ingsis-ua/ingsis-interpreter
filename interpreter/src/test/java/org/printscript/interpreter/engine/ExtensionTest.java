package org.printscript.interpreter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.Ports;
import org.printscript.interpreter.Run;
import org.printscript.interpreter.rules.expressions.NumberLiteralRule;
import org.printscript.interpreter.state.Environment;
import org.printscript.runtime.OutputPrinter;

// el criterio de aceptación del issue #60: agregar un nodo es escribir su regla y
// registrarla. El Executor no se toca y la ejecuta igual
class ExtensionTest {

    private static final Range RANGE = new Range(new Position(1, 1), new Position(1, 4));

    private final List<String> printed = new ArrayList<>();

    private final OutputPrinter printer =
            new OutputPrinter() {
                @Override
                public void println(String line) {
                    printed.add(line);
                }

                @Override
                public void print(String text) {
                    printed.add(text);
                }
            };

    private static final class NewPrintlnRule implements StatementRule<PrintlnStatement> {

        @Override
        public Result<Void> execute(PrintlnStatement statement, Execution execution) {
            execution.ports().printer().println("regla nueva");
            return new Success<>(null);
        }

        @Override
        public List<Object> children(PrintlnStatement statement) {
            return List.of(statement.expression());
        }
    }

    @Test
    void aNewRuleIsDispatchedWithoutChangingTheExecutor() {
        NodeRules rules =
                NodeRules.builder()
                        .statement(PrintlnStatement.class, new NewPrintlnRule())
                        .expression(NumberLiteral.class, new NumberLiteralRule())
                        .build();
        Execution execution = Run.execution(rules, new Environment(), Ports.of(printer));

        Result<Void> result =
                new Executor(rules)
                        .execute(
                                new PrintlnStatement(new NumberLiteral(1, RANGE), RANGE),
                                execution);

        assertInstanceOf(Success.class, result);
        assertEquals(List.of("regla nueva"), printed);
    }
}
