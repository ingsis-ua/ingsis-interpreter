package org.printscript.semantic.rules.statements;

import java.util.Optional;
import org.printscript.ast.AssignmentStatement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.semantic.Symbol;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// x = valor; la variable tiene que existir, no ser constante y recibir su tipo
public final class AssignmentCheck implements StatementCheck<AssignmentStatement> {

    @Override
    public Result<Void> check(AssignmentStatement assignment, Checking checking) {
        String name = assignment.name().name();
        Optional<Symbol> declared = checking.symbols().read(name);
        if (declared.isEmpty()) {
            // en una asignación la sentencia entera es la que no tiene sentido; cuando
            // el nombre aparece dentro de una expresión, el rango es el del uso
            return new Failure<>("Variable no declarada: " + name, assignment.range());
        }
        if (declared.get().immutable()) {
            return new Failure<>("No se puede reasignar la constante " + name, assignment.range());
        }
        Result<Void> checked =
                Declarations.checkAssignable(
                        checking, assignment.expression(), declared.get().type());
        checking.symbols(checking.symbols().assign(name));
        return checked;
    }
}
