package org.printscript.interpreter;

import java.util.List;
import org.printscript.ast.Statement;
import org.printscript.common.Result;

// sabe ejecutar un tipo de sentencia. Agregar una sentencia al intérprete es escribir
// una de estas y registrarla en InterpreterFactory: el Executor no cambia
public interface StatementRule<S extends Statement> {

    Result<Void> execute(S statement, Execution execution);

    // los nodos de adentro, en orden, para que el chequeo de nodos soportados los
    // recorra sin conocer la forma de cada sentencia
    List<Object> children(S statement);
}
