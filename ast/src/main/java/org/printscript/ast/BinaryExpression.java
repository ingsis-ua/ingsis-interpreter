package org.printscript.ast;

import org.printscript.common.Range;

// una cuenta, lo de la izquierda, el operador y lo de la derecha (2+2)
public record BinaryExpression(Expression left, Operator operator, Expression right, Range range)
        implements Expression {}
