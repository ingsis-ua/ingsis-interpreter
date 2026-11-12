package org.printscript.formatter.rules;

import java.util.Set;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

// Preguntas sobre la forma de los tokens que comparten las reglas.
public final class TokenShapes {

    private static final Set<String> OPERATIONS = Set.of("+", "-", "*", "/");

    private TokenShapes() {}

    public static boolean sameLine(Token previous, Token current) {
        return previous.range().end().line() == current.range().start().line();
    }

    public static boolean isOperation(Token token) {
        return token.type() == TokenType.OPERATOR && OPERATIONS.contains(token.value());
    }

    public static boolean isOperator(Token token, String text) {
        return token.type() == TokenType.OPERATOR && text.equals(token.value());
    }

    public static boolean isPunctuation(Token token, String text) {
        return token.type() == TokenType.PUNCTUATION && text.equals(token.value());
    }

    public static boolean isKeyword(Token token, String text) {
        return token.type() == TokenType.KEYWORD && text.equals(token.value());
    }
}
