package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.printscript.common.Failure;
import org.printscript.common.Version;
import org.printscript.runtime.OutputPrinter;

class NegationEndToEndTest {

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
    @EnumSource(Version.class)
    void negativeLiteralsAndNegationsRunInEveryVersion(Version version) {
        InMemoryPrinter printer = new InMemoryPrinter();

        List<Failure<?>> failures =
                new ExecutionRunner()
                        .run(
                                List.of(
                                                "let x: number = -5;",
                                                "x = -x;",
                                                "println(2 * -3);",
                                                "println(x);",
                                                "println(-2 * 3 + -(1 - 4));",
                                                "println(\"saldo: \" + -1.5);")
                                        .iterator(),
                                printer,
                                version);

        assertEquals(List.of(), failures);
        assertEquals(List.of("-6", "5", "-3", "saldo: -1.5"), printer.lines);
    }

    @ParameterizedTest
    @EnumSource(Version.class)
    void negatingAStringIsRejectedBeforeRunning(Version version) {
        List<Failure<?>> failures =
                new ValidationRunner().validate(List.of("println(-\"hola\");").iterator(), version);

        assertEquals(1, failures.size());
        assertEquals("No se puede aplicar - a string", failures.getFirst().message());
    }
}
