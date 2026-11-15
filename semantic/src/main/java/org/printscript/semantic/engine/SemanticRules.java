package org.printscript.semantic.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.printscript.ast.Expression;
import org.printscript.ast.Statement;

// qué regla chequea cada clase de sentencia y cuál tipa cada clase de expresión
// un nodo sin regla es un nodo que semantic no conoce
public final class SemanticRules {

    private final Map<Class<?>, StatementCheck<?>> checks;
    private final Map<Class<?>, ExpressionTyping<?>> typings;

    private SemanticRules(
            Map<Class<?>, StatementCheck<?>> checks, Map<Class<?>, ExpressionTyping<?>> typings) {
        this.checks = Map.copyOf(checks);
        this.typings = Map.copyOf(typings);
    }

    public static Registry registry() {
        return new Registry();
    }

    public boolean covers(Class<?> node) {
        return checks.containsKey(node) || typings.containsKey(node);
    }

    // el registro solo guarda una regla bajo la clase que ella chequea (ver Registry), así
    // que el cast es seguro; vive solo acá
    @SuppressWarnings("unchecked")
    Optional<StatementCheck<Statement>> checkOf(Statement statement) {
        return Optional.ofNullable((StatementCheck<Statement>) checks.get(statement.getClass()));
    }

    @SuppressWarnings("unchecked")
    Optional<ExpressionTyping<Expression>> typingOf(Expression expression) {
        return Optional.ofNullable(
                (ExpressionTyping<Expression>) typings.get(expression.getClass()));
    }

    public static final class Registry {

        private final Map<Class<?>, StatementCheck<?>> checks = new HashMap<>();
        private final Map<Class<?>, ExpressionTyping<?>> typings = new HashMap<>();

        private Registry() {}

        public <S extends Statement> Registry check(Class<S> sentence, StatementCheck<S> rule) {
            refuseRepeated(sentence);
            checks.put(sentence, rule);
            return this;
        }

        public <E extends Expression> Registry typing(Class<E> value, ExpressionTyping<E> rule) {
            refuseRepeated(value);
            typings.put(value, rule);
            return this;
        }

        public SemanticRules done() {
            return new SemanticRules(checks, typings);
        }

        private void refuseRepeated(Class<?> node) {
            if (checks.containsKey(node) || typings.containsKey(node)) {
                throw new IllegalArgumentException(
                        node.getSimpleName() + " ya tiene una regla semántica");
            }
        }
    }
}
