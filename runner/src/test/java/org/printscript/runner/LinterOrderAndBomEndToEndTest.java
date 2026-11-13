package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.Failure;
import org.printscript.common.Version;
import org.printscript.linter.Violation;
import org.printscript.runtime.OutputPrinter;

class LinterOrderAndBomEndToEndTest {

    @Test
    void theViolationsOfAStatementComeOutInSourceOrder() {
        AnalysisReport report =
                new AnalyzingRunner()
                        .analyze(
                                List.of(
                                                "if (true) {",
                                                "  let bad_name: number = 1;",
                                                "  println(1 + 1);",
                                                "  let other_bad: number = 2;",
                                                "}")
                                        .iterator(),
                                LinterStyles.camelCaseYPrintln(),
                                Version.V11);

        assertEquals(
                List.of(2, 3, 4),
                report.violations().stream()
                        .map(Violation::range)
                        .map(range -> range.start().line())
                        .toList());
    }

    @Test
    void aSourceThatStartsWithAByteOrderMarkRunsNormally() {
        List<String> printed = new ArrayList<>();
        OutputPrinter printer =
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

        List<Failure<?>> failures =
                new ExecutionRunner().run(List.of("﻿println(\"hola\");").iterator(), printer);

        assertEquals(List.of(), failures);
        assertEquals(List.of("hola"), printed);
    }
}
