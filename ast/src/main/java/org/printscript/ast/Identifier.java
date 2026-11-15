package org.printscript.ast;

import org.printscript.common.Range;

// uso de un nombre de variable dentro de una expresión
public record Identifier(String name, Range range) implements Expression {}
