package org.printscript.linter.rules.format;

import java.util.List;

public record CamelCaseChecker() implements IdentifierFormatChecker {

    @Override
    public boolean isValid(String name) {
        return !name.isEmpty()
                && Character.isLowerCase(name.charAt(0))
                && name.chars().allMatch(Character::isLetterOrDigit);
    }

    @Override
    public String expectedFormat() {
        return "camel case";
    }

    @Override
    public List<String> spellings() {
        return List.of("camel case", "camelcase", "camel_case");
    }
}
