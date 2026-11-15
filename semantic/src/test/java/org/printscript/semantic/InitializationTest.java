package org.printscript.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.printscript.semantic.Ast.assign;
import static org.printscript.semantic.Ast.at;
import static org.printscript.semantic.Ast.binary;
import static org.printscript.semantic.Ast.declare;
import static org.printscript.semantic.Ast.name;
import static org.printscript.semantic.Ast.number;
import static org.printscript.semantic.Ast.println;
import static org.printscript.semantic.Ast.text;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.printscript.ast.Block;
import org.printscript.ast.IfStatement;
import org.printscript.ast.Operator;
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.common.Success;

class InitializationTest {

    private static final String NUMBER = "number";
    private static final String USED_BEFORE = "Variable usada antes de asignarse: x";

    private final StatementChecker checker =
            new StatementChecker(Set.of(Type.NUMBER, Type.STRING, Type.BOOLEAN));

    private Result<Void> check(Statement statement) {
        return checker.check(statement);
    }

    private static Block block(Statement... statements) {
        return new Block(List.of(statements), Ast.ANY);
    }

    private static Statement conditional(Block thenBlock, Block elseBlock) {
        return new IfStatement(
                Ast.bool(true, Ast.ANY), thenBlock, Optional.ofNullable(elseBlock), Ast.ANY);
    }

    @Test
    void usingADeclaredButUnassignedVariableIsReportedAtTheUse() {
        check(declare("x", NUMBER, null));

        Failure<Void> failure =
                assertInstanceOf(Failure.class, check(println(name("x", at(2, 9)))));

        assertEquals(USED_BEFORE, failure.message());
        assertEquals(at(2, 9), failure.range());
    }

    @Test
    void usingItInsideAnExpressionIsAlsoReported() {
        check(declare("x", NUMBER, null));

        Failure<Void> failure =
                assertInstanceOf(
                        Failure.class,
                        check(
                                println(
                                        binary(
                                                name("x", at(2, 9)),
                                                Operator.PLUS,
                                                text("a", Ast.ANY)))));

        assertEquals(USED_BEFORE, failure.message());
    }

    @Test
    void anAssignmentInitializesTheVariable() {
        check(declare("x", NUMBER, null));
        check(assign("x", Ast.ANY, number(3, Ast.ANY)));

        assertInstanceOf(Success.class, check(println(name("x", Ast.ANY))));
    }

    @Test
    void anAssignmentCanUseTheVariableOnlyAfterItHasAValue() {
        check(declare("x", NUMBER, null));

        Failure<Void> failure =
                assertInstanceOf(
                        Failure.class,
                        check(
                                assign(
                                        "x",
                                        Ast.ANY,
                                        binary(
                                                name("x", at(2, 5)),
                                                Operator.PLUS,
                                                number(1, Ast.ANY)))));

        assertEquals(USED_BEFORE, failure.message());
    }

    @Test
    void insideTheBlockThatAssignsItTheVariableCanBeUsed() {
        check(declare("x", NUMBER, null));

        Result<Void> result =
                check(
                        conditional(
                                block(
                                        assign("x", Ast.ANY, number(1, Ast.ANY)),
                                        println(name("x", Ast.ANY))),
                                null));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void anAssignmentInsideABlockDoesNotInitializeTheVariableOutsideIt() {
        check(declare("x", NUMBER, null));
        check(
                conditional(
                        block(assign("x", Ast.ANY, number(1, Ast.ANY))),
                        block(assign("x", Ast.ANY, number(2, Ast.ANY)))));

        Failure<Void> failure =
                assertInstanceOf(Failure.class, check(println(name("x", at(5, 9)))));

        assertEquals(USED_BEFORE, failure.message());
    }

    @Test
    void aVariableDeclaredAndAssignedInsideTheSameBlockIsInitialized() {
        Result<Void> result =
                check(
                        conditional(
                                block(
                                        declare("x", NUMBER, null),
                                        assign("x", Ast.ANY, number(1, Ast.ANY)),
                                        println(name("x", Ast.ANY))),
                                null));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void shadowingAfterAnAssignmentInTheSameBlockIsStillAllowed() {
        check(declare("x", NUMBER, null));

        Result<Void> result =
                check(
                        conditional(
                                block(
                                        assign("x", Ast.ANY, number(1, Ast.ANY)),
                                        declare("x", "string", text("sombra", Ast.ANY))),
                                null));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void aDeclarationWithAWrongInitializerCountsAsInitializedSoErrorsDoNotCascade() {
        check(declare("x", NUMBER, text("no es un número", Ast.ANY)));

        assertInstanceOf(Success.class, check(println(name("x", Ast.ANY))));
    }
}
