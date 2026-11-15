package org.printscript.linter.traversal;

import java.util.ArrayList;
import java.util.List;

// busca nodos de una clase recorriendo el árbol en preorden con los hijos que da el
// registro; no conoce ningún nodo
public final class NodeSearch {

    private final NodeChildren children;

    public NodeSearch(NodeChildren children) {
        this.children = children;
    }

    // todos los que coinciden, también los que están adentro de otro que coincidió
    public <T> List<T> all(Object root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, type, true, found);
        return List.copyOf(found);
    }

    // solo los de más afuera: adentro de uno que coincidió no se sigue bajando
    public <T> List<T> outermost(Object root, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(root, type, false, found);
        return List.copyOf(found);
    }

    private <T> void collect(Object node, Class<T> type, boolean intoMatches, List<T> found) {
        boolean matches = type.isInstance(node);
        if (matches) {
            found.add(type.cast(node));
        }
        if (matches && !intoMatches) {
            return;
        }
        for (Object child : children.of(node)) {
            collect(child, type, intoMatches, found);
        }
    }
}
