package org.printscript.interpreter.engine;

import java.util.Optional;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Range;

// revisa la sentencia entera antes de ejecutar nada: si algún nodo no tiene regla en
// esta versión, la sentencia se rechaza sin efectos. Recorre el árbol con los hijos que
// declara cada regla, así que no cambia cuando se agrega un nodo
public final class NodeGate {

    private final NodeRules rules;

    public NodeGate(NodeRules rules) {
        this.rules = rules;
    }

    public Optional<Failure<Void>> check(Statement statement) {
        return firstUnsupported(statement);
    }

    // preorden: el nodo mismo y después sus hijos en el orden que da su regla
    private Optional<Failure<Void>> firstUnsupported(Object node) {
        if (!rules.supports(node.getClass())) {
            return Optional.of(NodeRules.unsupported(node, rangeOf(node)));
        }
        for (Object child : rules.childrenOf(node)) {
            Optional<Failure<Void>> rejected = firstUnsupported(child);
            if (rejected.isPresent()) {
                return rejected;
            }
        }
        return Optional.empty();
    }

    private static Range rangeOf(Object node) {
        return switch (node) {
            case Statement statement -> statement.range();
            case Expression expression -> expression.range();
            default ->
                    throw new IllegalArgumentException(
                            "No es un nodo del árbol: " + node.getClass().getSimpleName());
        };
    }
}
