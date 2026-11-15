package org.printscript.linter.traversal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;

// lo que antes avisaba el visitor al compilar: un nodo nuevo del AST que nadie registró.
// Recorre los nodos que declaran Statement y Expression, así que no cambia cuando se
// agrega uno
class NodeChildrenCompletenessTest {

    static Stream<Class<?>> nodes() {
        return Stream.concat(
                Stream.of(Statement.class.getPermittedSubclasses()),
                Stream.of(Expression.class.getPermittedSubclasses()));
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void everyNodeHasItsChildrenRegistered(Class<?> node) {
        assertTrue(
                NodeChildren.standard().knows(node),
                "Falta registrar los hijos de " + node.getSimpleName() + " en NodeChildren");
    }
}
