package org.printscript.interpreter;

import java.util.Optional;
import org.printscript.ast.ASTVisitor;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.Identifier;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.PrintlnStatement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VariableDeclaration;
import org.printscript.common.Failure;
import org.printscript.common.Range;
import org.printscript.common.Result;
import org.printscript.common.Success;
import org.printscript.runtime.OutputPrinter;

// ejecuta las sentencias
public final class Executor implements ASTVisitor<Result<Void>> {

    private final OutputPrinter printer;
    private Environment environment;

    public Executor(Environment environment, OutputPrinter printer) {
        this.environment = environment;
        this.printer = printer;
    }

    @Override
    public Result<Void> visitVariableDeclaration(VariableDeclaration variableDeclaration) {
        Optional<Expression> initializer = variableDeclaration.initializer();
        if (initializer.isEmpty()) {
            return declare(variableDeclaration, null);
        }
        Result<Object> evaluated = evaluate(initializer.get());
        if (evaluated instanceof Failure<Object> failure) {
            return propagate(failure);
        }
        Object value = valueOf(evaluated);
        Optional<String> typeError = typeMismatch(variableDeclaration.type().name(), value);
        if (typeError.isPresent()) {
            return new Failure<>(typeError.get(), variableDeclaration.range());
        }
        return declare(variableDeclaration, value);
    }

    // el tipo declarado tiene que coincidir con el valor calculado; tipos que no
    // conocemos no los frenamos acá (los rechaza antes el lexer/parser).
    private static Optional<String> typeMismatch(String declaredType, Object value) {
        String actual = runtimeType(value);
        if (declaredType.equals(actual) || !isKnownType(declaredType)) {
            return Optional.empty();
        }
        return Optional.of(
                "Tipo incompatible: se declaró " + declaredType + " pero el valor es " + actual);
    }

    private static String runtimeType(Object value) {
        if (value instanceof Double) {
            return "number";
        }
        if (value instanceof String) {
            return "string";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        return "desconocido";
    }

    private static boolean isKnownType(String declaredType) {
        return "number".equals(declaredType)
                || "string".equals(declaredType)
                || "boolean".equals(declaredType);
    }

    @Override
    public Result<Void> visitAssignmentStatement(AssignmentStatement assignmentStatement) {
        Result<Object> evaluated = evaluate(assignmentStatement.expression());
        if (evaluated instanceof Failure<Object> failure) {
            return propagate(failure);
        }
        return environment
                .assign(assignmentStatement.name().name(), valueOf(evaluated))
                .<Result<Void>>map(this::commit)
                .orElseGet(
                        () ->
                                new Failure<>(
                                        "Variable no declarada: "
                                                + assignmentStatement.name().name(),
                                        assignmentStatement.range()));
    }

    @Override
    public Result<Void> visitPrintlnStatement(PrintlnStatement printlnStatement) {
        Result<Object> evaluated = evaluate(printlnStatement.expression());
        if (evaluated instanceof Failure<Object> failure) {
            return propagate(failure);
        }
        printer.print(ExpressionEvaluator.asText(valueOf(evaluated)));
        return new Success<>(null);
    }

    @Override
    public Result<Void> visitNumberLiteral(NumberLiteral numberLiteral) {
        return notAStatement(numberLiteral.range());
    }

    @Override
    public Result<Void> visitStringLiteral(StringLiteral stringLiteral) {
        return notAStatement(stringLiteral.range());
    }

    @Override
    public Result<Void> visitIdentifier(Identifier identifier) {
        return notAStatement(identifier.range());
    }

    @Override
    public Result<Void> visitBinaryExpression(BinaryExpression binaryExpression) {
        return notAStatement(binaryExpression.range());
    }

    private Result<Object> evaluate(Expression expression) {
        return expression.accept(new ExpressionEvaluator(environment));
    }

    // calcula el valor y guarda la variable
    private Result<Void> declare(VariableDeclaration declaration, Object value) {
        return environment
                .declare(declaration.name().name(), declaration.type().name(), value)
                .<Result<Void>>map(this::commit)
                .orElseGet(
                        () ->
                                new Failure<>(
                                        "Variable ya declarada: " + declaration.name().name(),
                                        declaration.range()));
    }

    private Result<Void> commit(Environment updated) {
        this.environment = updated;
        return new Success<>(null);
    }

    private static Object valueOf(Result<Object> result) {
        return ((Success<Object>) result).value();
    }

    private static Result<Void> propagate(Failure<Object> failure) {
        return new Failure<>(failure.message(), failure.range());
    }

    private static Result<Void> notAStatement(Range range) {
        return new Failure<>("No es una sentencia ejecutable", range);
    }
}
