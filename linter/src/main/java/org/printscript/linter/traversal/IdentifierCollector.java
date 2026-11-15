package org.printscript.linter.traversal;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.Statement;

// todos los identificadores de un nodo, incluidos los nombres que se declaran o asignan
public final class IdentifierCollector {

    private static final NodeSearch SEARCH = new NodeSearch(NodeChildren.standard());

    public List<Identifier> collect(Statement statement) {
        return SEARCH.all(statement, Identifier.class);
    }

    public List<Identifier> collect(Expression expression) {
        return SEARCH.all(expression, Identifier.class);
    }
}
