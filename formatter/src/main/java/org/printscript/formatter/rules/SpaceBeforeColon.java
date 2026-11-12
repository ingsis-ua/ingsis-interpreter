package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.Optional;
import org.printscript.token.Token;

// enforce-spacing-before-colon-in-declaration
public record SpaceBeforeColon() implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag(
                    "enforce-spacing-before-colon-in-declaration", 50, new SpaceBeforeColon());

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        return isPunctuation(current, ":") ? Optional.of(" ") : Optional.empty();
    }
}
