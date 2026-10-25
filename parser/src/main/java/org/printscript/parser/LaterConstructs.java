package org.printscript.parser;

import java.util.Set;

public record LaterConstructs(Set<String> statements, Set<String> calls, String language) {

    public LaterConstructs {
        statements = Set.copyOf(statements);
        calls = Set.copyOf(calls);
    }

    public static LaterConstructs none() {
        return new LaterConstructs(Set.of(), Set.of(), "");
    }

    String messageFor(String word) {
        return "'" + word + "' no está soportado en " + language;
    }
}
