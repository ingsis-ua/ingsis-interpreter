package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isOperator;

import java.util.Optional;
import org.printscript.token.Token;

// enforce-spacing-around-equals / enforce-no-spacing-around-equals
public record EqualsSpacing(boolean spaced) implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.either(
                    "enforce-spacing-around-equals",
                    "enforce-no-spacing-around-equals",
                    40,
                    EqualsSpacing::new);

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        if (!isOperator(previous, "=") && !isOperator(current, "=")) {
            return Optional.empty();
        }
        return Optional.of(spaced ? " " : "");
    }
}
