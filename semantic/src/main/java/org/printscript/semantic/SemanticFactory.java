package org.printscript.semantic;

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
import org.printscript.semantic.engine.SemanticRules;
import org.printscript.semantic.rules.expressions.BinaryExpressionTyping;
import org.printscript.semantic.rules.expressions.BooleanLiteralTyping;
import org.printscript.semantic.rules.expressions.IdentifierTyping;
import org.printscript.semantic.rules.expressions.NumberLiteralTyping;
import org.printscript.semantic.rules.expressions.ReadEnvTyping;
import org.printscript.semantic.rules.expressions.ReadInputTyping;
import org.printscript.semantic.rules.expressions.StringLiteralTyping;
import org.printscript.semantic.rules.expressions.UnaryExpressionTyping;
import org.printscript.semantic.rules.statements.AssignmentCheck;
import org.printscript.semantic.rules.statements.BlockCheck;
import org.printscript.semantic.rules.statements.ConstDeclarationCheck;
import org.printscript.semantic.rules.statements.IfCheck;
import org.printscript.semantic.rules.statements.PrintlnCheck;
import org.printscript.semantic.rules.statements.VariableDeclarationCheck;

// acá vive lo que semantic sabe de cada versión, y qué regla chequea cada nodo:
// agregar un nodo es escribir su regla y registrarla en rules(); nada más cambia
public final class SemanticFactory {

    private SemanticFactory() {}

    public static SemanticAnalyzer create(Iterator<Result<Statement>> statements, Version version) {
        return new SemanticAnalyzer(
                statements, new StatementChecker(supportedTypes(version), language(version)));
    }

    // las mismas reglas para 1.0 y 1.1: las versiones solo difieren en los tipos
    public static SemanticRules rules() {
        return SemanticRules.registry()
                .check(VariableDeclaration.class, new VariableDeclarationCheck())
                .check(ConstDeclaration.class, new ConstDeclarationCheck())
                .check(AssignmentStatement.class, new AssignmentCheck())
                .check(PrintlnStatement.class, new PrintlnCheck())
                .check(Block.class, new BlockCheck())
                .check(IfStatement.class, new IfCheck())
                .typing(NumberLiteral.class, new NumberLiteralTyping())
                .typing(StringLiteral.class, new StringLiteralTyping())
                .typing(BooleanLiteral.class, new BooleanLiteralTyping())
                .typing(Identifier.class, new IdentifierTyping())
                .typing(BinaryExpression.class, new BinaryExpressionTyping())
                .typing(UnaryExpression.class, new UnaryExpressionTyping())
                .typing(ReadInput.class, new ReadInputTyping())
                .typing(ReadEnv.class, new ReadEnvTyping())
                .done();
    }

    private static String language(Version version) {
        return switch (version) {
            case V10 -> "PrintScript 1.0";
            case V11 -> "PrintScript 1.1";
        };
    }

    private static Set<Type> supportedTypes(Version version) {
        return switch (version) {
            case V10 -> Set.of(Type.NUMBER, Type.STRING);
            case V11 -> Set.of(Type.NUMBER, Type.STRING, Type.BOOLEAN);
        };
    }
}
