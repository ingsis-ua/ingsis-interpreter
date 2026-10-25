package org.printscript.cli;

import java.io.IOException;
import java.io.Writer;

final class ProgressClearingWriter extends Writer {

    private final Writer delegate;
    private final ProgressLine progress;

    ProgressClearingWriter(Writer delegate, ProgressLine progress) {
        this.delegate = delegate;
        this.progress = progress;
    }

    @Override
    public void write(char[] buffer, int offset, int length) throws IOException {
        progress.clear();
        delegate.write(buffer, offset, length);
        delegate.flush();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
