package org.printscript.linter.rules.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// El registro de los formatos de identificador que se pueden pedir en el archivo de reglas.
public final class IdentifierFormats {

    private final List<IdentifierFormatChecker> formats;

    public IdentifierFormats(List<IdentifierFormatChecker> formats) {
        this.formats = List.copyOf(formats);
    }

    public static IdentifierFormats standard() {
        return new IdentifierFormats(List.of(new CamelCaseChecker(), new SnakeCaseChecker()));
    }

    public IdentifierFormats with(IdentifierFormatChecker extra) {
        List<IdentifierFormatChecker> all = new ArrayList<>(formats);
        all.add(extra);
        return new IdentifierFormats(all);
    }

    // sin distinguir mayúsculas y sin los espacios de los extremos
    public Optional<IdentifierFormatChecker> find(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return formats.stream()
                .filter(format -> format.spellings().contains(normalized))
                .findFirst();
    }

    // la primera escritura de cada formato, en el orden del registro
    public String supported() {
        return String.join(
                ", ", formats.stream().map(format -> format.spellings().get(0)).toList());
    }
}
