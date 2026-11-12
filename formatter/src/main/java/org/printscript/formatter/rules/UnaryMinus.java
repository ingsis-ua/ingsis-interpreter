package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isOperator;
import static org.printscript.formatter.rules.TokenShapes.isPunctuation;
import static org.printscript.formatter.rules.TokenShapes.sameLine;

import java.util.Optional;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// El menos unario se queda pegado a su operando. No es una regla del archivo: lo aplican
// las reglas que separan con espacios, antes de separar, cada una con su propia copia.
final class UnaryMinus {

    private boolean pending;

    Optional<String> gap(Token previous, Token current) {
        return pending && sameLine(previous, current) ? Optional.of("") : Optional.empty();
    }

    void advancePast(Token previous, Token current) {
        pending = isOperator(current, "-") && opensAnOperand(previous);
    }

    private static boolean opensAnOperand(Token previous) {
        return previous == null
                || previous.type() == TokenType.OPERATOR
                || isPunctuation(previous, "(");
    }
}
