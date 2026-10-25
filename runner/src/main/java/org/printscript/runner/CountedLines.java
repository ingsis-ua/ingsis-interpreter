package org.printscript.runner;

import java.util.Iterator;

final class CountedLines implements Iterator<String> {

    private final Iterator<String> lines;
    private int consumed;

    CountedLines(Iterator<String> lines) {
        this.lines = lines;
    }

    @Override
    public boolean hasNext() {
        return lines.hasNext();
    }

    @Override
    public String next() {
        String line = lines.next();
        consumed = consumed + 1;
        return line;
    }

    int count() {
        return consumed;
    }
}
