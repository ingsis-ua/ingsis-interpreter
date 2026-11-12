package org.printscript.formatter.rules;

import java.util.Optional;
import org.printscript.token.Token;

// Una regla mientras formatea un archivo. Cada método tiene una respuesta por defecto que
// es "no opino", así cada regla solo implementa lo que le toca.
public interface ActiveRule {

    // el espacio que exige entre previous y current, o vacío si no le corresponde
    default Optional<String> gap(Token previous, Token current, Layout layout) {
        return Optional.empty();
    }

    // la sangría, en espacios, que exige para un nivel de bloque, o vacío si no opina
    default Optional<Integer> indent(int level) {
        return Optional.empty();
    }

    // se llama después de escribir cada token; previous es null en el primero
    default void advancePast(Token previous, Token current) {}
}
