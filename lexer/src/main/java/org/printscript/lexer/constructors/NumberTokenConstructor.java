package org.printscript.lexer.constructors;

import java.util.Optional;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// reconoce números, enteros y con coma decimal
public final class NumberTokenConstructor extends OffsetTokenConstructor {

    @Override
    public Optional<Token> tryBuild(String line, int from, Position start) {
        int last = digitsFrom(line, from);
        if (last == from) {
            return Optional.empty();
        }
        if (hasDecimalPartAt(line, last)) {
            last = digitsFrom(line, last + 1);
        }
        int length = last - from;
        String lexeme = line.substring(from, last);
        Position end = new Position(start.line(), start.column() + length - 1);
        return Optional.of(new Token(TokenType.NUMBER, lexeme, new Range(start, end)));
    }

    private static int digitsFrom(String rest, int index) {
        int current = index;
        while (current < rest.length() && Character.isDigit(rest.charAt(current))) {
            current = current + 1;
        }
        return current;
    }

    private static boolean hasDecimalPartAt(String rest, int index) {
        boolean decimalPoint = index < rest.length() && rest.charAt(index) == '.';
        boolean followedByDigit =
                index + 1 < rest.length() && Character.isDigit(rest.charAt(index + 1));
        return decimalPoint && followedByDigit;
    }
}
