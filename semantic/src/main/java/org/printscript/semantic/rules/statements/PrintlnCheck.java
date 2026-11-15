package org.printscript.semantic.rules.statements;

import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// println acepta cualquier tipo básico: solo hace falta que la expresión tipe
public final class PrintlnCheck implements StatementCheck<PrintlnStatement> {

    @Override
    public Result<Void> check(PrintlnStatement println, Checking checking) {
        return checking.typeOf(println.expression()).map(type -> null);
    }
}
