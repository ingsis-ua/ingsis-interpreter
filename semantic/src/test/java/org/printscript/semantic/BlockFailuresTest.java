package org.printscript.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.printscript.semantic.Ast.at;
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
import org.printscript.ast.Statement;
import org.printscript.common.Failure;
import org.printscript.common.Range;

class BlockFailuresTest {

    private final StatementChecker checker =
            new StatementChecker(Set.of(Type.NUMBER, Type.STRING, Type.BOOLEAN));

    private static Block block(Statement... statements) {
        return new Block(List.of(statements), Ast.ANY);
    }

    private static Statement conditional(Block thenBlock, Statement elseBranch) {
        return new IfStatement(
                Ast.bool(true, Ast.ANY), thenBlock, Optional.ofNullable(elseBranch), Ast.ANY);
    }

    private List<Range> rangesOf(Statement statement) {
        return checker.failuresOf(statement).stream().map(Failure::range).toList();
    }

    @Test
    void everyFailingStatementOfABlockIsReported() {
        Statement program =
                conditional(
                        block(
                                declare("a", "number", text("x", at(1, 29))),
                                declare("b", "string", number(5, at(1, 51))),
                                println(name("zzz", at(1, 62)))),
                        null);

        assertEquals(List.of(at(1, 29), at(1, 51), at(1, 62)), rangesOf(program));
    }

    @Test
    void theFailuresOfTheThenAndOfTheElseAreBothReported() {
        Statement program =
                conditional(
                        block(println(name("uno", at(2, 9)))),
                        block(println(name("dos", at(4, 9)))));

        assertEquals(List.of(at(2, 9), at(4, 9)), rangesOf(program));
    }

    @Test
    void aFailureInANestedBlockIsReportedOnce() {
        Statement program =
                conditional(
                        block(
                                conditional(block(println(name("hondo", at(3, 9)))), null),
                                println(name("afuera", at(5, 9)))),
                        null);

        assertEquals(List.of(at(3, 9), at(5, 9)), rangesOf(program));
    }

    @Test
    void aStatementWithoutBlocksReportsItsSingleFailure() {
        assertEquals(List.of(at(1, 9)), rangesOf(println(name("nada", at(1, 9)))));
    }

    @Test
    void aValidStatementHasNoFailures() {
        assertEquals(List.of(), rangesOf(declare("ok", "number", number(1, Ast.ANY))));
    }

    @Test
    void theScopeOfABlockClosesEvenWhenSeveralOfItsStatementsFail() {
        checker.failuresOf(
                conditional(
                        block(
                                declare("interna", "number", number(1, Ast.ANY)),
                                println(name("uno", Ast.ANY)),
                                println(name("dos", Ast.ANY))),
                        null));

        assertEquals(
                List.of("Variable no declarada: interna"),
                checker.failuresOf(println(name("interna", Ast.ANY))).stream()
                        .map(Failure::message)
                        .toList());
    }
}
