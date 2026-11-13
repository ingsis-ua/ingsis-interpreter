package org.printscript.linter.rules.format;

import java.util.List;

// snake case: arranca en minúscula y solo admite minúsculas, dígitos y guión bajo
public record SnakeCaseChecker() implements IdentifierFormatChecker {

    @Override
    public boolean isValid(String name) {
        return !name.isEmpty()
                && Character.isLowerCase(name.charAt(0))
                && name.chars().allMatch(SnakeCaseChecker::isSnakeCharacter);
    }

    private static boolean isSnakeCharacter(int character) {
        return character == '_'
                || (Character.isLetterOrDigit(character) && !Character.isUpperCase(character));
    }

    @Override
    public String expectedFormat() {
        return "snake case";
    }

    @Override
    public List<String> spellings() {
        return List.of("snake case", "snakecase", "snake_case");
    }
}
