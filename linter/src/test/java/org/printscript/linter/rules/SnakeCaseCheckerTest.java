package org.printscript.linter.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.printscript.linter.rules.format.SnakeCaseChecker;

class SnakeCaseCheckerTest {

    private final SnakeCaseChecker checker = new SnakeCaseChecker();

    @Test
    void aSingleLowercaseWordIsSnakeCase() {
        assertTrue(checker.isValid("nombre"));
    }

    @Test
    void severalWordsJoinedWithUnderscoresAreSnakeCase() {
        assertTrue(checker.isValid("nombre_del_usuario"));
    }

    @Test
    void digitsAfterTheFirstLetterAreAllowed() {
        assertTrue(checker.isValid("linea_2"));
    }

    @Test
    void aCapitalLetterIsNotSnakeCase() {
        assertFalse(checker.isValid("nombreDelUsuario"));
    }

    @Test
    void startingWithACapitalIsNotSnakeCase() {
        assertFalse(checker.isValid("Nombre"));
    }

    @Test
    void startingWithAnUnderscoreIsNotSnakeCase() {
        assertFalse(checker.isValid("_nombre"));
    }

    @Test
    void startingWithADigitIsNotSnakeCase() {
        assertFalse(checker.isValid("2linea"));
    }

    @Test
    void anEmptyNameIsNotSnakeCase() {
        assertFalse(checker.isValid(""));
    }

    @Test
    void theCheckerNamesTheFormatItExpects() {
        assertTrue(checker.expectedFormat().contains("snake"), checker.expectedFormat());
    }
}
