package org.printscript.formatter.rules;

import java.util.Optional;

// indent-inside-if: cuántos espacios por nivel de bloque. No decide espacios entre tokens:
// solo la sangría de las líneas que ya empiezan.
public record IfIndent(int size) implements FormatRule, ActiveRule {

    @Override
    public void validate() {
        FormatRule.requireNotNegative(size);
    }

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<Integer> indent(int level) {
        return Optional.of(size * level);
    }
}
