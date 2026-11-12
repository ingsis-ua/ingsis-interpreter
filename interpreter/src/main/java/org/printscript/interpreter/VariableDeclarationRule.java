package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Result;

// let x: number = 12;
final class VariableDeclarationRule implements StatementRule<VariableDeclaration> {

    @Override
    public Result<Void> execute(VariableDeclaration declaration, Execution execution) {
        return Declarations.declare(
                declaration.name().name(),
                declaration.type().name(),
                declaration.initializer(),
                declaration.range(),
                false,
                execution);
    }

    @Override
    public List<Object> children(VariableDeclaration declaration) {
        return Declarations.childrenOf(declaration.initializer());
    }
}
