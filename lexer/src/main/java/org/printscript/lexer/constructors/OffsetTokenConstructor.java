package org.printscript.lexer.constructors;

import java.util.Optional;
import org.printscript.common.Position;
import org.printscript.token.Token;

public abstract class OffsetTokenConstructor implements TokenConstructor {

    @Override
    public final Optional<Token> tryBuild(String rest, Position start) {
        return tryBuild(rest, 0, start);
    }

    @Override
    public abstract Optional<Token> tryBuild(String line, int from, Position start);
}
