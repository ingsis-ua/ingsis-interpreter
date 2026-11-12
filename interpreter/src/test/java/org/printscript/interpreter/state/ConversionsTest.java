package org.printscript.interpreter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Success;

class ConversionsTest {

    private static final Range AT = new Range(new Position(1, 1), new Position(1, 9));

    @ParameterizedTest
    @CsvSource({"7, 7.0", "3.5, 3.5", "-2, -2.0", "0.25, 0.25", "-0.5, -0.5"})
    void numbersWrittenLikeInPrintScriptAreAccepted(String text, double expected) {
        assertEquals(new Success<>(expected), Conversions.toDeclaredType("number", text, AT));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {" 7 ", "7 ", "1e3", "NaN", "Infinity", "5d", "0x1p3", "+3", ".5", "5.", ""})
    void anythingElseIsRejected(String text) {
        assertEquals(
                new Failure<>("'" + text + "' no es un number", AT),
                Conversions.toDeclaredType("number", text, AT));
    }
}
