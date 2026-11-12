package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;
import static org.printscript.formatter.rules.TokenShapes.sameLine;

import java.util.Optional;
import org.printscript.token.Token;

// mandatory-single-space-separation: todo separado por un espacio, salvo el ';', que se
// queda contra lo que cierra
public record SingleSpace() implements FormatRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag("mandatory-single-space-separation", 80, new SingleSpace());

    @Override
    public ActiveRule start() {
        return new Active();
    }

    private static final class Active implements ActiveRule {

        private final UnaryMinus unaryMinus = new UnaryMinus();

        @Override
        public Optional<String> gap(Token previous, Token current, Layout layout) {
            return unaryMinus.gap(previous, current).or(() -> separation(previous, current));
        }

        @Override
        public void advancePast(Token previous, Token current) {
            unaryMinus.advancePast(previous, current);
        }

        private static Optional<String> separation(Token previous, Token current) {
            if (!sameLine(previous, current)) {
                return Optional.empty();
            }
            return Optional.of(isPunctuation(current, ";") ? "" : " ");
        }
    }
}
