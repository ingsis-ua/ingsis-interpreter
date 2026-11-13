package org.printscript.linter;

import java.util.Iterator;
import org.printscript.ast.Statement;
import org.printscript.common.Result;
import org.printscript.linter.config.LinterConfiguration;

// arma el linter con las reglas que pide la configuración
public final class LinterFactory {

    private LinterFactory() {}

    public static Linter create(
            Iterator<Result<Statement>> statements, LinterConfiguration configuration) {
        return new Linter(statements, configuration.rules());
    }
}
