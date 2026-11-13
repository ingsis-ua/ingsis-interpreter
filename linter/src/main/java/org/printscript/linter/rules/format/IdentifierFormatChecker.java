package org.printscript.linter.rules.format;

import java.util.List;

// Un formato de identificador. Agregar uno es escribir esta clase y sumarla al registro de
// formatos (IdentifierFormats); la regla que los usa no cambia.
public interface IdentifierFormatChecker {

    boolean isValid(String name);

    String expectedFormat();

    // cómo se lo puede nombrar en el archivo de reglas, en minúscula; la primera es la que
    // se muestra cuando el usuario escribe un formato que no existe
    List<String> spellings();
}
