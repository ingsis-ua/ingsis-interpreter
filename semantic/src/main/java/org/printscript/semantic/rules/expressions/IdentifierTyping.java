package org.printscript.semantic.rules.expressions;

import java.util.Optional;
import org.printscript.ast.Identifier;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Symbol;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.ExpressionTyping;

// una variable vale el tipo con el que se declaró, si existe y ya tiene valor
public final class IdentifierTyping implements ExpressionTyping<Identifier> {

    @Override
    public Result<Type> type(Identifier identifier, Checking checking) {
        String name = identifier.name();
        Optional<Symbol> symbol = checking.symbols().read(name);
        if (symbol.isEmpty()) {
            return new Failure<>("Variable no declarada: " + name, identifier.range());
        }
        if (!checking.symbols().isInitialized(name)) {
            return new Failure<>("Variable usada antes de asignarse: " + name, identifier.range());
        }
        return new Success<>(symbol.get().type());
    }
}
