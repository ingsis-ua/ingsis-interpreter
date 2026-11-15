package org.printscript.semantic.rules.expressions;

import org.printscript.ast.Expression;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;

// las dos lecturas externas valen string: lo que entra de afuera es texto, y lo que
// reciben (el mensaje o el nombre de la variable) también tiene que serlo
final class StringArgument {

    private StringArgument() {}

    static Result<Type> of(Expression argument, String function, Checking checking) {
        return checking.typeOf(argument)
                .andThen(actual -> requireString(actual, argument, function));
    }

    private static Result<Type> requireString(Type actual, Expression argument, String function) {
        if (actual != Type.STRING) {
            return new Failure<>(
                    "El argumento de " + function + " tiene que ser string", argument.range());
        }
        return new Success<>(Type.STRING);
    }
}
