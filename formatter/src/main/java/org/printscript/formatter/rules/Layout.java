package org.printscript.formatter.rules;

import org.printscript.token.Token;

// Lo que el formatter le ofrece a las reglas que cortan la línea: los saltos más la
// sangría que le toca a current según cuántos bloques hay abiertos.
@FunctionalInterface
public interface Layout {

    String lineBreaks(int howMany, Token current);
}
