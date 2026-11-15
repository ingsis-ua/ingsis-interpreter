package org.printscript.semantic.rules.statements;

import org.printscript.ast.Block;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// un Block es exactamente un scope: se abre un frame al entrar y se cierra al salir,
// también cuando una sentencia de adentro falló. Es la misma regla para el then, para
// el else y para el bloque de un else if, porque los tres son Block.
public final class BlockCheck implements StatementCheck<Block> {

    @Override
    public Result<Void> check(Block block, Checking checking) {
        checking.symbols(checking.symbols().openScope());
        int before = checking.nestedCount();
        try {
            for (Statement statement : block.statements()) {
                record(statement, checking);
            }
            return checking.nestedCount() == before
                    ? Declarations.success()
                    : checking.nestedAt(before);
        } finally {
            checking.symbols(checking.symbols().closeScope());
        }
    }

    // cada falla se anota una sola vez: si la sentencia ya anotó las suyas (un bloque
    // adentro de otro), no se vuelve a anotar
    private static void record(Statement statement, Checking checking) {
        int before = checking.nestedCount();
        Result<Void> checked = checking.check(statement);
        if (checked instanceof Failure<Void> failure && checking.nestedCount() == before) {
            checking.recordNested(failure);
        }
    }
}
