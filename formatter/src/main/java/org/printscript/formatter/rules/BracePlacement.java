package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.Optional;
import org.printscript.token.Token;

// if-brace-same-line / if-brace-below-line: dónde va la llave que abre el bloque
public record BracePlacement(boolean sameLine) implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.either(
                    "if-brace-same-line", "if-brace-below-line", 30, BracePlacement::new);

    @Override
    public ActiveRule start() {
        return this;
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        if (!isPunctuation(current, "{")) {
            return Optional.empty();
        }
        return Optional.of(sameLine ? " " : layout.lineBreaks(1, current));
    }
}
