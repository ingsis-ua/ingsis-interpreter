package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.Block;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

// { ... }: un bloque es exactamente un scope
final class BlockRule implements StatementRule<Block> {

    // el frame se abre al entrar al bloque y se cierra al salir, también cuando una
    // sentencia de adentro falló: es la misma regla y el mismo lugar que en semantic
    @Override
    public Result<Void> execute(Block block, Execution execution) {
        execution.update(execution.environment().openScope());
        try {
            for (Statement statement : block.statements()) {
                Result<Void> executed = execution.execute(statement);
                if (executed instanceof Failure<Void>) {
                    return executed;
                }
            }
            return new Success<>(null);
        } finally {
            execution.update(execution.environment().closeScope());
        }
    }

    @Override
    public List<Object> children(Block block) {
        return List.copyOf(block.statements());
    }
}
