package org.printscript.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.printscript.config.ConfigurationReader;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.linter.config.LinterConfiguration;

// Lee el archivo de reglas del usuario (JSON/YAML) hacia una RulesConfiguration genérica.
// Lo comparten las operaciones que se configuran con reglas:
// analyzing (linter) y formatting (formatter).
final class ConfigurationFiles {

    private ConfigurationFiles() {}

    static RulesConfiguration read(Path configFile, PrintStream err) throws IOException {
        RulesConfiguration rules = read(configFile);
        rules.keys().stream()
                .filter(key -> !FormatterConfiguration.KEYS.contains(key))
                .filter(key -> !LinterConfiguration.KEYS.contains(key))
                .sorted()
                .forEach(
                        key ->
                                err.println(
                                        "Aviso: la clave '" + key + "' no la usa ninguna regla"));
        return rules;
    }

    // configFile null: configuración vacía, así cada regla cae en su valor por defecto
    static RulesConfiguration read(Path configFile) throws IOException {
        if (configFile == null) {
            return new RulesConfiguration(Map.of());
        }
        if (!Files.isRegularFile(configFile)) {
            throw new IllegalArgumentException(
                    "el archivo de configuración '" + configFile + "' no existe");
        }
        String format = formatOf(configFile);
        try (InputStream source = Files.newInputStream(configFile)) {
            return ConfigurationReader.standard().read(source, format);
        }
    }

    private static String formatOf(Path configFile) {
        String name = configFile.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException(
                    "el archivo de configuración no tiene extensión (.json/.yaml/.yml): "
                            + configFile);
        }
        return name.substring(dot + 1);
    }
}
