package org.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigurationProblemsEndToEndTest {

    private static final String JSON_FILE = "reglas.json";

    @TempDir Path dir;

    private Path source;

    @BeforeEach
    void writeSource() throws IOException {
        source = Files.writeString(dir.resolve("programa.ps"), "let a:number=12;\n");
    }

    private CliTestSupport.Result run(String command, String configName, String content)
            throws IOException {
        Path config = Files.writeString(dir.resolve(configName), content);
        return CliTestSupport.run(command, source.toString(), "-c", config.toString());
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "analyzing|{\"identifier_format\": 5}|La clave 'identifier_format' tiene que ser"
                        + " texto y vino: 5",
                "analyzing|{\"mandatory-variable-or-literal-in-println\": \"yes\"}|La clave"
                        + " 'mandatory-variable-or-literal-in-println' tiene que ser boolean",
                "formatting|{\"enforce-spacing-around-equals\": \"true\"}|La clave"
                        + " 'enforce-spacing-around-equals' tiene que ser boolean",
                "formatting|{\"indent-inside-if\": \"2\"}|La clave 'indent-inside-if' tiene que"
                        + " ser un número entero"
            })
    void aKnownKeyWithTheWrongTypeIsAUsageErrorNamingTheKey(
            String command, String content, String expected) throws IOException {
        CliTestSupport.Result result = run(command, JSON_FILE, content);

        assertEquals(2, result.exitCode());
        assertTrue(result.stderr().contains(expected), result.stderr());
    }

    @ParameterizedTest
    @ValueSource(strings = {"formatting", "analyzing"})
    void aKeyThatNoRuleUsesIsWarnedAbout(String command) throws IOException {
        CliTestSupport.Result result =
                run(command, JSON_FILE, "{\"enforce-spacing-arround-equals\": true}");

        assertEquals(0, result.exitCode());
        assertTrue(
                result.stderr()
                        .contains(
                                "Aviso: la clave 'enforce-spacing-arround-equals' no la usa"
                                        + " ninguna regla"),
                result.stderr());
    }

    @Test
    void aSharedFileWithTheKeysOfTheOtherToolIsNotWarnedAbout() throws IOException {
        CliTestSupport.Result result =
                run(
                        "formatting",
                        JSON_FILE,
                        "{\"identifier_format\": \"camel case\","
                                + " \"enforce-spacing-around-equals\": true}");

        assertEquals(0, result.exitCode());
        assertFalse(result.stderr().contains("no la usa"), result.stderr());
    }

    @ParameterizedTest
    @ValueSource(strings = {"reglas.json", "reglas.yaml"})
    void anEmptyFileOnlyWarnsThatNoRuleIsActive(String name) throws IOException {
        CliTestSupport.Result result = run("analyzing", name, "");

        assertEquals(0, result.exitCode());
        assertTrue(result.stderr().contains("no hay reglas"), result.stderr());
    }
}
