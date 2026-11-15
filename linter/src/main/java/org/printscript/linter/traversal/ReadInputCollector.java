package org.printscript.linter.traversal;

import java.util.List;
import org.printscript.ast.Expression;
import org.printscript.ast.ReadInput;
import org.printscript.ast.Statement;

// todos los readInput de un nodo; adentro de uno que ya encontró no sigue bajando
public final class ReadInputCollector {

    private static final NodeSearch SEARCH = new NodeSearch(NodeChildren.standard());

    public List<ReadInput> collect(Statement statement) {
        return SEARCH.outermost(statement, ReadInput.class);
    }

    public List<ReadInput> collect(Expression expression) {
        return SEARCH.outermost(expression, ReadInput.class);
    }
}
