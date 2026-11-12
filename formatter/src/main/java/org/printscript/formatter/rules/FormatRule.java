package org.printscript.formatter.rules;

// Una regla de formato con el valor que leyó del archivo de reglas.
//
// Es inmutable y se compara por valor, así dos archivos que piden lo mismo dan la misma
// configuración. Lo que la regla recuerda mientras formatea vive en la ActiveRule que crea.
// no se anota como funcional: una regla se compara por valor, y una lambda no, así que las
// reglas son records y no lambdas
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface FormatRule {

    // una instancia nueva por cada Formatter: el estado no pasa de un archivo al siguiente
    ActiveRule start();

    // se llama al armar la configuración, después de leer todas las claves
    default void validate() {}

    static void requireNotNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("El valor no puede ser negativo: " + value);
        }
    }
}
