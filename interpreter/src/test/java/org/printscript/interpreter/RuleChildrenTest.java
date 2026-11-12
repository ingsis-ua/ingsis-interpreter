package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.printscript.ast.Block;
import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Operator;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.Statement;
import org.printscript.ast.TypeName;
import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Position;
import org.printscript.common.Range;
import org.printscript.common.Version;
import org.printscript.interpreter.engine.NodeRules;

// el chequeo de nodos soportados recorre el árbol con los hijos que declara cada regla.
// Si una regla se olvida un hijo, un nodo no soportado se escaparía del chequeo y la
// sentencia fallaría a mitad de camino, con efectos ya hechos. Este test compara, para
// cada nodo, los hijos de su regla con la forma real del record en el ast
class RuleChildrenTest {

    private static final Range RANGE = new Range(new Position(1, 1), new Position(1, 4));
    private static final Version LATEST = Version.values()[Version.values().length - 1];

    private final NodeRules rules = InterpreterFactory.rules(LATEST);
    private int samples;

    @Test
    void everyRuleListsExactlyTheChildNodesOfItsNode() {
        Stream.concat(
                        Stream.of(Statement.class.getPermittedSubclasses()),
                        Stream.of(Expression.class.getPermittedSubclasses()))
                .forEach(
                        type -> {
                            Object node = sample(type);
                            assertEquals(
                                    childNodesOf(node),
                                    rules.childrenOf(node),
                                    type.getSimpleName());
                        });
    }

    @Test
    void anAbsentOptionalChildIsNotListed() {
        Statement declaration =
                new VariableDeclaration(
                        new Identifier("x", RANGE),
                        new TypeName("number", RANGE),
                        Optional.empty(),
                        RANGE);
        Expression condition = new NumberLiteral(1, RANGE);
        Block thenBlock = new Block(List.of(), RANGE);
        Statement withoutElse = new IfStatement(condition, thenBlock, Optional.empty(), RANGE);

        assertEquals(List.of(), rules.childrenOf(declaration));
        assertEquals(List.of(condition, thenBlock), rules.childrenOf(withoutElse));
    }

    // un nodo de muestra con cada hijo distinto, para que un hijo de más, de menos o en
    // otro orden se note en la comparación
    private Object sample(Class<?> type) {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameters =
                Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Object[] arguments = Arrays.stream(components).map(this::valueFor).toArray();
        try {
            return type.getDeclaredConstructor(parameters).newInstance(arguments);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private Object valueFor(RecordComponent component) {
        Class<?> type = component.getType();
        if (type == Optional.class) {
            return Optional.of(element(component));
        }
        if (type == List.class) {
            return List.of(element(component), element(component));
        }
        return leaf(type);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private Object leaf(Class<?> type) {
        if (type == Expression.class) {
            return expression();
        }
        if (type == Statement.class) {
            return statement();
        }
        if (type == Block.class) {
            return new Block(List.of(statement()), RANGE);
        }
        if (type == Identifier.class) {
            return new Identifier("nombre", RANGE);
        }
        if (type == TypeName.class) {
            return new TypeName("number", RANGE);
        }
        if (type == Operator.class) {
            return Operator.PLUS;
        }
        if (type == Range.class) {
            return RANGE;
        }
        if (type == String.class) {
            return "texto";
        }
        if (type == double.class) {
            return 1.0;
        }
        if (type == boolean.class) {
            return true;
        }
        throw new IllegalStateException("Componente sin muestra: " + type.getSimpleName());
    }

    private Object element(RecordComponent component) {
        ParameterizedType generic = (ParameterizedType) component.getGenericType();
        return leaf((Class<?>) generic.getActualTypeArguments()[0]);
    }

    private Expression expression() {
        samples = samples + 1;
        return new NumberLiteral(samples, RANGE);
    }

    private Statement statement() {
        return new PrintlnStatement(expression(), RANGE);
    }

    // los hijos son los componentes que son nodos, en orden. El nombre de una declaración
    // o de una asignación está declarado como Identifier: es lo que se declara, no un
    // hijo que se ejecute, y el chequeo nunca lo recorrió
    private static List<Object> childNodesOf(Object node) {
        List<Object> children = new ArrayList<>();
        for (RecordComponent component : node.getClass().getRecordComponents()) {
            if (component.getType() == Identifier.class) {
                continue;
            }
            Object value = valueOf(component, node);
            if (value instanceof Optional<?> optional) {
                optional.ifPresent(children::add);
            } else if (value instanceof List<?> list) {
                children.addAll(list);
            } else if (value instanceof Statement || value instanceof Expression) {
                children.add(value);
            }
        }
        return children;
    }

    private static Object valueOf(RecordComponent component, Object node) {
        try {
            return component.getAccessor().invoke(node);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
