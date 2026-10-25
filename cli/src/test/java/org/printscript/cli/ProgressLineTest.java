package org.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProgressLineTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream err = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String written() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void clearingErasesTheLineSoTheNextOutputStartsClean() {
        ProgressLine progress = ProgressLine.enabled(err);

        progress.show(33);
        progress.clear();

        assertEquals("\rProgreso: 33%\r" + " ".repeat("Progreso: 33%".length()) + "\r", written());
    }

    @Test
    void afterBeingClearedTheSamePercentIsShownAgain() {
        ProgressLine progress = ProgressLine.enabled(err);

        progress.show(33);
        progress.clear();
        progress.show(33);

        assertTrue(written().endsWith("\rProgreso: 33%"), written());
    }

    @Test
    void finishingAlwaysEndsAtOneHundredWithALineBreak() {
        ProgressLine progress = ProgressLine.enabled(err);

        progress.show(92);
        progress.finish();
        progress.finish();

        assertEquals("\rProgreso: 92%\rProgreso: 100%\n", written());
    }

    @Test
    void aDisabledLineWritesNothing() {
        ProgressLine progress = ProgressLine.disabled();

        progress.show(50);
        progress.clear();
        progress.finish();

        assertEquals("", written());
    }

    @Test
    void aSourceWithCarriageReturnsStillReachesOneHundred() {
        List<String> lines = List.of("println(1);", "println(2);");
        long bytesWithCrlf = "println(1);\r\nprintln(2);\r\n".length();
        ProgressReportingIterator iterator =
                new ProgressReportingIterator(
                        lines.iterator(), bytesWithCrlf, ProgressLine.enabled(err));

        while (iterator.hasNext()) {
            iterator.next();
        }

        assertTrue(written().endsWith("\rProgreso: 100%\n"), written());
    }

    @Test
    void theConsolePrinterClearsTheProgressBeforeWritingTheProgramOutput() {
        ProgressLine progress = ProgressLine.enabled(err);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConsoleOutputPrinter printer =
                new ConsoleOutputPrinter(
                        new PrintStream(out, true, StandardCharsets.UTF_8), progress);

        progress.show(40);
        printer.println("hola");

        assertTrue(written().endsWith("\r"), written());
        assertEquals("hola" + System.lineSeparator(), out.toString(StandardCharsets.UTF_8));
    }
}
