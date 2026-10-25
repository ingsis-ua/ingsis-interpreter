package org.printscript.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.printscript.semantic.Ast.at;
import static org.printscript.semantic.Ast.number;
import static org.printscript.semantic.Ast.text;

import org.junit.jupiter.api.Test;
import org.printscript.ast.Expression;
import org.printscript.ast.Operator;
import org.printscript.ast.UnaryExpression;
import org.printscript.common.Failure;
import org.printscript.common.Success;

class NegationTypingTest {

    private static UnaryExpression negate(Expression operand) {
        return new UnaryExpression(Operator.MINUS, operand, at(1, 1));
    }

    @Test
    void negatingANumberIsANumber() {
        assertEquals(
                new Success<>(Type.NUMBER),
                ExpressionTyper.typeOf(negate(number(5, Ast.ANY)), new SymbolTable()));
    }

    @Test
    void negatingAStringIsATypeError() {
        assertEquals(
                new Failure<Type>("No se puede aplicar MINUS a string", at(1, 1)),
                ExpressionTyper.typeOf(negate(text("hola", Ast.ANY)), new SymbolTable()));
    }

    @Test
    void negatingABooleanIsATypeError() {
        assertEquals(
                new Failure<Type>("No se puede aplicar MINUS a boolean", at(1, 1)),
                ExpressionTyper.typeOf(negate(Ast.bool(true, Ast.ANY)), new SymbolTable()));
    }
}
