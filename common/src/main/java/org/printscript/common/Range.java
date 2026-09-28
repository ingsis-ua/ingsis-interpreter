package org.printscript.common;

import java.util.Objects;

// desde que posición hasta que posición está algo en el archivo
public record Range(Position start, Position end) {

    public Range {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (end.line() < start.line()
                || end.line() == start.line() && end.column() < start.column()) {
            throw new IllegalArgumentException("Range end must not precede start");
        }
    }
}
