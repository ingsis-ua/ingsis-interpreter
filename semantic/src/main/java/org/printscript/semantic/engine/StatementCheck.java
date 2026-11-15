package org.printscript.semantic.engine;

import org.printscript.ast.Statement;
import org.printscript.common.Result;

// sabe si un tipo de sentencia tiene sentido. Agregar una sentencia a semantic es
// escribir una de estas y registrarla en SemanticFactory: StatementChecker no cambia
@FunctionalInterface
public interface StatementCheck<S extends Statement> {

    Result<Void> check(S statement, Checking checking);
}
