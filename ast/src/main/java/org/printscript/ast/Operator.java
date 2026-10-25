package org.printscript.ast;

public enum Operator {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/");

    private final String written;

    Operator(String symbol) {
        this.written = symbol;
    }

    public String symbol() {
        return written;
    }
}
