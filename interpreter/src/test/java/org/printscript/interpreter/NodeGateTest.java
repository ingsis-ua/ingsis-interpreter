package org.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.ConstDeclaration;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Operator;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.ReadEnv;
import org.printscript.ast.ReadInput;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.TypeName;
import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Failure;
import org.printscript.common.Position;
import org.printscript.common.Range;

class NodeGateTest {

    private static final Range ANY = new Range(new Position(1, 1), new Position(1, 1));

    private static final Set<Class<?>> VERSION_10 =
            Set.of(
                    VariableDeclaration.class,
                    AssignmentStatement.class,
                    PrintlnStatement.class,
                    NumberLiteral.class,
                    StringLiteral.class,
                    Identifier.class,
                    BinaryExpression.class);

    private static final Set<Class<?>> VERSION_11 =
            Set.of(
                    VariableDeclaration.class,
                    AssignmentStatement.class,
                    PrintlnStatement.class,
                    NumberLiteral.class,
                    StringLiteral.class,
                    Identifier.class,
                    BinaryExpression.class,
                    ConstDeclaration.class,
                    IfStatement.class,
                    Block.class,
                    BooleanLiteral.class,
                    ReadInput.class,
                    ReadEnv.class);

    private static Range at(int line) {
        return new Range(new Position(line, 1), new Position(line, 9));
    }

    private static Statement everyNode() {
        Block then =
                new Block(
                        List.of(
                                new AssignmentStatement(
                                        new Identifier("x", ANY),
                                        new BinaryExpression(
                                                new NumberLiteral(1, ANY),
                                                Operator.PLUS,
                                                new Identifier("x", ANY),
                                                ANY),
                                        ANY),
                                new ConstDeclaration(
                                        new Identifier("c", ANY),
                                        new TypeName("string", ANY),
                                        Optional.of(
                                                new ReadInput(new StringLiteral("?", ANY), ANY)),
                                        ANY)),
                        ANY);
        Block otherwise =
                new Block(
                        List.of(
                                new PrintlnStatement(
                                        new ReadEnv(new StringLiteral("HOME", ANY), at(7)), ANY),
                                new VariableDeclaration(
                                        new Identifier("v", ANY),
                                        new TypeName("number", ANY),
                                        Optional.empty(),
                                        ANY)),
                        ANY);
        return new IfStatement(new BooleanLiteral(true, at(3)), then, Optional.of(otherwise), ANY);
    }

    @Test
    void aVersionThatSupportsEveryNodeLetsTheWholeTreeThrough() {
        assertEquals(Optional.empty(), new NodeGate(VERSION_11).check(everyNode()));
    }

    @Test
    void theOutermostUnsupportedNodeIsTheOneReported() {
        Failure<Void> failure = new NodeGate(VERSION_10).check(everyNode()).orElseThrow();

        assertTrue(failure.message().contains("IfStatement"), failure.message());
    }

    @Test
    void anUnsupportedNodeDeepInsideABlockIsFoundWithItsRange() {
        Set<Class<?>> withoutReadEnv =
                VERSION_11.stream()
                        .filter(node -> node != ReadEnv.class)
                        .collect(Collectors.toSet());

        Failure<Void> failure = new NodeGate(withoutReadEnv).check(everyNode()).orElseThrow();

        assertTrue(failure.message().contains("ReadEnv"), failure.message());
        assertEquals(at(7), failure.range());
    }

    @Test
    void theConditionIsCheckedBeforeTheBlocks() {
        Set<Class<?>> withoutBooleans =
                VERSION_11.stream()
                        .filter(node -> node != BooleanLiteral.class)
                        .collect(Collectors.toSet());

        Failure<Void> failure = new NodeGate(withoutBooleans).check(everyNode()).orElseThrow();

        assertEquals(at(3), failure.range());
    }
}
