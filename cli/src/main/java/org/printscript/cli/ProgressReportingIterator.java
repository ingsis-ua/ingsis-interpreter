package org.printscript.cli;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

// reporta el progreso del parseo estimando el total consumido en bytes contra el tamaño del archivo
final class ProgressReportingIterator implements Iterator<String> {

    private static final int MAX_PERCENT = 100;

    private final Iterator<String> delegate;
    private final long totalBytes;
    private final ProgressLine progress;
    private long consumedBytes;

    ProgressReportingIterator(Iterator<String> delegate, long totalBytes, PrintStream progressOut) {
        this(delegate, totalBytes, ProgressLine.enabled(progressOut));
    }

    ProgressReportingIterator(Iterator<String> delegate, long totalBytes, ProgressLine progress) {
        this.delegate = delegate;
        this.totalBytes = totalBytes;
        this.progress = progress;
    }

    @Override
    public boolean hasNext() {
        boolean more = delegate.hasNext();
        if (!more) {
            progress.finish();
        }
        return more;
    }

    @Override
    public String next() {
        if (!delegate.hasNext()) {
            throw new NoSuchElementException();
        }
        String line = delegate.next();
        consumedBytes += line.getBytes(StandardCharsets.UTF_8).length + 1;
        progress.show(percentComplete());
        return line;
    }

    private int percentComplete() {
        if (totalBytes <= 0) {
            return MAX_PERCENT;
        }
        return (int) Math.min(MAX_PERCENT, consumedBytes * MAX_PERCENT / totalBytes);
    }
}
