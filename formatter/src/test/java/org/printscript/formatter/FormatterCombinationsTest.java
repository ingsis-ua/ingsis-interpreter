package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.printscript.common.Success;
import org.printscript.common.Version;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.config.FormatterConfiguration;
import org.printscript.lexer.LexerFactory;

// Varias reglas prendidas a la vez compiten por el mismo espacio: gana la que se consulta
// primero. Estos casos fijan esa prioridad.
class FormatterCombinationsTest {

    private static String format(String source, Map<String, Object> rules) {
        Formatter formatter =
                new Formatter(
                        LexerFactory.create(
                                List.of(source.split("\n", -1)).iterator(), Version.V11),
                        FormatterConfiguration.from(new RulesConfiguration(rules)));
        StringBuilder out = new StringBuilder();
        while (formatter.hasNext()) {
            out.append(((Success<String>) formatter.next()).value());
        }
        return out.toString();
    }

    @Test
    void printlnBreaksWinOverTheStatementBreak() {
        assertEquals(
                "println(1);\n\nlet a: number=2;\nprintln(a);\n\nprintln(a);",
                format(
                        "println(1);let a: number=2;println(a);println(a);",
                        Map.of(
                                "line-breaks-after-println",
                                1,
                                "mandatory-line-break-after-statement",
                                true)));
    }

    @Test
    void operationsAndSingleSpaceKeepTheNegationAttached() {
        assertEquals(
                "let x : number = 2 * -3 - -y;",
                format(
                        "let  x:number=2*-3- -y;",
                        Map.of(
                                "mandatory-space-surrounding-operations", true,
                                "mandatory-single-space-separation", true)));
    }

    @Test
    void braceBelowWithIndentAndStatementBreaks() {
        assertEquals(
                "if (a)\n{println(a);\n  println(a);\n}",
                format(
                        "if (a) {println(a);println(a);}",
                        Map.of(
                                "if-brace-below-line", true,
                                "indent-inside-if", 2,
                                "mandatory-line-break-after-statement", true)));
    }

    @Test
    void noSpacingAroundEqualsWinsOverSingleSpace() {
        assertEquals(
                "let a : number=1;",
                format(
                        "let a : number = 1 ;",
                        Map.of(
                                "enforce-no-spacing-around-equals", true,
                                "mandatory-single-space-separation", true)));
    }

    @Test
    void colonRulesWithSingleSpace() {
        assertEquals(
                "let a : number = 1;",
                format(
                        "let a:number=1;",
                        Map.of(
                                "enforce-spacing-before-colon-in-declaration", true,
                                "enforce-spacing-after-colon-in-declaration", true,
                                "mandatory-single-space-separation", true)));
    }
}
