package org.printscript.interpreter.rules.statements;

import java.util.List;
import org.printscript.ast.ConstDeclaration;
import org.printscript.common.Result;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.engine.StatementRule;

// const x: number = 12; queda inmutable
public final class ConstDeclarationRule implements StatementRule<ConstDeclaration> {

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
