package org.printscript.cli;

import java.io.PrintStream;
import org.printscript.runtime.OutputPrinter;

// el puerto por el que el programa en ejecución habla con el mundo de afuera.
// escribe en System.out
final class ConsoleOutputPrinter implements OutputPrinter {

    private final PrintStream target;
    private final ProgressLine progress;

    ConsoleOutputPrinter() {
        this(System.out);
    }

    ConsoleOutputPrinter(PrintStream target) {
        this(target, ProgressLine.disabled());
    }

    ConsoleOutputPrinter(PrintStream target, ProgressLine progress) {
        this.target = target;
        this.progress = progress;
    }

    @Override
    public void println(String line) {
        progress.clear();
        target.println(line);
    }

    @Override
    public void print(String text) {
        progress.clear();
        target.print(text);
        target.flush();
    }
}
