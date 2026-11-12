package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isOperation;

import java.util.Optional;
import org.printscript.token.Token;

// mandatory-space-surrounding-operations: un espacio a cada lado de + - * /, salvo el menos
// unario, que se queda pegado
public record OperationsSpacing() implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag(
                    "mandatory-space-surrounding-operations", 70, new OperationsSpacing());

    @Override
    public ActiveRule start() {
        return UnaryMinus.before(this);
    }

    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        return isOperation(previous) || isOperation(current) ? Optional.of(" ") : Optional.empty();
    }
}
