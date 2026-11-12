package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;
import static org.printscript.formatter.rules.TokenShapes.sameLine;

import java.util.Optional;
import org.printscript.token.Token;

// mandatory-single-space-separation: todo separado por un espacio, salvo el ';', que se
// queda contra lo que cierra, y el menos unario, que se queda pegado
public record SingleSpace() implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag("mandatory-single-space-separation", 80, new SingleSpace());

    @Override
    public ActiveRule start() {
        return UnaryMinus.before(this);
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        if (!sameLine(previous, current)) {
            return Optional.empty();
        }
        return Optional.of(isPunctuation(current, ";") ? "" : " ");
    }
}
