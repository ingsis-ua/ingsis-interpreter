package org.printscript.interpreter;

import java.util.Set;
import java.util.function.Predicate;
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
import org.printscript.common.Result;
import org.printscript.common.Version;
import org.printscript.runtime.RuntimePorts;

// arma ejecuciones para los tests sin pasar por el pipeline entero
final class Run {

    private Run() {}

    static Execution execution(Environment environment, RuntimePorts ports) {
        return execution(InterpreterFactory.rules(Version.V11), environment, ports);
    }

    static Execution execution(NodeRules rules, Environment environment, RuntimePorts ports) {
        return new Execution(
                environment, ports, new Executor(rules), new ExpressionEvaluator(rules));
    }

    static Result<Void> statement(Statement statement, RuntimePorts ports) {
        return execution(new Environment(), ports).execute(statement);
    }

    static Result<Object> expression(
            Expression expression, Environment environment, RuntimePorts ports) {
        return execution(environment, ports).evaluate(expression);
    }

    // todas las reglas de 1.1 salvo las de estos nodos
    static NodeRules rulesWithout(Class<?>... excluded) {
        Set<Class<?>> skipped = Set.of(excluded);
        return rules(type -> !skipped.contains(type));
    }

    // solo las reglas de estos nodos
    static NodeRules rulesOnly(Class<?>... kept) {
        Set<Class<?>> included = Set.of(kept);
        return rules(included::contains);
    }

    private static NodeRules rules(Predicate<Class<?>> keep) {
        NodeRules.Builder builder = NodeRules.builder();
        statement(builder, keep, VariableDeclaration.class, new VariableDeclarationRule());
        statement(builder, keep, ConstDeclaration.class, new ConstDeclarationRule());
        statement(builder, keep, AssignmentStatement.class, new AssignmentRule());
        statement(builder, keep, PrintlnStatement.class, new PrintlnRule());
        statement(builder, keep, Block.class, new BlockRule());
        statement(builder, keep, IfStatement.class, new IfRule());
        expression(builder, keep, NumberLiteral.class, new NumberLiteralRule());
        expression(builder, keep, StringLiteral.class, new StringLiteralRule());
        expression(builder, keep, BooleanLiteral.class, new BooleanLiteralRule());
        expression(builder, keep, Identifier.class, new IdentifierRule());
        expression(builder, keep, BinaryExpression.class, new BinaryExpressionRule());
        expression(builder, keep, UnaryExpression.class, new UnaryExpressionRule());
        expression(builder, keep, ReadInput.class, new ReadInputRule());
        expression(builder, keep, ReadEnv.class, new ReadEnvRule());
        return builder.build();
    }

    private static <S extends Statement> void statement(
            NodeRules.Builder builder,
            Predicate<Class<?>> keep,
            Class<S> type,
            StatementRule<S> rule) {
        if (keep.test(type)) {
            builder.statement(type, rule);
        }
    }

    private static <E extends Expression> void expression(
            NodeRules.Builder builder,
            Predicate<Class<?>> keep,
            Class<E> type,
            ExpressionRule<E> rule) {
        if (keep.test(type)) {
            builder.expression(type, rule);
        }
    }
}
