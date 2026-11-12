package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.Optional;
import org.printscript.token.Token;

// enforce-spacing-before-colon-in-declaration
public record SpaceBeforeColon() implements FormatRule, ActiveRule {

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        return isPunctuation(current, ":") ? Optional.of(" ") : Optional.empty();
    }
}
