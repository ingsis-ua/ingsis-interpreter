package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isKeyword;
import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.Optional;
import org.printscript.token.Token;

// line-breaks-after-println: cuántas líneas en blanco van después de un println
public record PrintlnBreaks(int howMany) implements FormatRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.count("line-breaks-after-println", 10, PrintlnBreaks::new);

    @Override
    public void validate() {
        FormatRule.requireNotNegative(howMany);
    }

    @Override
    public ActiveRule start() {
        return new Active();
    }

    private final class Active implements ActiveRule {

        private boolean insidePrintln;

        @Override
        public Optional<String> gap(Token previous, Token current, Layout layout) {
            if (!insidePrintln || !isPunctuation(previous, ";")) {
                return Optional.empty();
            }
            return Optional.of(layout.lineBreaks(howMany + 1, current));
        }

        // el estado se actualiza después de decidir el espacio, porque la decisión mira la
        // sentencia que termina, no la que empieza
        @Override
        public void advancePast(Token previous, Token current) {
            if (previous != null && isPunctuation(previous, ";")) {
                insidePrintln = false;
            }
            if (isKeyword(current, "println")) {
                insidePrintln = true;
            }
        }
    }
}
