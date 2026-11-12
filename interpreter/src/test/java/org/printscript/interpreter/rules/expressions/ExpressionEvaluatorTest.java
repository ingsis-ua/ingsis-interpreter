package org.printscript.interpreter.rules.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Operator;
import org.printscript.ast.StringLiteral;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.interpreter.Ports;
import org.printscript.interpreter.Run;
import org.printscript.interpreter.engine.Execution;
import org.printscript.interpreter.state.Environment;

class ExpressionEvaluatorTest {

    private static final Range ANY = rangeAt(1, 1, 1, 1);

    private static Execution newEvaluator(Environment environment) {
        return Run.execution(
                environment,
                Ports.of(
                        new org.printscript.runtime.OutputPrinter() {

                            @Override
                            public void println(String line) {
                                // los tests de expresiones no miran la salida
                            }

                            @Override
                            public void print(String text) {
                                // los tests de expresiones no miran la salida
                            }
                        },
                        List.of(),
                        Map.of()));
    }

    private static Range rangeAt(int startLine, int startColumn, int endLine, int endColumn) {
        return new Range(new Position(startLine, startColumn), new Position(endLine, endColumn));
    }

    private static NumberLiteral number(double value) {
        return new NumberLiteral(value, ANY);
    }

    private static StringLiteral text(String value) {
        return new StringLiteral(value, ANY);
    }

    private static BinaryExpression binary(Expression left, Operator operator, Expression right) {
        return new BinaryExpression(left, operator, right, ANY);
    }

    private static Object evaluate(Expression expression, Environment environment) {
        Result<Object> result = newEvaluator(environment).evaluate(expression);
        return assertInstanceOf(Success.class, result, String.valueOf(result)).value();
    }

    private static Failure<Object> evaluateFailing(Expression expression, Environment environment) {
        Result<Object> result = newEvaluator(environment).evaluate(expression);
        @SuppressWarnings("unchecked")
        Failure<Object> failure = assertInstanceOf(Failure.class, result, String.valueOf(result));
        return failure;
    }

    @Test
    void theArithmeticFollowsThePrecedenceAlreadyResolvedByTheTree() {
        Expression multiplyLast =
                binary(binary(number(2), Operator.PLUS, number(3)), Operator.MULTIPLY, number(4));
        Expression multiplyFirst =
                binary(number(2), Operator.PLUS, binary(number(3), Operator.MULTIPLY, number(4)));

        assertEquals(20.0, evaluate(multiplyLast, new Environment()));
        assertEquals(14.0, evaluate(multiplyFirst, new Environment()));
    }

    @Test
    void twelveDividedByFourEvaluatesToAnExactInteger() {
        Expression division = binary(number(12), Operator.DIVIDE, number(4));

        assertEquals(3.0, evaluate(division, new Environment()));
    }

    @Test
    void concatenationJoinsStringsAndNumbersInBothOrders() {
        Environment environment = new Environment();

        assertEquals(
                "Joe Doe", evaluate(binary(text("Joe"), Operator.PLUS, text(" Doe")), environment));
        assertEquals(
                "Result: 3",
                evaluate(binary(text("Result: "), Operator.PLUS, number(3)), environment));
        assertEquals(
                "3 is the result",
                evaluate(binary(number(3), Operator.PLUS, text(" is the result")), environment));
    }

    @Test
    void aLargeIntegerKeepsItsValueWhenConcatenated() {
        Expression concatenation = binary(text("total: "), Operator.PLUS, number(1e20));

        assertEquals("total: 100000000000000000000", evaluate(concatenation, new Environment()));
    }

    @Test
    void anUndeclaredIdentifierFailsWithItsRange() {
        Range range = rangeAt(2, 5, 2, 9);

        Failure<Object> failure =
                evaluateFailing(new Identifier("total", range), new Environment());

        assertEquals(range, failure.range());
        assertTrue(failure.message().contains("total"), failure.message());
    }

    @Test
    void anInvalidOperationFailsWithTheRangeOfTheExpression() {
        Range range = rangeAt(4, 1, 4, 11);
        Expression invalid = new BinaryExpression(text("a"), Operator.MINUS, text("b"), range);

        Failure<Object> failure = evaluateFailing(invalid, new Environment());

        assertEquals(range, failure.range());
        assertTrue(failure.message().contains("aplicar -"), failure.message());
        assertTrue(failure.message().contains("string"), failure.message());
    }

    @Test
    void booleanLiteralsEvaluateToTheirValue() {
        assertEquals(true, evaluate(new BooleanLiteral(true, ANY), new Environment()));
        assertEquals(false, evaluate(new BooleanLiteral(false, ANY), new Environment()));
    }

    // semantic lo rechaza antes, pero el intérprete no puede quedarse mudo si le llega
    // igual: la red de seguridad tiene que nombrar el tipo correcto
    @Test
    void anOperationOverABooleanNamesTheTypeInTheMessage() {
        Expression expression = binary(new BooleanLiteral(true, ANY), Operator.MINUS, number(1));

        assertEquals(
                "No se puede aplicar - entre boolean y number",
                evaluateFailing(expression, new Environment()).message());
    }
}
