package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.printscript.formatter.rules.TokenShapes.isKeyword;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.printscript.common.Success;
import org.printscript.common.Version;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.config.FormatRules;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.formatter.rules.ActiveRule;
import org.printscript.formatter.rules.FormatRule;
import org.printscript.formatter.rules.Layout;
import org.printscript.formatter.rules.RuleDefinition;
import org.printscript.lexer.LexerFactory;
import org.printscript.token.Token;

// Agregar una regla de formato es escribir la regla y registrarla: Gaps, Formatter y
// FormatterConfiguration no se tocan.
class ExtensionTest {

    private static final String KEY = "test-space-after-println";

    // una regla que no existe en la consigna: dos espacios entre println y el paréntesis
    private record SpaceAfterPrintln() implements FormatRule, ActiveRule {

        @Override
        public ActiveRule start() {
            return this;
        }

        @Override
        public Optional<String> gap(Token previous, Token current, Layout layout) {
            return isKeyword(previous, "println") ? Optional.of("  ") : Optional.empty();
        }
    }

    private static final RuleDefinition DEFINITION =
            RuleDefinition.flag(KEY, 5, new SpaceAfterPrintln());

    private static String format(String source, FormatterConfiguration configuration) {
        Formatter formatter =
                new Formatter(
                        LexerFactory.create(
                                List.of(source.split("\n", -1)).iterator(), Version.V11),
                        configuration);
        StringBuilder out = new StringBuilder();
        while (formatter.hasNext()) {
            out.append(((Success<String>) formatter.next()).value());
        }
        return out.toString();
    }

    @Test
    void aNewRuleChangesTheFormattingWithoutTouchingTheFormatter() {
        FormatterConfiguration configuration =
                FormatRules.standard()
                        .with(DEFINITION)
                        .read(new RulesConfiguration(Map.of(KEY, true)));

        assertEquals("println  (1);", format("println(1);", configuration));
    }

    @Test
    void aNewRuleWorksTogetherWithTheStandardOnes() {
        FormatterConfiguration configuration =
                FormatRules.standard()
                        .with(DEFINITION)
                        .read(
                                new RulesConfiguration(
                                        Map.of(
                                                KEY,
                                                true,
                                                "mandatory-single-space-separation",
                                                true)));

        // la regla nueva tiene prioridad 5, así que se consulta antes que el espacio simple:
        // gana entre println y el paréntesis, y el resto lo separa el espacio simple
        assertEquals("println  ( 1 );", format("println(1);", configuration));
    }

    @Test
    void theKeyOfTheNewRuleIsKnown() {
        Set<String> keys = FormatRules.standard().with(DEFINITION).keys();

        assertTrue(keys.contains(KEY));
        assertTrue(keys.containsAll(FormatterConfiguration.KEYS));
        assertEquals(FormatterConfiguration.KEYS.size() + 1, keys.size());
    }
}
