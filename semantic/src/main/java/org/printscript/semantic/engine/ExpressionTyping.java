package org.printscript.semantic.engine;

import org.printscript.ast.Expression;
import org.printscript.common.Result;
import org.printscript.semantic.Type;

// sabe qué tipo tiene un tipo de expresión, sin evaluarla. Agregar una expresión a
// semantic es escribir una de estas y registrarla en SemanticFactory
@FunctionalInterface
public interface ExpressionTyping<E extends Expression> {

    Result<Type> type(E expression, Checking checking);
}
