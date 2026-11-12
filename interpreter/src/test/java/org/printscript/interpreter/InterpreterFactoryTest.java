package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.ConstDeclaration;
import org.printscript.ast.Expression;
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
import org.printscript.common.Version;

// con el visitor, olvidarse de ejecutar un nodo nuevo era un error de compilación; con el
// registro sería una falla en ejecución. Este test lo vuelve a atrapar antes: un nodo
// que el ast agrega sin su regla en la última versión rompe acá
class InterpreterFactoryTest {

    private static final Version LATEST = Version.values()[Version.values().length - 1];

    @Test
    void latestVersionHasARuleForEveryNode() {
        NodeRules rules = InterpreterFactory.rules(LATEST);

        Stream.concat(
                        Stream.of(Statement.class.getPermittedSubclasses()),
                        Stream.of(Expression.class.getPermittedSubclasses()))
                .forEach(type -> assertTrue(rules.supports(type), type.getSimpleName()));
    }

    @Test
    void version10SupportsExactlyItsNodes() {
        NodeRules rules = InterpreterFactory.rules(Version.V10);

        List.of(
                        VariableDeclaration.class,
                        AssignmentStatement.class,
                        PrintlnStatement.class,
                        NumberLiteral.class,
                        StringLiteral.class,
                        Identifier.class,
                        BinaryExpression.class,
                        UnaryExpression.class)
                .forEach(type -> assertTrue(rules.supports(type), type.getSimpleName()));
        List.of(
                        ConstDeclaration.class,
                        IfStatement.class,
                        Block.class,
                        BooleanLiteral.class,
                        ReadInput.class,
                        ReadEnv.class)
                .forEach(type -> assertFalse(rules.supports(type), type.getSimpleName()));
    }
}
