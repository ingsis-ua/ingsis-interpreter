package org.printscript.formatter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.printscript.config.RulesConfiguration;
import org.printscript.formatter.rules.BracePlacement;
import org.printscript.formatter.rules.EqualsSpacing;
import org.printscript.formatter.rules.FormatRule;
import org.printscript.formatter.rules.IfIndent;
import org.printscript.formatter.rules.OperationsSpacing;
import org.printscript.formatter.rules.PrintlnBreaks;
import org.printscript.formatter.rules.SingleSpace;
import org.printscript.formatter.rules.SpaceAfterColon;
import org.printscript.formatter.rules.SpaceBeforeColon;
import org.printscript.formatter.rules.StatementBreak;

// Las reglas de formato, tal como las nombra la consigna.
// Todas son opcionales y ninguna tiene un valor por defecto
public record FormatterConfiguration(
        Optional<Boolean> spacingAroundEquals,
        boolean spacingBeforeColon,
        boolean spacingAfterColon,
        boolean spaceSurroundingOperations,
        boolean singleSpaceSeparation,
        boolean lineBreakAfterStatement,
        Optional<Integer> lineBreaksAfterPrintln,
        Optional<Boolean> ifBraceOnSameLine,
        Optional<Integer> indentInsideIf) {

    public static final String SPACING_AROUND_EQUALS_KEY = "enforce-spacing-around-equals";
    public static final String NO_SPACING_AROUND_EQUALS_KEY = "enforce-no-spacing-around-equals";
    public static final String SPACING_BEFORE_COLON_KEY =
            "enforce-spacing-before-colon-in-declaration";
    public static final String SPACING_AFTER_COLON_KEY =
            "enforce-spacing-after-colon-in-declaration";
    public static final String SPACE_SURROUNDING_OPERATIONS_KEY =
            "mandatory-space-surrounding-operations";
    public static final String SINGLE_SPACE_SEPARATION_KEY = "mandatory-single-space-separation";
    public static final String LINE_BREAK_AFTER_STATEMENT_KEY =
            "mandatory-line-break-after-statement";
    public static final String LINE_BREAKS_AFTER_PRINTLN_KEY = "line-breaks-after-println";
    public static final String IF_BRACE_SAME_LINE_KEY = "if-brace-same-line";
    public static final String IF_BRACE_BELOW_LINE_KEY = "if-brace-below-line";
    public static final String INDENT_INSIDE_IF_KEY = "indent-inside-if";

    public static final Set<String> KEYS =
            Set.of(
                    SPACING_AROUND_EQUALS_KEY,
                    NO_SPACING_AROUND_EQUALS_KEY,
                    SPACING_BEFORE_COLON_KEY,
                    SPACING_AFTER_COLON_KEY,
                    SPACE_SURROUNDING_OPERATIONS_KEY,
                    SINGLE_SPACE_SEPARATION_KEY,
                    LINE_BREAK_AFTER_STATEMENT_KEY,
                    LINE_BREAKS_AFTER_PRINTLN_KEY,
                    IF_BRACE_SAME_LINE_KEY,
                    IF_BRACE_BELOW_LINE_KEY,
                    INDENT_INSIDE_IF_KEY);

    // nada activado: el archivo sale tal como entró
    public static FormatterConfiguration none() {
        return new FormatterConfiguration(
                Optional.empty(),
                false,
                false,
                false,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public FormatterConfiguration {
        lineBreaksAfterPrintln.ifPresent(FormatterConfiguration::requireNotNegative);
        indentInsideIf.ifPresent(FormatterConfiguration::requireNotNegative);
    }

    public static FormatterConfiguration from(RulesConfiguration rules) {
        return new FormatterConfiguration(
                equalsSpacingOf(rules),
                rules.getBoolean(SPACING_BEFORE_COLON_KEY).orElse(false),
                rules.getBoolean(SPACING_AFTER_COLON_KEY).orElse(false),
                rules.getBoolean(SPACE_SURROUNDING_OPERATIONS_KEY).orElse(false),
                rules.getBoolean(SINGLE_SPACE_SEPARATION_KEY).orElse(false),
                rules.getBoolean(LINE_BREAK_AFTER_STATEMENT_KEY).orElse(false),
                rules.getInt(LINE_BREAKS_AFTER_PRINTLN_KEY),
                bracePlacementOf(rules),
                rules.getInt(INDENT_INSIDE_IF_KEY));
    }

    // las reglas activas, en el orden en que se consultan: gana la primera que opine
    public List<FormatRule> rules() {
        List<FormatRule> rules = new ArrayList<>();
        lineBreaksAfterPrintln.ifPresent(howMany -> rules.add(new PrintlnBreaks(howMany)));
        if (lineBreakAfterStatement) {
            rules.add(new StatementBreak());
        }
        ifBraceOnSameLine.ifPresent(sameLine -> rules.add(new BracePlacement(sameLine)));
        spacingAroundEquals.ifPresent(spaced -> rules.add(new EqualsSpacing(spaced)));
        if (spacingBeforeColon) {
            rules.add(new SpaceBeforeColon());
        }
        if (spacingAfterColon) {
            rules.add(new SpaceAfterColon());
        }
        if (spaceSurroundingOperations) {
            rules.add(new OperationsSpacing());
        }
        if (singleSpaceSeparation) {
            rules.add(new SingleSpace());
        }
        indentInsideIf.ifPresent(size -> rules.add(new IfIndent(size)));
        return List.copyOf(rules);
    }

    // dos claves opuestas sobre el mismo espacio: la que pide espacio manda si están las dos
    private static Optional<Boolean> equalsSpacingOf(RulesConfiguration rules) {
        if (rules.getBoolean(SPACING_AROUND_EQUALS_KEY).orElse(false)) {
            return Optional.of(true);
        }
        if (rules.getBoolean(NO_SPACING_AROUND_EQUALS_KEY).orElse(false)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private static Optional<Boolean> bracePlacementOf(RulesConfiguration rules) {
        if (rules.getBoolean(IF_BRACE_SAME_LINE_KEY).orElse(false)) {
            return Optional.of(true);
        }
        if (rules.getBoolean(IF_BRACE_BELOW_LINE_KEY).orElse(false)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private static void requireNotNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("El valor no puede ser negativo: " + value);
        }
    }
}
