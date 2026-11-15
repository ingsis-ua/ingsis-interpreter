package org.printscript.semantic.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.semantic.StatementChecker;
import org.printscript.semantic.Type;

// agregar un nodo a semantic es escribir su regla y registrarla: StatementChecker no se
// toca y la usa igual
class ExtensionTest {

    private static final Range RANGE = new Range(new Position(1, 1), new Position(1, 4));
    private static final PrintlnStatement PRINTLN =
            new PrintlnStatement(new NumberLiteral(1, RANGE), RANGE);

    private static List<Failure<Void>> failuresWith(SemanticRules rules) {
        return new StatementChecker(Set.of(Type.NUMBER), "PrintScript de prueba", rules)
                .failuresOf(PRINTLN);
    }

    @Test
    void aNewRuleIsUsedWithoutChangingTheChecker() {
        SemanticRules rules =
                SemanticRules.registry()
                        .check(
                                PrintlnStatement.class,
                                (println, checking) -> new Failure<>("regla nueva", RANGE))
                        .done();

        List<Failure<Void>> failures = failuresWith(rules);

        assertEquals(1, failures.size());
        assertEquals("regla nueva", failures.get(0).message());
    }

    @Test
    void aNodeWithoutRuleIsReportedAsUnsupported() {
        List<Failure<Void>> failures = failuresWith(SemanticRules.registry().done());

        assertEquals(1, failures.size());
        assertEquals(
                "Nodo PrintlnStatement no soportado por PrintScript", failures.get(0).message());
        assertEquals(RANGE, failures.get(0).range());
    }

    @Test
    void anExpressionWithoutRuleIsReportedAsUnsupported() {
        SemanticRules rules =
                SemanticRules.registry()
                        .check(
                                PrintlnStatement.class,
                                (println, checking) ->
                                        checking.typeOf(println.expression()).map(type -> null))
                        .done();

        assertEquals(
                "Nodo NumberLiteral no soportado por PrintScript",
                failuresWith(rules).get(0).message());
    }

    @Test
    void aNodeCannotHaveTwoRules() {
        SemanticRules.Registry registry =
                SemanticRules.registry()
                        .typing(NumberLiteral.class, (n, c) -> null)
                        .check(PrintlnStatement.class, (p, c) -> null);

        assertThrows(
                IllegalArgumentException.class,
                () -> registry.check(PrintlnStatement.class, (p, c) -> null));
        assertThrows(
                IllegalArgumentException.class,
                () -> registry.typing(NumberLiteral.class, (n, c) -> null));
    }
}
