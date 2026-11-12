package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// cómo se muestra un valor y cómo se nombra su tipo en los mensajes de error
class ValuesTest {

    @Test
    void numbersAreShownWithoutTrailingZerosOrExponent() {
        assertEquals("100000000000000000000", Values.asText(1e20));
        assertEquals("3", Values.asText(3.0));
        assertEquals("2.5", Values.asText(2.5));
        assertEquals("-0.5", Values.asText(-0.5));
    }

    @Test
    void stringsAndBooleansAreShownAsTheyAre() {
        assertEquals("hola", Values.asText("hola"));
        assertEquals("true", Values.asText(true));
    }

    @Test
    void typeNamesAreTheOnesWrittenInPrintScript() {
        assertEquals("string", Values.typeOf("x"));
        assertEquals("boolean", Values.typeOf(true));
        assertEquals("number", Values.typeOf(1.0));
    }
}
