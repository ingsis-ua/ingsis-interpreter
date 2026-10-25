package org.printscript.interpreter;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.printscript.ast.ASTVisitor;
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
import org.printscript.common.Failure;
import org.printscript.common.Range;

final class NodeGate implements ASTVisitor<Optional<Failure<Void>>> {

    private final Set<Class<?>> supported;

    NodeGate(Set<Class<?>> supported) {
        this.supported = Set.copyOf(supported);
    }

    Optional<Failure<Void>> check(Statement statement) {
        return statement.accept(this);
    }

    @Override
    public Optional<Failure<Void>> visitNumberLiteral(NumberLiteral numberLiteral) {
        return node(numberLiteral, numberLiteral.range());
    }

    @Override
    public Optional<Failure<Void>> visitStringLiteral(StringLiteral stringLiteral) {
        return node(stringLiteral, stringLiteral.range());
    }

    @Override
    public Optional<Failure<Void>> visitBooleanLiteral(BooleanLiteral booleanLiteral) {
        return node(booleanLiteral, booleanLiteral.range());
    }

    @Override
    public Optional<Failure<Void>> visitIdentifier(Identifier identifier) {
        return node(identifier, identifier.range());
    }

    @Override
    public Optional<Failure<Void>> visitBinaryExpression(BinaryExpression binaryExpression) {
        return node(
                binaryExpression,
                binaryExpression.range(),
                () -> binaryExpression.left().accept(this),
                () -> binaryExpression.right().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitUnaryExpression(UnaryExpression unaryExpression) {
        return node(
                unaryExpression,
                unaryExpression.range(),
                () -> unaryExpression.operand().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitReadInput(ReadInput readInput) {
        return node(readInput, readInput.range(), () -> readInput.prompt().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitReadEnv(ReadEnv readEnv) {
        return node(readEnv, readEnv.range(), () -> readEnv.name().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitVariableDeclaration(VariableDeclaration declaration) {
        return node(declaration, declaration.range(), () -> inside(declaration.initializer()));
    }

    @Override
    public Optional<Failure<Void>> visitConstDeclaration(ConstDeclaration declaration) {
        return node(declaration, declaration.range(), () -> inside(declaration.initializer()));
    }

    @Override
    public Optional<Failure<Void>> visitAssignmentStatement(AssignmentStatement assignment) {
        return node(assignment, assignment.range(), () -> assignment.expression().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitPrintlnStatement(PrintlnStatement println) {
        return node(println, println.range(), () -> println.expression().accept(this));
    }

    @Override
    public Optional<Failure<Void>> visitBlock(Block block) {
        return node(
                block,
                block.range(),
                () ->
                        block.statements().stream()
                                .map(statement -> statement.accept(this))
                                .flatMap(Optional::stream)
                                .findFirst());
    }

    @Override
    public Optional<Failure<Void>> visitIfStatement(IfStatement ifStatement) {
        return node(
                ifStatement,
                ifStatement.range(),
                () -> ifStatement.condition().accept(this),
                () -> ifStatement.thenBlock().accept(this),
                () -> ifStatement.elseBranch().flatMap(branch -> branch.accept(this)));
    }

    private Optional<Failure<Void>> inside(Optional<Expression> expression) {
        return expression.flatMap(inner -> inner.accept(this));
    }

    @SafeVarargs
    private Optional<Failure<Void>> node(
            Object node, Range range, Supplier<Optional<Failure<Void>>>... children) {
        return rejected(node, range)
                .or(
                        () ->
                                Stream.of(children)
                                        .map(Supplier::get)
                                        .flatMap(Optional::stream)
                                        .findFirst());
    }

    private Optional<Failure<Void>> rejected(Object node, Range range) {
        if (supported.contains(node.getClass())) {
            return Optional.empty();
        }
        return Optional.of(
                new Failure<>(
                        "Nodo " + node.getClass().getSimpleName() + " no soportado por PrintScript",
                        range));
    }
}
