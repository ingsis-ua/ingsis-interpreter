package org.printscript.semantic.rules.statements;

import org.printscript.ast.ConstDeclaration;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// const x: tipo = valor; el valor es obligatorio y no se puede reasignar
public final class ConstDeclarationCheck implements StatementCheck<ConstDeclaration> {

    @Override
    public Result<Void> check(ConstDeclaration declaration, Checking checking) {
        if (declaration.initializer().isEmpty()) {
            // una constante sin valor no se puede completar después: nace inservible
            return new Failure<>(
                    "Una constante tiene que declararse con un valor", declaration.range());
        }
        return Declarations.declare(
                checking,
                declaration.name().name(),
                declaration.type(),
                declaration.initializer(),
                declaration.range(),
                true);
    }
}
