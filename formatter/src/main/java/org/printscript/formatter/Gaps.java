package org.printscript.formatter;

import static org.printscript.formatter.rules.TokenShapes.isPunctuation;

import java.util.List;
import java.util.Optional;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.formatter.rules.ActiveRule;
import org.printscript.formatter.rules.FormatRule;
import org.printscript.formatter.rules.Layout;
import org.printscript.token.Token;

// Decide qué va entre dos tokens.
//
// No conoce ninguna regla: les pregunta en orden y gana la primera que opine. Si ninguna
// opina, el espacio queda como estaba en el fuente. Agregar una regla no cambia esta clase.
final class Gaps implements Layout {

    private final List<ActiveRule> rules;

    // cuántas llaves hay abiertas: es la forma del fuente, no una regla, y la necesita la
    // sangría de cualquier salto de línea
    private int depth;

    Gaps(FormatterConfiguration configuration) {
        this.rules = configuration.rules().stream().map(FormatRule::start).toList();
    }

    String between(Token previous, Token current) {
        for (ActiveRule rule : rules) {
            Optional<String> gap = rule.gap(previous, current, this);
            if (gap.isPresent()) {
                return gap.get();
            }
        }
        return original(previous, current);
    }

    // el estado se actualiza después de decidir el espacio, porque la decisión mira la
    // sentencia que termina, no la que empieza
    void advancePast(Token previous, Token current) {
        for (ActiveRule rule : rules) {
            rule.advancePast(previous, current);
        }
        if (isPunctuation(current, "{")) {
            depth = depth + 1;
        } else if (isPunctuation(current, "}")) {
            depth = depth - 1;
        }
    }

    @Override
    public String lineBreaks(int howMany, Token current) {
        return "\n".repeat(howMany) + indentBefore(current, 0);
    }

    private String original(Token previous, Token current) {
        int breaks = current.range().start().line() - previous.range().end().line();
        if (breaks == 0) {
            return spaces(current.range().start().column() - previous.range().end().column() - 1);
        }
        // el salto ya estaba: la columna del token es su sangría real en el archivo
        return "\n".repeat(breaks) + indentBefore(current, current.range().start().column() - 1);
    }

    // la sangría que pida una regla manda sobre la que traía el archivo; el '}' cierra su
    // nivel, así que se alinea con la línea del if y no con el cuerpo
    private String indentBefore(Token current, int asItWas) {
        int level = isPunctuation(current, "}") ? depth - 1 : depth;
        for (ActiveRule rule : rules) {
            Optional<Integer> indent = rule.indent(level);
            if (indent.isPresent()) {
                return spaces(indent.get());
            }
        }
        return spaces(asItWas);
    }

    private static String spaces(int howMany) {
        return " ".repeat(Math.max(0, howMany));
    }
}
