package org.printscript.ast;

import org.printscript.common.Range;

public record UnaryExpression(Operator operator, Expression operand, Range range)
        implements Expression {}
