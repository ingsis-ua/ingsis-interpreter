package org.printscript.semantic.rules.statements;

import org.printscript.ast.IfStatement;
import org.printscript.common.Failure;
import org.printscript.common.Result;
import org.printscript.semantic.Type;
import org.printscript.semantic.engine.Checking;
import org.printscript.semantic.engine.StatementCheck;

// la condición tiene que ser boolean, y después se chequean las dos ramas
public final class IfCheck implements StatementCheck<IfStatement> {

    @Override
    public Result<Void> check(IfStatement ifStatement, Checking checking) {
        return checking.typeOf(ifStatement.condition())
                .andThen(type -> conditionAndBranches(ifStatement, type, checking));
    }

    private static Result<Void> conditionAndBranches(
            IfStatement ifStatement, Type type, Checking checking) {
        if (type != Type.BOOLEAN) {
            return new Failure<>(
                    "La condición de un if tiene que ser boolean", ifStatement.condition().range());
        }
        Result<Void> thenChecked = checking.check(ifStatement.thenBlock());
        Result<Void> elseChecked =
                ifStatement.elseBranch().map(checking::check).orElseGet(Declarations::success);
        return thenChecked instanceof Failure<Void> ? thenChecked : elseChecked;
    }
}
