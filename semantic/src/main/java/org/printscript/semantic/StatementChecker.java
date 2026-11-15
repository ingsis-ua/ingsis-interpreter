package org.printscript.semantic;

import java.util.List;
import java.util.Set;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.SemanticRules;

// las reglas semánticas, una por nodo, buscadas en el registro: no conoce ningún nodo
// el estado de error es por sentencia y no acumulado:
// una sentencia inválida no condena a las que siguen
public final class StatementChecker {

    private final Checking checking;

    public StatementChecker(Set<Type> supportedTypes) {
        this(supportedTypes, "esta versión");
    }

    public StatementChecker(Set<Type> supportedTypes, String language) {
        this(supportedTypes, language, SemanticFactory.rules());
    }

    public StatementChecker(Set<Type> supportedTypes, String language, SemanticRules rules) {
        this.checking = new Checking(rules, supportedTypes, language, new SymbolTable());
    }

    // todas las fallas de la sentencia: las de adentro de sus bloques si las hubo, o la de
    // la sentencia misma
    public List<Failure<Void>> failuresOf(Statement statement) {
        checking.forgetNested();
        Result<Void> checked = checking.check(statement);
        List<Failure<Void>> nested = checking.nested();
        if (!nested.isEmpty()) {
            return nested;
        }
        return checked instanceof Failure<Void> failure ? List.of(failure) : List.of();
    }

    // el resultado de una sola sentencia, sin juntar las fallas anidadas
    Result<Void> check(Statement statement) {
        return checking.check(statement);
    }
}
