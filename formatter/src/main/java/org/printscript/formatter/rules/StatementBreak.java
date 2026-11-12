package org.printscript.formatter.rules;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;
import static org.printscript.formatter.rules.TokenShapes.sameLine;

import java.util.Optional;
import org.printscript.token.Token;

// mandatory-line-break-after-statement: una sentencia por línea
public record StatementBreak() implements FormatRule, ActiveRule {

    public static final RuleDefinition DEFINITION =
            RuleDefinition.flag("mandatory-line-break-after-statement", 20, new StatementBreak());

    @Override
    public ActiveRule start() {
        return this;
    }

    // solo si la sentencia siguiente arranca en la misma línea: si ya había un salto, la
    // regla está cumplida y no hay nada que imponer
    @Override
    public Optional<String> gap(Token previous, Token current, Layout layout) {
        return isPunctuation(previous, ";") && sameLine(previous, current)
                ? Optional.of(layout.lineBreaks(1, current))
                : Optional.empty();
    }
}
