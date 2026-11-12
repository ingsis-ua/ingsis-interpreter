package org.printscript.interpreter.engine;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.common.Result;

// sabe evaluar un tipo de expresión. Agregar una expresión al intérprete es escribir
// una de estas y registrarla en InterpreterFactory: el ExpressionEvaluator no cambia
public interface ExpressionRule<E extends Expression> {

    Result<Object> evaluate(E expression, Execution execution);

    // los nodos de adentro, en orden
    List<Object> children(E expression);
}
