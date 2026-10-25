package org.printscript.formatter;

import java.util.Iterator;
import java.util.Optional;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// Reescribe el fuente token por token, entregando de a un pedazo.
// No arma el árbol y no conoce la gramática: lo que hace es saber qué
// tokens hay y cuánto espacio va entre ellos.
public final class Formatter implements Iterator<Result<String>> {

    private final Iterator<Token> tokens;
    private final Gaps gaps;

    private Token previous;
    private Optional<Failure<String>> pendingFailure = Optional.empty();

    public Formatter(Iterator<Token> tokens, FormatterConfiguration configuration) {
        this.tokens = tokens;
        this.gaps = new Gaps(configuration);
    }

    @Override
    public boolean hasNext() {
        return pendingFailure.isPresent() || tokens.hasNext();
    }

    @Override
    public Result<String> next() {
        if (pendingFailure.isPresent()) {
            Failure<String> failure = pendingFailure.get();
            pendingFailure = Optional.empty();
            return failure;
        }
        Token token = tokens.next();
        if (token.type() == TokenType.UNKNOWN) {
            pendingFailure =
                    Optional.of(
                            new Failure<>(
                                    "Símbolo no reconocido: '" + token.value() + "'",
                                    token.range()));
        }
        String piece = space(token) + token.value();
        gaps.advancePast(previous, token);
        previous = token;
        return new Success<>(piece);
    }

    public String ending(int sourceLines) {
        int lastLine = previous == null ? 1 : previous.range().end().line();
        return "\n".repeat(Math.max(0, sourceLines - lastLine));
    }

    private String space(Token token) {
        return previous == null ? leading(token) : gaps.between(previous, token);
    }

    private static String leading(Token token) {
        Position start = token.range().start();
        return "\n".repeat(start.line() - 1) + " ".repeat(start.column() - 1);
    }
}
