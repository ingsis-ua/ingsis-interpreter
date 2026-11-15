package org.printscript.semantic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;

// lo que antes avisaba el visitor al compilar: un nodo nuevo del AST sin regla semántica.
// Recorre los nodos que declaran Statement y Expression, así que no cambia cuando se
// agrega uno
class SemanticRulesCompletenessTest {

    static Stream<Class<?>> nodes() {
        return Stream.concat(
                Stream.of(Statement.class.getPermittedSubclasses()),
                Stream.of(Expression.class.getPermittedSubclasses()));
    }

    @ParameterizedTest
    @MethodSource("nodes")
    void everyNodeHasItsRule(Class<?> node) {
        assertTrue(
                SemanticFactory.rules().covers(node),
                "Falta registrar la regla semántica de " + node.getSimpleName());
    }
}
