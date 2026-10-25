package org.printscript.cli;

import java.io.Console;
import java.io.PrintStream;

final class ProgressLine {

    private static final int COMPLETE = 100;

    private final PrintStream target;
    private int shownPercent = -1;
    private boolean visible;
    private boolean finished;

    private ProgressLine(PrintStream target) {
        this.target = target;
    }

    static ProgressLine enabled(PrintStream target) {
        return new ProgressLine(target);
    }

    static ProgressLine disabled() {
        return new ProgressLine(null);
    }

    static ProgressLine forTerminal(PrintStream target) {
        Console console = System.console();
        return console != null && console.isTerminal() ? enabled(target) : disabled();
    }

    void show(int percent) {
        if (target == null || finished || visible && percent == shownPercent) {
            return;
        }
        target.print("\r" + text(percent));
        target.flush();
        shownPercent = percent;
        visible = true;
    }

    void clear() {
        if (target == null || !visible) {
            return;
        }
        target.print("\r" + " ".repeat(text(shownPercent).length()) + "\r");
        target.flush();
        visible = false;
    }

    void finish() {
        if (target == null || finished) {
            return;
        }
        show(COMPLETE);
        target.println();
        finished = true;
        visible = false;
    }

    private static String text(int percent) {
        return "Progreso: " + percent + "%";
    }
}
