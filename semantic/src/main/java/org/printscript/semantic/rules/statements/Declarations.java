package org.printscript.semantic.rules.statements;

import java.util.EnumSet;
import java.util.Optional;
import org.printscript.ast.Expression;
import org.printscript.ast.ReadEnv;
import org.printscript.ast.ReadInput;
import org.printscript.ast.TypeName;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.semantic.Symbol;
import org.printscript.semantic.SymbolTable;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;

// lo que comparten las reglas que guardan un valor en una variable: declarar (let y
// const) y revisar que lo asignado tenga el tipo de la variable (declarar y asignar)
final class Declarations {

    private Declarations() {}

    static Result<Void> declare(
            Checking checking,
            String name,
            TypeName annotation,
            Optional<Expression> initializer,
            Range statement,
            boolean immutable) {
        Optional<Type> declared =
                Type.fromWrittenName(annotation.name(), checking.supportedTypes());
        if (declared.isEmpty()) {
            return new Failure<>(
                    unknownType(annotation.name(), checking.language()), annotation.range());
        }

        // la tabla decide si ya estaba declarada, y solo mira el scope actual: el mismo
        // nombre en un bloque más interno es sombra, no redeclaración
        Optional<SymbolTable> updated =
                checking.symbols()
                        .declare(
                                name,
                                new Symbol(declared.get(), immutable),
                                initializer.isPresent());
        if (updated.isEmpty()) {
            // el rango es el de la declaración entera, como venía reportándolo el
            // intérprete: lo que está de más es la sentencia, no el nombre
            return new Failure<>("Variable ya declarada: " + name, statement);
        }
        // se declara aunque el inicializador esté mal, así un error no arrastra a las
        // sentencias que usan la variable después
        checking.symbols(updated.get());

        if (initializer.isEmpty()) {
            return success();
        }
        return checkAssignable(checking, initializer.get(), declared.get());
    }

    private static String unknownType(String written, String language) {
        boolean existsLater = Type.fromWrittenName(written, EnumSet.allOf(Type.class)).isPresent();
        return existsLater
                ? "El tipo " + written + " no está soportado en " + language
                : "Tipo desconocido: " + written;
    }

    static Result<Void> checkAssignable(Checking checking, Expression expression, Type declared) {
        return checking.typeOf(expression)
                .andThen(actual -> compareTypes(expression, declared, actual));
    }

    private static Result<Void> compareTypes(Expression expression, Type declared, Type actual) {
        // excepción acotada: lo que entra de afuera es texto, y el tipo declarado manda
        // la conversión. Vale solo cuando el inicializador es exactamente una lectura;
        // adentro de una expresión es un string común, o si no deja de ser checkeable.
        if (isExternalRead(expression)) {
            return success();
        }
        if (actual != declared) {
            return new Failure<>(
                    "No se puede asignar "
                            + actual.writtenName()
                            + " a una variable "
                            + declared.writtenName(),
                    expression.range());
        }
        return success();
    }

    private static boolean isExternalRead(Expression expression) {
        return expression instanceof ReadInput || expression instanceof ReadEnv;
    }

    static Result<Void> success() {
        return new Success<>(null);
    }
}
