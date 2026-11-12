package org.printscript.interpreter.state;

import java.math.BigDecimal;

// cómo se muestra un valor del programa y cómo se nombra su tipo en los mensajes
public final class Values {

    private Values() {}

    public static String asText(Object value) {
        if (value instanceof Double number && Double.isFinite(number)) {
            return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    public static String typeOf(Object value) {
        if (value instanceof String) {
            return "string";
        }
        return value instanceof Boolean ? "boolean" : "number";
    }
}
