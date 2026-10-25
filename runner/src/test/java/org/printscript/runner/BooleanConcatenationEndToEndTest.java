package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.Failure;
import org.printscript.common.Version;
import org.printscript.runtime.OutputPrinter;

class BooleanConcatenationEndToEndTest {

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

    @Test
    void aBooleanConcatenatesWithAStringOnEitherSide() {
        InMemoryPrinter printer = new InMemoryPrinter();

        List<Failure<?>> failures =
                new ExecutionRunner()
                        .run(
                                List.of(
                                                "let listo: boolean = false;",
                                                "println(\"v\" + true);",
                                                "println(listo + \" x\");",
                                                "let texto: string = \"es \" + listo;",
                                                "println(texto);")
                                        .iterator(),
                                printer,
                                Version.V11);

        assertEquals(List.of(), failures);
        assertEquals(List.of("vtrue", "false x", "es false"), printer.lines);
    }

    @Test
    void aBooleanStillDoesNotTakePartInArithmetic() {
        List<Failure<?>> failures =
                new ValidationRunner()
                        .validate(
                                List.of("println(1 + true);", "println(true * 2);").iterator(),
                                Version.V11);

        assertEquals(
                List.of(
                        "No se puede aplicar + entre number y boolean",
                        "No se puede aplicar * entre boolean y number"),
                failures.stream().map(Failure::message).toList());
    }
}
