package org.printscript.interpreter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.ConstDeclaration;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.ReadEnv;
import org.printscript.ast.ReadInput;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.UnaryExpression;
import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Result;
import org.printscript.common.Version;
import org.printscript.runtime.RuntimePorts;

// acá vive lo que el intérprete sabe de cada versión
public final class InterpreterFactory {

    private InterpreterFactory() {}

    public static Interpreter create(
            Iterator<Result<Statement>> statements, RuntimePorts ports, Version version) {
        Executor executor = new Executor(new Environment(), ports);
        return new Interpreter(statements, executor, supportedNodes(version));
    }

    private static Set<Class<?>> union(Set<Class<?>> base, Class<?>... extra) {
        Set<Class<?>> all = new HashSet<>(base);
        all.addAll(Set.of(extra));
        return Set.copyOf(all);
    }

    private static Set<Class<?>> supportedNodes(Version version) {
        Set<Class<?>> common =
                Set.of(
                        VariableDeclaration.class,
                        AssignmentStatement.class,
                        PrintlnStatement.class,
                        NumberLiteral.class,
                        StringLiteral.class,
                        Identifier.class,
                        BinaryExpression.class,
                        UnaryExpression.class);
        return switch (version) {
            case V10 -> common;
            case V11 ->
                    union(
                            common,
                            ConstDeclaration.class,
                            IfStatement.class,
                            Block.class,
                            BooleanLiteral.class,
                            ReadInput.class,
                            ReadEnv.class);
        };
    }
}
