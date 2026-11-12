package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.Optional;
import org.printscript.token.Token;

// enforce-spacing-after-colon-in-declaration
public record SpaceAfterColon() implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag(
                    "enforce-spacing-after-colon-in-declaration", 60, new SpaceAfterColon());

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        return isPunctuation(previous, ":") ? Optional.of(" ") : Optional.empty();
    }
}
