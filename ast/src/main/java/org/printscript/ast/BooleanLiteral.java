package org.printscript.ast;

import org.printscript.common.Range;

// true o false escrito directo en el codigo
public record BooleanLiteral(boolean value, Range range) implements Expression {}
