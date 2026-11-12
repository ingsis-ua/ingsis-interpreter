package org.printscript.interpreter.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Range;

// qué regla atiende a cada clase de nodo. Lo arma InterpreterFactory por versión: un
// nodo sin regla es un nodo que esa versión no soporta
public final class NodeRules {

    private final Map<Class<?>, StatementRule<?>> statements;
    private final Map<Class<?>, ExpressionRule<?>> expressions;

    private NodeRules(
            Map<Class<?>, StatementRule<?>> statements,
            Map<Class<?>, ExpressionRule<?>> expressions) {
        this.statements = Map.copyOf(statements);
        this.expressions = Map.copyOf(expressions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean supports(Class<?> type) {
        return statements.containsKey(type) || expressions.containsKey(type);
    }

    // el registro solo acepta reglas cuyo tipo coincide con la clave (ver Builder), así
    // que el cast es seguro; vive acá para que ningún otro lugar tenga que hacerlo
    @SuppressWarnings("unchecked")
    public Optional<StatementRule<Statement>> statementRuleFor(Statement statement) {
        return Optional.ofNullable((StatementRule<Statement>) statements.get(statement.getClass()));
    }

    @SuppressWarnings("unchecked")
    public Optional<ExpressionRule<Expression>> expressionRuleFor(Expression expression) {
        return Optional.ofNullable(
                (ExpressionRule<Expression>) expressions.get(expression.getClass()));
    }

    public List<Object> childrenOf(Object node) {
        return switch (node) {
            case Statement statement ->
                    statementRuleFor(statement)
                            .map(rule -> rule.children(statement))
                            .orElseGet(List::of);
            case Expression expression ->
                    expressionRuleFor(expression)
                            .map(rule -> rule.children(expression))
                            .orElseGet(List::of);
            default -> List.of();
        };
    }

    // el mismo registro con solo las reglas de los nodos que pasan el filtro
    public NodeRules only(Predicate<Class<?>> keep) {
        return new NodeRules(filtered(statements, keep), filtered(expressions, keep));
    }

    private static <V> Map<Class<?>, V> filtered(Map<Class<?>, V> rules, Predicate<Class<?>> keep) {
        Map<Class<?>, V> kept = new HashMap<>();
        rules.forEach(
                (type, rule) -> {
                    if (keep.test(type)) {
                        kept.put(type, rule);
                    }
                });
        return kept;
    }

    public static <T> Failure<T> unsupported(Object node, Range range) {
        return new Failure<>(
                "Nodo " + node.getClass().getSimpleName() + " no soportado por PrintScript", range);
    }

    public static final class Builder {

        private final Map<Class<?>, StatementRule<?>> statements = new HashMap<>();
        private final Map<Class<?>, ExpressionRule<?>> expressions = new HashMap<>();

        private Builder() {}

        public <S extends Statement> Builder statement(Class<S> type, StatementRule<S> rule) {
            requireNew(type);
            statements.put(type, rule);
            return this;
        }

        public <E extends Expression> Builder expression(Class<E> type, ExpressionRule<E> rule) {
            requireNew(type);
            expressions.put(type, rule);
            return this;
        }

        public NodeRules build() {
            return new NodeRules(statements, expressions);
        }

        private void requireNew(Class<?> type) {
            if (statements.containsKey(type) || expressions.containsKey(type)) {
                throw new IllegalArgumentException("Ya hay una regla para " + type.getSimpleName());
            }
        }
    }
}
