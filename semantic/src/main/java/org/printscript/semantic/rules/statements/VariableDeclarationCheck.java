package org.printscript.semantic.rules.statements;

import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// let x: tipo = valor; el valor es opcional y la variable se puede reasignar
public final class VariableDeclarationCheck implements StatementCheck<VariableDeclaration> {

    @Override
    public Result<Void> check(VariableDeclaration declaration, Checking checking) {
        return Declarations.declare(
                checking,
                declaration.name().name(),
                declaration.type(),
                declaration.initializer(),
                declaration.range(),
                false);
    }
}
