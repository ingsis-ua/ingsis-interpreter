package org.printscript.interpreter;

import java.util.Iterator;
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

// acá vive lo que el intérprete sabe de cada versión: qué regla ejecuta cada nodo
// agregar un nodo es escribir su regla y registrarla acá; el Executor no cambia
public final class InterpreterFactory {

    private InterpreterFactory() {}

    public static Interpreter create(
            Iterator<Result<Statement>> statements, RuntimePorts ports, Version version) {
        return new Interpreter(statements, rules(version), ports);
    }

    static NodeRules rules(Version version) {
        return switch (version) {
            case V10 -> version10Rules().build();
            case V11 ->
                    version10Rules()
                            .statement(ConstDeclaration.class, new ConstDeclarationRule())
                            .statement(IfStatement.class, new IfRule())
                            .statement(Block.class, new BlockRule())
                            .expression(BooleanLiteral.class, new BooleanLiteralRule())
                            .expression(ReadInput.class, new ReadInputRule())
                            .expression(ReadEnv.class, new ReadEnvRule())
                            .build();
        };
    }

    private static NodeRules.Builder version10Rules() {
        return NodeRules.builder()
                .statement(VariableDeclaration.class, new VariableDeclarationRule())
                .statement(AssignmentStatement.class, new AssignmentRule())
                .statement(PrintlnStatement.class, new PrintlnRule())
                .expression(NumberLiteral.class, new NumberLiteralRule())
                .expression(StringLiteral.class, new StringLiteralRule())
                .expression(Identifier.class, new IdentifierRule())
                .expression(BinaryExpression.class, new BinaryExpressionRule())
                .expression(UnaryExpression.class, new UnaryExpressionRule());
    }
}
