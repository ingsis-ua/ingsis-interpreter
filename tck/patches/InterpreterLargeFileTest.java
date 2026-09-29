package interpreter;

import implementation.CustomImplementationFactory;
import java.util.Objects;
import java.util.function.IntSupplier;
import org.junit.Test;
import util.ErrorCollector;
import util.MockInputStream;
import util.PrintCollector;
import util.PrintCounter;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Streaming checks for the classroom TCK. A collector may or may not exhaust a 7 MB
 * heap depending on the JVM and the representation of strings. Successful collection
 * is valid only if all output is present; bounded read ahead checks streaming directly.
 */
public class InterpreterLargeFileTest {

    private static final String MESSAGE = "This is a text";
    private static final String LINE = "println(\"" + MESSAGE + "\");\n";
    private static final int NUMBER_OF_LINES = 32 * 1024;
    private static final int MAX_READ_AHEAD_LINES = 1024;

    private final PrintScriptInterpreter interpreter = new CustomImplementationFactory().interpreter();

    @Test
    public void testWithCounter() {
        PrintCounter counter = new PrintCounter(message -> Objects.equals(message, MESSAGE));
        ErrorCollector errors = new ErrorCollector();

        interpreter.execute(new MockInputStream(LINE, NUMBER_OF_LINES), "1.0", counter, errors, ignored -> "");

        assertThat(errors.getErrors(), is(emptyList()));
        assertThat(counter.getCount(), is(NUMBER_OF_LINES));
    }

    @Test
    public void testWithCollector() {
        PrintCollector collector = new PrintCollector();
        ErrorCollector errors = new ErrorCollector();

        interpreter.execute(new MockInputStream(LINE, NUMBER_OF_LINES), "1.0", collector, errors, ignored -> "");

        if (errors.getErrors().isEmpty()) {
            assertThat(collector.getMessages().size(), is(NUMBER_OF_LINES));
            assertThat(collector.getMessages().stream().allMatch(MESSAGE::equals), is(true));
        } else {
            assertThat(errors.getErrors(), is(singletonList("Java heap space")));
        }
    }

    @Test
    public void testDoesNotReadTheWholeProgramBeforeProducingOutput() {
        PrintCounter counter = new PrintCounter(message -> Objects.equals(message, MESSAGE));
        ErrorCollector errors = new ErrorCollector();
        MockInputStream input = new BoundedReadAheadInputStream(counter::getCount);

        interpreter.execute(input, "1.0", counter, errors, ignored -> "");

        assertThat(errors.getErrors(), is(emptyList()));
        assertThat(counter.getCount(), is(NUMBER_OF_LINES));
    }

    private static final class BoundedReadAheadInputStream extends MockInputStream {

        private final IntSupplier emittedCount;

        private BoundedReadAheadInputStream(IntSupplier emittedCount) {
            super(LINE, NUMBER_OF_LINES);
            this.emittedCount = emittedCount;
        }

        @Override
        public int read() {
            if (lineNumber - emittedCount.getAsInt() > MAX_READ_AHEAD_LINES) {
                throw new AssertionError("The interpreter read more than 1024 lines ahead of output");
            }
            return super.read();
        }
    }
}
