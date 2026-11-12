package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isOperator;
import static org.printscript.formatter.rules.TokenShapes.isPunctuation;
import static org.printscript.formatter.rules.TokenShapes.sameLine;

import java.util.Optional;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// El menos unario se queda pegado a su operando. No es una regla del archivo: envuelve a
// las reglas que separan con espacios y opina antes que ellas. Cada regla tiene su propia
// copia, así que no comparten estado.
final class UnaryMinus implements ActiveRule {

    private final ActiveRule spacing;
    private boolean pending;

    private UnaryMinus(ActiveRule spacing) {
        this.spacing = spacing;
    }

    static ActiveRule before(ActiveRule spacing) {
        return new UnaryMinus(spacing);
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        if (pending && sameLine(previous, current)) {
            return Optional.of("");
        }
        return spacing.gap(previous, current, layout);
    }

    @Override
    public void advancePast(Token previous, Token current) {
        pending = isOperator(current, "-") && opensAnOperand(previous);
        spacing.advancePast(previous, current);
    }

    private static boolean opensAnOperand(Token previous) {
        return previous == null
                || previous.type() == TokenType.OPERATOR
                || isPunctuation(previous, "(");
    }
}
