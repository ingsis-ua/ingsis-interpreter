package org.printscript.parser;

import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.token.Token;

public final class LaterStatementParser implements StatementParser {

    private final LaterConstructs constructs;

    public LaterStatementParser(LaterConstructs constructs) {
        this.constructs = constructs;
    }

    @Override
    public boolean matches(TokenBuffer tokens) {
        return tokens.lookahead(0)
                .filter(token -> constructs.statements().contains(token.value()))
                .isPresent();
    }

    @Override
    public Result<Statement> parse(TokenBuffer tokens) {
        Token first = tokens.lookahead(0).orElseThrow();
        Token last = first;
        while (tokens.lookahead(0).isPresent()) {
            last = tokens.lookahead(0).orElseThrow();
            tokens.commit(1);
        }
        return new Failure<>(
                constructs.messageFor(first.value()),
                new Range(first.range().start(), last.range().end()));
    }
}
