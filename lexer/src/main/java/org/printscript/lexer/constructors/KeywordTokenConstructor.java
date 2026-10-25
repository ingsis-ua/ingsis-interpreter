package org.printscript.lexer.constructors;

import java.util.Map;
import java.util.Optional;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// reconoce palabras y decide su categoría según la tabla que le pasan
public final class KeywordTokenConstructor extends OffsetTokenConstructor {

    private final Map<String, TokenType> keywords;

    public KeywordTokenConstructor(Map<String, TokenType> keywords) {
        this.keywords = Map.copyOf(keywords);
    }

    @Override
    public Optional<Token> tryBuild(String line, int from, Position start) {
        if (from >= line.length() || !startsWord(line.charAt(from))) {
            return Optional.empty();
        }
        int length = 1;
        while (from + length < line.length() && continuesWord(line.charAt(from + length))) {
            length = length + 1;
        }
        String word = line.substring(from, from + length);
        TokenType type = keywords.getOrDefault(word, TokenType.IDENTIFIER);
        Position end = new Position(start.line(), start.column() + length - 1);
        return Optional.of(new Token(type, word, new Range(start, end)));
    }

    private static boolean startsWord(char character) {
        return Character.isLetter(character) || character == '_';
    }

    private static boolean continuesWord(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }
}
