package org.printscript.ast;

import org.printscript.common.Range;

public record UnaryExpression(Operator operator, Expression operand, Range range)
        implements Expression {

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}
