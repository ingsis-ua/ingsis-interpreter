package org.printscript.semantic;

import java.util.EnumSet;
import org.printscript.ast.Expression;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;

// el tipo de una expresión suelta, con una tabla dada: para probar las reglas de
// expresiones sin armar una sentencia alrededor
final class ExpressionTyper {

    private ExpressionTyper() {}

    static Result<Type> typeOf(Expression expression, SymbolTable symbols) {
        return new Checking(
                        SemanticFactory.rules(), EnumSet.allOf(Type.class), "esta versión", symbols)
                .typeOf(expression);
    }
}
