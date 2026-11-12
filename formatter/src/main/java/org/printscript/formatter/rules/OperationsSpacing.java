package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isOperation;

import java.util.Optional;
import org.printscript.token.Token;

// mandatory-space-surrounding-operations: un espacio a cada lado de + - * /
public record OperationsSpacing() implements FormatRule {

    @Override
    public ActiveRule start() {
        return new Active();
    }

    private static final class Active implements ActiveRule {

        private final UnaryMinus unaryMinus = new UnaryMinus();

        @Override
        public Optional<String> gap(Token previous, Token current, Layout layout) {
            return unaryMinus
                    .gap(previous, current)
                    .or(
                            () ->
                                    isOperation(previous) || isOperation(current)
                                            ? Optional.of(" ")
                                            : Optional.empty());
        }

        @Override
        public void advancePast(Token previous, Token current) {
            unaryMinus.advancePast(previous, current);
        }
    }
}
