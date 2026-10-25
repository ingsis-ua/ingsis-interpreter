package org.printscript.lexer.constructors;

import java.util.Optional;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// reconoce textos entre comillas, simples o dobles
public final class StringTokenConstructor extends OffsetTokenConstructor {

    @Override
    public Optional<Token> tryBuild(String line, int from, Position start) {
        if (from >= line.length()) {
            return Optional.empty();
        }
        char quote = line.charAt(from);
        if (quote != '\'' && quote != '"') {
            return Optional.empty();
        }
        int closing = line.indexOf(quote, from + 1);
        if (closing < 0) {
            String rest = line.substring(from);
            return Optional.of(new Token(TokenType.UNKNOWN, rest, rangeOf(start, rest.length())));
        }
        String lexeme = line.substring(from, closing + 1);
        return Optional.of(new Token(TokenType.STRING, lexeme, rangeOf(start, lexeme.length())));
    }

    private static Range rangeOf(Position start, int length) {
        return new Range(start, new Position(start.line(), start.column() + length - 1));
    }
}
