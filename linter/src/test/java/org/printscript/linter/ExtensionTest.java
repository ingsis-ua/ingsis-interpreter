package org.printscript.linter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.printscript.ast.Identifier;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.Statement;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.config.RulesConfiguration;
import org.printscript.linter.config.LintRules;
import org.printscript.linter.config.LinterConfiguration;
import org.printscript.linter.rules.IdentifierFormatRule;
import org.printscript.linter.rules.LintRule;
import org.printscript.linter.rules.LintRuleDefinition;
import org.printscript.linter.rules.PrintlnArgumentRule;
import org.printscript.linter.rules.ReadInputArgumentRule;
import org.printscript.linter.rules.format.IdentifierFormatChecker;
import org.printscript.linter.rules.format.IdentifierFormats;

// Agregar una regla de lint o un formato de identificador es escribir la clase y registrarla:
// Linter, LinterFactory, LinterConfiguration e IdentifierFormatRule no se tocan.
class ExtensionTest {

    private static final Range AT = new Range(new Position(1, 1), new Position(1, 15));
    private static final String NO_PRINTLN = "test-no-println";
    private static final String FORMAT = "identifier_format";

    // una regla que no existe en la consigna: ningún println
    private record NoPrintln() implements LintRule {

        @Override
        public List<Violation> check(Statement statement) {
            return statement instanceof PrintlnStatement
                    ? List.of(new Violation("no se permite println", statement.range()))
                    : List.of();
        }
    }

    // un formato que no existe en la consigna: arranca en mayúscula
    private record PascalCase() implements IdentifierFormatChecker {

        @Override
        public boolean isValid(String name) {
            return !name.isEmpty() && Character.isUpperCase(name.charAt(0));
        }

        @Override
        public String expectedFormat() {
            return "pascal case";
        }

        @Override
        public List<String> spellings() {
            return List.of("pascal case", "pascalcase", "pascal_case");
        }
    }

    private static final LintRuleDefinition NO_PRINTLN_DEFINITION =
            LintRuleDefinition.flag(NO_PRINTLN, new NoPrintln());

    // las reglas de la consigna, pero con un formato más en el registro de formatos
    private static LintRules withPascalCase() {
        return new LintRules(
                List.of(
                        IdentifierFormatRule.definition(
                                IdentifierFormats.standard().with(new PascalCase())),
                        PrintlnArgumentRule.DEFINITION,
                        ReadInputArgumentRule.DEFINITION));
    }

    private static List<String> messagesOf(Statement statement, LinterConfiguration config) {
        Iterator<Result<Statement>> one =
                List.<Result<Statement>>of(new Success<>(statement)).iterator();
        Linter linter = LinterFactory.create(one, config);
        if (linter.next() instanceof Success<List<Violation>> success) {
            return success.value().stream().map(Violation::message).toList();
        }
        throw new AssertionError("se esperaba un Success");
    }

    private static Statement printlnOf(String name) {
        return new PrintlnStatement(new Identifier(name, AT), AT);
    }

    @Test
    void aNewRuleIsReportedWithoutTouchingTheLinter() {
        LinterConfiguration config =
                LintRules.standard()
                        .with(NO_PRINTLN_DEFINITION)
                        .read(new RulesConfiguration(Map.of(NO_PRINTLN, true)));

        assertEquals(List.of("no se permite println"), messagesOf(printlnOf("x"), config));
    }

    @Test
    void theKeyOfTheNewRuleIsKnown() {
        Set<String> keys = LintRules.standard().with(NO_PRINTLN_DEFINITION).keys();

        assertTrue(keys.contains(NO_PRINTLN));
        assertTrue(keys.containsAll(LinterConfiguration.KEYS));
        assertEquals(LinterConfiguration.KEYS.size() + 1, keys.size());
    }

    @Test
    void aNewFormatIsCheckedWithoutTouchingTheFormatRule() {
        LinterConfiguration config =
                withPascalCase().read(new RulesConfiguration(Map.of(FORMAT, "PascalCase")));

        assertEquals(List.of(), messagesOf(printlnOf("MiVariable"), config));
        assertEquals(
                List.of("El identificador 'miVariable' debe estar escrito en pascal case"),
                messagesOf(printlnOf("miVariable"), config));
    }

    @Test
    void aNewFormatIsListedAmongTheSupportedOnes() {
        RulesConfiguration kebab = new RulesConfiguration(Map.of(FORMAT, "kebab case"));

        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> withPascalCase().read(kebab));

        assertEquals(
                "Formato de identificador no soportado: 'kebab case'. Soportados: camel case,"
                        + " snake case, pascal case",
                thrown.getMessage());
    }
}
