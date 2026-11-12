package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.Failure;
import org.printscript.common.Success;
import org.printscript.common.Version;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.lexer.LexerFactory;

// El formatter reescribe el fuente: lo que no pide ninguna regla activa tiene que salir
// exactamente como entró. Cada test prende una sola regla, que es como las usa la consigna.
class FormatterTest {

    // una regla por vez, sobre la configuración vacía, con las mismas claves del archivo
    // de reglas
    private static final class Rules {

        private final Map<String, Object> values = new HashMap<>();

        private Rules with(String key, Object value) {
            values.put(key, value);
            return this;
        }

        private Rules equalsSpacing(boolean value) {
            return with(
                    value ? "enforce-spacing-around-equals" : "enforce-no-spacing-around-equals",
                    true);
        }

        private Rules afterColon() {
            return with("enforce-spacing-after-colon-in-declaration", true);
        }

        private Rules beforeColon() {
            return with("enforce-spacing-before-colon-in-declaration", true);
        }

        private Rules operations() {
            return with("mandatory-space-surrounding-operations", true);
        }

        private Rules singleSpace() {
            return with("mandatory-single-space-separation", true);
        }

        private Rules breakAfterStatement() {
            return with("mandatory-line-break-after-statement", true);
        }

        private Rules printlnBreaks(int value) {
            return with("line-breaks-after-println", value);
        }

        private Rules braceSameLine(boolean value) {
            return with(value ? "if-brace-same-line" : "if-brace-below-line", true);
        }

        private Rules indent(int value) {
            return with("indent-inside-if", value);
        }

        private FormatterConfiguration build() {
            return FormatterConfiguration.from(new RulesConfiguration(values));
        }
    }

    private static String format(String source, Rules rules) {
        return format(source, rules.build());
    }

    private static String format(String source, FormatterConfiguration configuration) {
        Formatter formatter = formatterOver(source, configuration);
        StringBuilder out = new StringBuilder();
        while (formatter.hasNext()) {
            out.append(((Success<String>) formatter.next()).value());
        }
        return out.toString();
    }

    private static Formatter formatterOver(String source, FormatterConfiguration configuration) {
        return new Formatter(
                LexerFactory.create(List.of(source.split("\n", -1)).iterator(), Version.V11),
                configuration);
    }

    @Test
    void withoutAnyRuleTheSourceComesOutUntouched() {
        String source =
                "let something:string = \"cosa\";\nlet other  :  number=  2;\nprintln(other);";

        assertEquals(source, format(source, FormatterConfiguration.none()));
    }

    @Test
    void spacingAroundEqualsOnlyTouchesTheEquals() {
        String source = "let a: string= \"x\";\nlet b: string =\"y\";";

        assertEquals(
                "let a: string = \"x\";\nlet b: string = \"y\";",
                format(source, new Rules().equalsSpacing(true)));
    }

    @Test
    void noSpacingAroundEqualsIsTheOppositeRule() {
        String source = "let a: string = \"x\";\nlet b: string =\"y\";";

        assertEquals(
                "let a: string=\"x\";\nlet b: string=\"y\";",
                format(source, new Rules().equalsSpacing(false)));
    }

    // el caso que separa a este formatter de un pretty-printer: la regla mira el espacio de
    // después del ':' y el de antes queda como estaba, aunque quede inconsistente
    @Test
    void spacingAfterColonLeavesTheSpaceBeforeTheColonAlone() {
        String source = "let a:string = \"x\";\nlet b : string = \"y\";";

        assertEquals(
                "let a: string = \"x\";\nlet b : string = \"y\";",
                format(source, new Rules().afterColon()));
    }

    @Test
    void spacingBeforeColonLeavesTheSpaceAfterItAlone() {
        String source = "let a:string = \"x\";";

        assertEquals("let a :string = \"x\";", format(source, new Rules().beforeColon()));
    }

    @Test
    void spaceSurroundingOperationsDoesNotTouchTheEquals() {
        String source = "let a: number=1+2*3;";

        assertEquals("let a: number=1 + 2 * 3;", format(source, new Rules().operations()));
    }

    @Test
    void singleSpaceSeparationCollapsesEverythingButKeepsTheSemicolonAttached() {
        String source = "let a:      string=\"x\";\nprintln(a);";

        assertEquals(
                "let a : string = \"x\";\nprintln ( a );",
                format(source, new Rules().singleSpace()));
    }

    @Test
    void lineBreakAfterStatementOnlySplitsWhatSharesALine() {
        String source = "let a: number=1;let b: number=2;\nlet c: number=3;";

        assertEquals(
                "let a: number=1;\nlet b: number=2;\nlet c: number=3;",
                format(source, new Rules().breakAfterStatement()));
    }

    @Test
    void lineBreaksAfterPrintlnAppliesOnlyAfterAPrintln() {
        String source = "let a: number=1;\nprintln(a);\nprintln(a);";

        assertEquals(
                "let a: number=1;\nprintln(a);\n\n\nprintln(a);",
                format(source, new Rules().printlnBreaks(2)));
    }

    @Test
    void zeroLineBreaksAfterPrintlnCollapsesTheBlankLinesThatWereThere() {
        String source = "println(1);\n\n\n\nprintln(2);";

        assertEquals("println(1);\nprintln(2);", format(source, new Rules().printlnBreaks(0)));
    }

    @Test
    void theBraceOfAnIfGoesWhereTheRuleSaysAndTheBodyIsNotTouched() {
        String sameLine = "let a: boolean = true;\nif (a)\n{\n  println(a);\n}";
        String below = "let a: boolean = true;\nif (a) {\n  println(a);\n}";

        assertEquals(
                "let a: boolean = true;\nif (a) {\n  println(a);\n}",
                format(sameLine, new Rules().braceSameLine(true)));
        assertEquals(
                "let a: boolean = true;\nif (a)\n{\n  println(a);\n}",
                format(below, new Rules().braceSameLine(false)));
    }

    @Test
    void indentInsideIfReindentsEveryLevelAndAlignsTheClosingBrace() {
        String source = "if (a) {\n  if (a) {\n    println(a);\n  }\n}";

        assertEquals(
                "if (a) {\n    if (a) {\n        println(a);\n    }\n}",
                format(source, new Rules().indent(4)));
    }

    @Test
    void unknownSymbolsAreReportedAsFailuresAndStillWritten() {
        String source = "let x: number = 5 @ 3;";
        Formatter formatter = formatterOver(source, FormatterConfiguration.none());

        StringBuilder written = new StringBuilder();
        List<String> failures = new ArrayList<>();
        while (formatter.hasNext()) {
            switch (formatter.next()) {
                case Success<String> piece -> written.append(piece.value());
                case Failure<String> failure -> failures.add(failure.message());
            }
        }

        assertEquals(source, written.toString());
        assertEquals(List.of("Símbolo no reconocido: '@'"), failures);
    }

    @Test
    void anUnclosedStringKeepsTheRestOfItsLine() {
        String source = "println(\"abc);\nprintln(x);";
        Formatter formatter = formatterOver(source, FormatterConfiguration.none());

        StringBuilder written = new StringBuilder();
        List<String> failures = new ArrayList<>();
        while (formatter.hasNext()) {
            switch (formatter.next()) {
                case Success<String> piece -> written.append(piece.value());
                case Failure<String> failure -> failures.add(failure.message());
            }
        }

        assertEquals(source, written.toString());
        assertEquals(1, failures.size());
    }

    @Test
    void eachStringKeepsTheQuoteItWasWrittenWith() {
        String source = "let s: string = 'hola' + \"chau\" + 'it\"s';";

        assertEquals(source, format(source, FormatterConfiguration.none()));
    }

    @Test
    void theOperationsRuleDoesNotSeparateANegationFromItsOperand() {
        assertEquals(
                "println(2 * -3 - -x);", format("println(2*-3- -x);", new Rules().operations()));
    }

    @Test
    void singleSpaceSeparationKeepsANegationAttachedToItsOperand() {
        assertEquals(
                "let x : number = -5;",
                format("let  x :  number  =  -  5;", new Rules().singleSpace()));
    }

    @Test
    void withoutRulesTheSpaceAfterANegationIsKept() {
        String source = "println(- 5);";

        assertEquals(source, format(source, FormatterConfiguration.none()));
    }

    // el runner reusa la misma configuración para cada archivo: lo que una regla recuerda
    // (si está dentro de un println, si viene un menos unario) no puede pasar de uno al otro
    @Test
    void aConfigurationCanFormatTwiceWithoutCarryingState() {
        FormatterConfiguration rules = new Rules().printlnBreaks(1).operations().build();
        String source = "println(1);\nlet a: number=2*-3;";

        String first = format(source, rules);

        assertEquals("println(1);\n\nlet a: number=2 * -3;", first);
        assertEquals(first, format(source, rules));
    }
}
