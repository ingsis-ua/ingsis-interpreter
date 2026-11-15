package org.printscript.ast;

import org.printscript.common.Range;

// un println(...) con la expresion que va adentro
public record PrintlnStatement(Expression expression, Range range) implements Statement {}
