package org.printscript.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Version;
import org.printscript.lexer.constructors.TokenConstructor;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

class LongLineTest {

    private static final class RecordingConstructor implements TokenConstructor {

        private final List<String> lines = new ArrayList<>();
        private final List<Integer> offsets = new ArrayList<>();

        @Override
        public Optional<Token> tryBuild(String rest, Position start) {
            throw new AssertionError("el lexer no tiene que copiar el resto de la línea");
        }

        @Override
        public Optional<Token> tryBuild(String line, int from, Position start) {
            lines.add(line);
            offsets.add(from);
            Position end = new Position(start.line(), start.column());
            return Optional.of(
                    new Token(
                            TokenType.IDENTIFIER,
                            line.substring(from, from + 1),
                            new Range(start, end)));
        }
    }

    @Test
    void everyConstructorReceivesTheWholeLineAndAnOffsetInsteadOfACopy() {
        String line = "ab c";
        RecordingConstructor constructor = new RecordingConstructor();
        Lexer lexer = new Lexer(List.of(line).iterator(), List.of(constructor));

        while (lexer.hasNext()) {
            lexer.next();
        }

        assertEquals(List.of(0, 1, 3), constructor.offsets);
        constructor.lines.forEach(received -> assertSame(line, received));
    }

    @Test
    void aVeryLongLineIsTokenizedInLinearTime() {
        String line = "println(1);".repeat(160_000);

        int tokens =
                assertTimeoutPreemptively(
                        Duration.ofSeconds(3),
                        () -> {
                            Lexer lexer =
                                    LexerFactory.create(List.of(line).iterator(), Version.V10);
                            int count = 0;
                            while (lexer.hasNext()) {
                                lexer.next();
                                count = count + 1;
                            }
                            return count;
                        });

        assertEquals(5 * 160_000, tokens);
    }
}
