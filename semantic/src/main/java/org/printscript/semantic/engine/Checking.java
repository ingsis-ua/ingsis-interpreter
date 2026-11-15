package org.printscript.semantic.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.semantic.SymbolTable;
import org.printscript.semantic.Type;

// el estado de un chequeo que reciben las reglas: la tabla de tipos, lo que soporta la
// versión, las fallas de las sentencias anidadas en bloques, y la forma de chequear o
// tipar un nodo hijo sin saber qué regla le toca
public final class Checking {

    private final SemanticRules rules;
    private final Set<Type> supported;
    private final String languageName;
    private final List<Failure<Void>> nestedFailures = new ArrayList<>();
    private SymbolTable table;

    public Checking(SemanticRules rules, Set<Type> supported, String language, SymbolTable table) {
        this.rules = rules;
        this.supported = Set.copyOf(supported);
        this.languageName = language;
        this.table = table;
    }

    public SymbolTable symbols() {
        return table;
    }

    // SymbolTable es inmutable: declarar, asignar o abrir un scope devuelve una nueva
    public void symbols(SymbolTable updated) {
        this.table = updated;
    }

    public Set<Type> supportedTypes() {
        return supported;
    }

    public String language() {
        return languageName;
    }

    public Result<Void> check(Statement statement) {
        return rules.checkOf(statement)
                .map(rule -> rule.check(statement, this))
                .orElseGet(() -> unknown(statement, statement.range()));
    }

    public Result<Type> typeOf(Expression expression) {
        return rules.typingOf(expression)
                .map(rule -> rule.type(expression, this))
                .orElseGet(() -> unknown(expression, expression.range()));
    }

    // las fallas de las sentencias de adentro de un bloque, en el orden en que aparecen
    public void recordNested(Failure<Void> failure) {
        nestedFailures.add(failure);
    }

    public int nestedCount() {
        return nestedFailures.size();
    }

    public Failure<Void> nestedAt(int index) {
        return nestedFailures.get(index);
    }

    public List<Failure<Void>> nested() {
        return List.copyOf(nestedFailures);
    }

    public void forgetNested() {
        nestedFailures.clear();
    }

    private static <T> Failure<T> unknown(Object node, Range range) {
        return new Failure<>(
                "Nodo " + node.getClass().getSimpleName() + " no soportado por PrintScript", range);
    }
}
