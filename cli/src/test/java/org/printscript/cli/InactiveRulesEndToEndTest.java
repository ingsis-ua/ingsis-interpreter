package org.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InactiveRulesEndToEndTest {

    private static final String ANALYZING = "analyzing";
    private static final String SOURCE = "programa.ps";
    private static final String PROGRAM = "let a:number=12;\n";
    private static final String WARNING = "no hay reglas";

    @TempDir Path dir;

    @ParameterizedTest
    @ValueSource(strings = {"formatting", ANALYZING})
    void explainsMissingConfiguration(String command) throws IOException {
        Path source = Files.writeString(dir.resolve(SOURCE), PROGRAM);

        CliTestSupport.Result result = CliTestSupport.run(command, source.toString());

        assertEquals(0, result.exitCode());
        assertTrue(result.stderr().contains(WARNING), result.stderr());
        assertTrue(result.stderr().contains("--config"), result.stderr());
    }

    @ParameterizedTest
    @ValueSource(strings = {"formatting", ANALYZING})
    void explainsEmptyConfiguration(String command) throws IOException {
        Path source = Files.writeString(dir.resolve(SOURCE), PROGRAM);
        Path config = Files.writeString(dir.resolve("reglas.json"), "{}");

        CliTestSupport.Result result =
                CliTestSupport.run(command, source.toString(), "-c", config.toString());

        assertEquals(0, result.exitCode());
        assertTrue(result.stderr().contains(WARNING), result.stderr());
    }

    @Test
    void analyzingStillReportsSyntaxErrorsWithoutRules() throws IOException {
        Path source = Files.writeString(dir.resolve(SOURCE), "let a number = 12;\n");

        CliTestSupport.Result result = CliTestSupport.run(ANALYZING, source.toString());

        assertEquals(1, result.exitCode());
        assertTrue(result.stderr().contains("error:"), result.stderr());
        assertTrue(result.stderr().contains("solo se revisa la sintaxis"), result.stderr());
    }

    // reglas en false son lo mismo que ninguna regla
    @Test
    void analyzingWarnsWhenEveryRuleIsOff() throws IOException {
        Path source = Files.writeString(dir.resolve(SOURCE), PROGRAM);
        Path config =
                Files.writeString(
                        dir.resolve("reglas.json"),
                        "{\"mandatory-variable-or-literal-in-println\": false,"
                                + " \"mandatory-variable-or-literal-in-readInput\": false}");

        CliTestSupport.Result result =
                CliTestSupport.run(ANALYZING, source.toString(), "-c", config.toString());

        assertTrue(result.stderr().contains(WARNING), result.stderr());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"identifier_format\": \"camel case\"}",
                "{\"mandatory-variable-or-literal-in-println\": true}",
                "{\"mandatory-variable-or-literal-in-readInput\": true}"
            })
    void analyzingDoesNotWarnWhenARuleIsOn(String rules) throws IOException {
        Path source = Files.writeString(dir.resolve(SOURCE), PROGRAM);
        Path config = Files.writeString(dir.resolve("reglas.json"), rules);

        CliTestSupport.Result result =
                CliTestSupport.run(ANALYZING, source.toString(), "-c", config.toString());

        assertFalse(result.stderr().contains(WARNING), result.stderr());
    }
}
