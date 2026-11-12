package org.printscript.interpreter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.Expression;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Operator;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;

// el registro es lo único que sabe qué regla atiende a cada nodo
class NodeRulesTest {

    private static final Range RANGE = new Range(new Position(1, 1), new Position(1, 4));

    private final List<String> calls = new ArrayList<>();

    private final StatementRule<PrintlnStatement> printlnRule =
            new StatementRule<>() {
                @Override
                public Result<Void> execute(PrintlnStatement statement, Execution execution) {
                    calls.add("println");
                    return new Success<>(null);
                }

                @Override
                public List<Object> children(PrintlnStatement statement) {
                    return List.of(statement.expression());
                }
            };

    private final ExpressionRule<BinaryExpression> binaryRule =
            new ExpressionRule<>() {
                @Override
                public Result<Object> evaluate(BinaryExpression expression, Execution execution) {
                    return new Success<>(0.0);
                }

                @Override
                public List<Object> children(BinaryExpression expression) {
                    return List.of(expression.left(), expression.right());
                }
            };

    private NodeRules rules() {
        return NodeRules.builder()
                .statement(PrintlnStatement.class, printlnRule)
                .expression(BinaryExpression.class, binaryRule)
                .build();
    }

    private static NumberLiteral number(double value) {
        return new NumberLiteral(value, RANGE);
    }

    @Test
    void registeredStatementIsSupported() {
        assertTrue(rules().supports(PrintlnStatement.class));
        assertFalse(rules().supports(IfStatement.class));
    }

    @Test
    void ruleLookupReturnsTheRegisteredRule() {
        Statement println = new PrintlnStatement(number(1), RANGE);

        StatementRule<Statement> rule = rules().statementRuleFor(println).orElseThrow();
        rule.execute(println, null);

        assertEquals(List.of("println"), calls);
    }

    @Test
    void missingRuleIsEmpty() {
        Expression bool = new BooleanLiteral(true, RANGE);

        assertEquals(Optional.empty(), rules().expressionRuleFor(bool));
    }

    @Test
    void childrenComeFromTheRule() {
        NumberLiteral left = number(1);
        NumberLiteral right = number(2);
        BinaryExpression binary = new BinaryExpression(left, Operator.PLUS, right, RANGE);

        assertEquals(List.of(left, right), rules().childrenOf(binary));
        assertEquals(List.of(), rules().childrenOf(new BooleanLiteral(true, RANGE)));
    }

    @Test
    void registeringTheSameClassTwiceFails() {
        NodeRules.Builder builder =
                NodeRules.builder().statement(PrintlnStatement.class, printlnRule);

        assertThrows(
                IllegalArgumentException.class,
                () -> builder.statement(PrintlnStatement.class, printlnRule));
    }

    @Test
    void unsupportedMessage() {
        Failure<Void> failure = NodeRules.unsupported(number(1), RANGE);

        assertEquals("Nodo NumberLiteral no soportado por PrintScript", failure.message());
        assertEquals(RANGE, failure.range());
    }

    @Test
    void onlyKeepsTheRulesOfTheChosenNodes() {
        NodeRules only = rules().only(type -> type == PrintlnStatement.class);

        assertTrue(only.supports(PrintlnStatement.class));
        assertFalse(only.supports(BinaryExpression.class));
        assertTrue(rules().supports(BinaryExpression.class));
    }
}
