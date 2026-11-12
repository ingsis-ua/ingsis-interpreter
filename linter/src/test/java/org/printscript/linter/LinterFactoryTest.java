package org.printscript.linter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Operator;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.ReadInput;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.config.RulesConfiguration;
import org.printscript.linter.config.LinterConfiguration;

class LinterFactoryTest {

    private static final Range ANY = new Range(new Position(1, 1), new Position(1, 1));
    private static final String MAL = "mal_escrito";
    private static final String FORMAT = "identifier_format";
    private static final String CAMEL = "camel case";
    private static final String PRINTLN = "mandatory-variable-or-literal-in-println";
    private static final String READ_INPUT = "mandatory-variable-or-literal-in-readInput";

    // la configuración con las mismas claves que escribiría el usuario en su archivo
    private static LinterConfiguration config(Map<String, Object> rules) {
        return LinterConfiguration.from(new RulesConfiguration(rules));
    }

    private static final class OneStatement implements Iterator<Result<Statement>> {

        private final Statement statement;
        private boolean served;

        private OneStatement(Statement statement) {
            this.statement = statement;
        }

        @Override
        public boolean hasNext() {
            return !served;
        }

        @Override
        public Result<Statement> next() {
            served = true;
            return new Success<>(statement);
        }
    }

    private static Statement printlnOfASum(String name) {
        return new PrintlnStatement(
                new BinaryExpression(
                        new Identifier(name, ANY), Operator.PLUS, new NumberLiteral(1.0, ANY), ANY),
                ANY);
    }

    private static List<Violation> violationsOf(Statement statement, LinterConfiguration config) {
        Linter linter = LinterFactory.create(new OneStatement(statement), config);
        if (linter.next() instanceof Success<List<Violation>> success) {
            return success.value();
        }
        throw new AssertionError("se esperaba un Success");
    }

    @Test
    void withEverythingOffNothingIsReported() {
        LinterConfiguration config = config(Map.of());

        assertTrue(violationsOf(printlnOfASum(MAL), config).isEmpty());
    }

    @Test
    void withOnlyTheFormatRuleOnOnlyTheIdentifierIsReported() {
        LinterConfiguration config = config(Map.of(FORMAT, CAMEL));

        List<Violation> violations = violationsOf(printlnOfASum(MAL), config);

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).message().contains(MAL), violations.get(0).message());
    }

    @Test
    void withOnlyThePrintlnRuleOnOnlyTheArgumentIsReported() {
        LinterConfiguration config = config(Map.of(PRINTLN, true));

        List<Violation> violations = violationsOf(printlnOfASum(MAL), config);

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).message().contains("println"), violations.get(0).message());
    }

    @Test
    void withBothRulesOnBothAreReported() {
        LinterConfiguration config = config(Map.of(FORMAT, CAMEL, PRINTLN, true));

        assertEquals(2, violationsOf(printlnOfASum(MAL), config).size());
    }

    @Test
    void aCleanStatementIsReportedCleanWithEveryRuleOn() {
        LinterConfiguration config = config(Map.of(FORMAT, CAMEL, PRINTLN, true));
        Statement println = new PrintlnStatement(new Identifier("bienEscrito", ANY), ANY);

        assertTrue(violationsOf(println, config).isEmpty());
    }

    // el identificador "mal_escrito" viola camel case pero cumple snake case: sirve para
    // comprobar que la factory elige el checker que corresponde y no siempre el mismo
    @Test
    void snakeCaseProducesItsOwnChecker() {
        LinterConfiguration snake = config(Map.of(FORMAT, "snake case"));
        LinterConfiguration camel = config(Map.of(FORMAT, CAMEL));

        assertEquals(List.of(), violationsOf(printlnOfASum(MAL), snake));
        assertEquals(1, violationsOf(printlnOfASum(MAL), camel).size());
    }

    @Test
    void theReadInputRuleIsWiredOnlyWhenItIsOn() {
        Statement statement =
                new PrintlnStatement(
                        new ReadInput(
                                new BinaryExpression(
                                        new StringLiteral("a", ANY),
                                        Operator.PLUS,
                                        new StringLiteral("b", ANY),
                                        ANY),
                                ANY),
                        ANY);

        LinterConfiguration encendida = config(Map.of(READ_INPUT, true));
        LinterConfiguration apagada = config(Map.of());

        assertEquals(1, violationsOf(statement, encendida).size());
        assertEquals(List.of(), violationsOf(statement, apagada));
    }

    // todas las violaciones caen en la misma posición: el desempate sigue el orden de las
    // reglas (formato de identificador, println, readInput)
    @Test
    void violationsAtTheSamePositionFollowTheOrderOfTheRules() {
        Statement statement =
                new PrintlnStatement(
                        new ReadInput(
                                new BinaryExpression(
                                        new Identifier(MAL, ANY),
                                        Operator.PLUS,
                                        new StringLiteral("b", ANY),
                                        ANY),
                                ANY),
                        ANY);

        List<String> messages =
                violationsOf(
                                statement,
                                config(Map.of(FORMAT, CAMEL, PRINTLN, true, READ_INPUT, true)))
                        .stream()
                        .map(Violation::message)
                        .toList();

        assertEquals(
                List.of(
                        "El identificador '" + MAL + "' debe estar escrito en camel case",
                        "println solo puede llamarse con un identificador o un literal",
                        "readInput solo puede llamarse con un identificador o un literal"),
                messages);
    }
}
