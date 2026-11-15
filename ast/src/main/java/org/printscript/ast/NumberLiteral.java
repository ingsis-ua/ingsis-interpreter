package org.printscript.ast;

import org.printscript.common.Range;

// numero escrito directo en el codigo. Guarda un double
public record NumberLiteral(double value, Range range) implements Expression {}
