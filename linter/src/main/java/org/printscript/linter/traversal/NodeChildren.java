package org.printscript.linter.traversal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
import org.printscript.ast.StringLiteral;
import org.printscript.ast.UnaryExpression;
import org.printscript.ast.VariableDeclaration;

// qué nodos tiene adentro cada clase de nodo, en el orden en que se recorren
// es lo único del linter que conoce la forma del árbol: agregar un nodo es sumarle una
// línea a standard(), y ni el buscador ni los collectors cambian
public final class NodeChildren {

    private final Map<Class<?>, Function<Object, List<Object>>> children;

    private NodeChildren(Map<Class<?>, Function<Object, List<Object>>> children) {
        this.children = Map.copyOf(children);
    }

    public static NodeChildren none() {
        return new NodeChildren(Map.of());
    }

    public static NodeChildren standard() {
        return none().with(NumberLiteral.class, leaf())
                .with(StringLiteral.class, leaf())
                .with(BooleanLiteral.class, leaf())
                .with(Identifier.class, leaf())
                .with(BinaryExpression.class, e -> List.of(e.left(), e.right()))
                .with(UnaryExpression.class, e -> List.of(e.operand()))
                .with(ReadInput.class, e -> List.of(e.prompt()))
                .with(ReadEnv.class, e -> List.of(e.name()))
                .with(VariableDeclaration.class, s -> then(s.name(), s.initializer()))
                .with(ConstDeclaration.class, s -> then(s.name(), s.initializer()))
                .with(AssignmentStatement.class, s -> List.of(s.name(), s.expression()))
                .with(PrintlnStatement.class, s -> List.of(s.expression()))
                .with(Block.class, s -> List.copyOf(s.statements()))
                .with(IfStatement.class, s -> then(s.condition(), s.thenBlock(), s.elseBranch()));
    }

    // el mismo registro con una clase más; una clase repetida es un error de armado
    public <T> NodeChildren with(Class<T> type, Function<T, List<Object>> of) {
        if (children.containsKey(type)) {
            throw new IllegalArgumentException("Ya están los hijos de " + type.getSimpleName());
        }
        Map<Class<?>, Function<Object, List<Object>>> extended = new HashMap<>(children);
        extended.put(type, node -> of.apply(type.cast(node)));
        return new NodeChildren(extended);
    }

    public boolean knows(Class<?> type) {
        return children.containsKey(type);
    }

    // un nodo que el registro no conoce se trata como hoja
    public List<Object> of(Object node) {
        Function<Object, List<Object>> of = children.get(node.getClass());
        return of == null ? List.of() : of.apply(node);
    }

    private static <T> Function<T, List<Object>> leaf() {
        return node -> List.of();
    }

    private static List<Object> then(Object first, Optional<?> optional) {
        return optional.<List<Object>>map(value -> List.of(first, value))
                .orElseGet(() -> List.of(first));
    }

    private static List<Object> then(Object first, Object second, Optional<?> optional) {
        return optional.<List<Object>>map(value -> List.of(first, second, value))
                .orElseGet(() -> List.of(first, second));
    }
}
