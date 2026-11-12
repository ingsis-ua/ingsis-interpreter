package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.ConstDeclaration;
import org.printscript.common.Result;

// const x: number = 12; queda inmutable
final class ConstDeclarationRule implements StatementRule<ConstDeclaration> {

    @Override
    public Result<Void> execute(ConstDeclaration declaration, Execution execution) {
        return Declarations.declare(
                declaration.name().name(),
                declaration.type().name(),
                declaration.initializer(),
                declaration.range(),
                true,
                execution);
    }

    @Override
    public List<Object> children(ConstDeclaration declaration) {
        return Declarations.childrenOf(declaration.initializer());
    }
}
