package org.printscript.linter.rules.format;

public final class SnakeCaseChecker implements IdentifierFormatChecker {

    @Override
    public boolean isValid(String name) {
        return !name.isEmpty()
                && Character.isLowerCase(name.charAt(0))
                && name.chars()
                        .allMatch(
                                character ->
                                        character == '_'
                                                || (Character.isLetterOrDigit(character)
                                                        && !Character.isUpperCase(character)));
    }

    @Override
    public String expectedFormat() {
        return "snake case";
    }
}
