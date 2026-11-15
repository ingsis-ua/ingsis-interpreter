package org.printscript.linter.traversal;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.Statement;

// todos los println de un nodo, incluidos los que están adentro de un bloque
public final class PrintlnCollector {

    private static final NodeSearch SEARCH = new NodeSearch(NodeChildren.standard());

    public List<PrintlnStatement> collect(Statement statement) {
        return SEARCH.outermost(statement, PrintlnStatement.class);
    }

    public List<PrintlnStatement> collect(Expression expression) {
        return SEARCH.outermost(expression, PrintlnStatement.class);
    }
}
