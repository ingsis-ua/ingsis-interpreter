package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Version;
import org.printscript.runtime.OutputPrinter;
import org.printscript.runtime.RuntimePorts;

class ErrorMessagesEndToEndTest {

    private static final OutputPrinter SILENT =
            new OutputPrinter() {
                @Override
                public void println(String line) {}

                @Override
                public void print(String text) {}
            };

    private static Range range(int line, int start, int end) {
        return new Range(new Position(line, start), new Position(line, end));
    }

    private static List<Failure<?>> validate(String... lines) {
        return new ValidationRunner().validate(List.of(lines).iterator(), Version.V11);
    }

    private static List<Failure<?>> execute(List<String> input, String... lines) {
        Deque<String> pending = new ArrayDeque<>(input);
        RuntimePorts ports =
                new RuntimePorts(
                        SILENT,
                        () -> Optional.ofNullable(pending.poll()),
                        name -> Optional.empty());
        return new ExecutionRunner().run(List.of(lines).iterator(), ports, Version.V11);
    }

    @Test
    void anUnreadableSymbolIsNamedAtItsPosition() {
        Failure<?> failure = validate("let x: number = 5 @ 3;").getFirst();

        assertEquals("Símbolo no reconocido: '@'", failure.message());
        assertEquals(range(1, 19, 19), failure.range());
    }

    @Test
    void anUnrecognizedStatementCoversTheWholeStatement() {
        Failure<?> failure = validate("x y z;").getFirst();

        assertEquals("Sentencia no reconocida", failure.message());
        assertEquals(range(1, 1, 6), failure.range());
    }

    @Test
    void anUnrecognizedStatementInsideABlockCoversThatStatement() {
        Failure<?> failure = validate("if (true) { 5 6; }").getFirst();

        assertEquals("Sentencia no reconocida", failure.message());
        assertEquals(range(1, 13, 16), failure.range());
    }

    @Test
    void aFailedConversionPointsAtTheReadingCall() {
        Failure<?> failure =
                execute(List.of("abc"), "let n: number = readInput(\"n: \");").getFirst();

        assertEquals("'abc' no es un number", failure.message());
        assertEquals(range(1, 17, 32), failure.range());
    }

    @Test
    void usingAVariableWhoseDeclarationFailedExplainsWhy() {
        List<Failure<?>> failures =
                execute(List.of("abc"), "let n: number = readInput(\"n: \");", "println(n);");

        assertEquals(2, failures.size());
        assertEquals(
                "La variable n no tiene valor porque su declaración falló",
                failures.get(1).message());
        assertEquals(range(2, 9, 9), failures.get(1).range());
    }
}
