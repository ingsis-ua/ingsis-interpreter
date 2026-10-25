package org.printscript.cli;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import org.printscript.common.Failure;
import org.printscript.common.Version;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.runner.FormattingRunner;

// Orquesta la operación de formateo para cli: carga las reglas del --config,
// corre el FormattingRunner y escribe el código formateado a un
// archivo (--output) o por consola.
final class Formatting {

    private Formatting() {}

    static ExitCode run(
            Path sourceFile,
            Path configFile,
            Path outputFile,
            PrintStream console,
            PrintStream err,
            Version version) {
        if (!FileArguments.requireExistingRegularFile(sourceFile, err)
                || !FileArguments.requireSeparateOutput(sourceFile, outputFile, err)) {
            return ExitCode.USAGE_ERROR;
        }
        FormatterConfiguration configuration;
        try {
            configuration = FormatterConfiguration.from(ConfigurationFiles.read(configFile, err));
        } catch (IOException | IllegalArgumentException e) {
            err.println("No se pudo leer la configuración: " + e.getMessage());
            return ExitCode.USAGE_ERROR;
        }
        if (configuration.equals(FormatterConfiguration.none())) {
            err.println("Aviso: no hay reglas de formato activas. Usá --config para activarlas.");
        }
        String ending;
        try {
            ending = endsWithLineBreak(sourceFile) ? "\n" : "";
        } catch (IOException e) {
            err.println("No se pudo leer '" + sourceFile + "': " + e.getMessage());
            return ExitCode.USAGE_ERROR;
        }
        Job job = new Job(configuration, version, ending);
        return SourceFileProcessor.read(
                sourceFile, lines -> format(lines, job, outputFile, console, err), err);
    }

    private record Job(FormatterConfiguration configuration, Version version, String ending) {

        List<Failure<?>> format(Iterator<String> lines, Writer destination) throws IOException {
            List<Failure<?>> failures =
                    new FormattingRunner().format(lines, configuration, destination, version);
            destination.write(ending);
            return failures;
        }
    }

    private static boolean endsWithLineBreak(Path sourceFile) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(sourceFile)) {
            if (channel.size() == 0) {
                return false;
            }
            ByteBuffer last = ByteBuffer.allocate(1);
            channel.position(channel.size() - 1).read(last);
            return last.get(0) == '\n';
        }
    }

    private static ExitCode format(
            Iterator<String> lines,
            Job job,
            Path outputFile,
            PrintStream console,
            PrintStream err) {
        if (outputFile == null) {
            return formatToConsole(lines, job, console, err);
        }
        return formatToFile(lines, job, outputFile, err);
    }

    private static ExitCode formatToConsole(
            Iterator<String> lines, Job job, PrintStream console, PrintStream err) {
        // No se cierra la consola: el resto del proceso sigue usándola.
        Writer destination = new OutputStreamWriter(console, StandardCharsets.UTF_8);
        try {
            List<Failure<?>> failures = job.format(lines, destination);
            destination.flush();
            return finish(failures, err);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ExitCode formatToFile(
            Iterator<String> lines, Job job, Path outputFile, PrintStream err) {
        Path target = outputFile.toAbsolutePath();
        Path draft = null;
        try {
            draft = Files.createTempFile(target.getParent(), ".formatting-", ".tmp");
            List<Failure<?>> failures = formatInto(draft, lines, job);
            if (failures.isEmpty()) {
                Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING);
                err.println("Código formateado guardado en " + outputFile);
            }
            return finish(failures, err);
        } catch (IOException e) {
            err.println("No se pudo escribir '" + outputFile + "': " + e.getMessage());
            return ExitCode.USAGE_ERROR;
        } finally {
            discard(draft);
        }
    }

    private static List<Failure<?>> formatInto(Path draft, Iterator<String> lines, Job job)
            throws IOException {
        try (Writer destination = Files.newBufferedWriter(draft, StandardCharsets.UTF_8)) {
            return job.format(lines, destination);
        }
    }

    private static void discard(Path draft) {
        if (draft == null) {
            return;
        }
        try {
            Files.deleteIfExists(draft);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ExitCode finish(List<Failure<?>> failures, PrintStream err) {
        new FailureReporter(err).report(failures);
        return failures.isEmpty() ? ExitCode.SUCCESS : ExitCode.SOURCE_ERRORS;
    }
}
