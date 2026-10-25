package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.runtime.OutputPrinter;

class DivisionByZeroEndToEndTest {

    private static final class InMemoryPrinter implements OutputPrinter {

        private final List<String> lines = new ArrayList<>();

        @Override
        public void println(String line) {
            lines.add(line);
        }

        @Override
        public void print(String text) {
            lines.add(text);
        }
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "println(1 / 0);|9|13",
                "println(0 / 0);|9|13",
                "println(\"z=\" + 5 / 0);|16|20",
                "let c: number = 0; println(4 / c);|28|32"
            })
    void dividingByZeroIsARuntimeErrorAtTheDivision(String program, int start, int end) {
        InMemoryPrinter printer = new InMemoryPrinter();

        List<Failure<?>> failures =
                new ExecutionRunner()
                        .run(List.of(program, "println(\"sigue\");").iterator(), printer);

        assertEquals(1, failures.size());
        assertEquals("División por cero", failures.getFirst().message());
        assertEquals(
                new Range(new Position(1, start), new Position(1, end)),
                failures.getFirst().range());
        assertEquals(List.of("sigue"), printer.lines);
    }
}
