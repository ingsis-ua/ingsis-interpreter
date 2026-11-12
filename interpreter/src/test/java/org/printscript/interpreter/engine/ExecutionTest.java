package org.printscript.interpreter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.printscript.common.Success;
import org.printscript.interpreter.Run;
import org.printscript.interpreter.state.Environment;
import org.printscript.runtime.RuntimePorts;

// commit es la única forma en que una regla deja un cambio de variables: reemplaza el
// entorno y devuelve el éxito de la sentencia
class ExecutionTest {

    private static final RuntimePorts PORTS =
            new RuntimePorts(
                    new org.printscript.runtime.OutputPrinter() {
                        @Override
                        public void println(String line) {
                            // sin salida
                        }

                        @Override
                        public void print(String text) {
                            // sin salida
                        }
                    },
                    java.util.Optional::empty,
                    name -> java.util.Optional.empty());

    @Test
    void commitReplacesTheEnvironmentAndSucceeds() {
        Execution execution = Run.execution(new Environment(), PORTS);
        Environment declared = new Environment().declare("x", "number", 1.0, false).orElseThrow();

        assertInstanceOf(Success.class, execution.commit(declared));
        assertEquals(1.0, execution.environment().read("x").orElseThrow().value());
    }
}
