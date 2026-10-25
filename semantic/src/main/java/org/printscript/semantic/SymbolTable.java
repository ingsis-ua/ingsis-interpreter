package org.printscript.semantic;

import java.util.Optional;
import org.printscript.common.ScopedValues;

// Tabla de nombres y tipos. La política de redeclaración pertenece a ScopedValues.
public final class SymbolTable {

    private final ScopedValues<Symbol> symbols;
    private final ScopedValues<Boolean> assigned;

    public SymbolTable() {
        this(new ScopedValues<>(), new ScopedValues<>());
    }

    private SymbolTable(ScopedValues<Symbol> symbols, ScopedValues<Boolean> assigned) {
        this.symbols = symbols;
        this.assigned = assigned;
    }

    public SymbolTable openScope() {
        return new SymbolTable(symbols.openScope(), assigned.openScope());
    }

    public SymbolTable closeScope() {
        return new SymbolTable(symbols.closeScope(), assigned.closeScope());
    }

    public Optional<SymbolTable> declare(String name, Symbol symbol) {
        return declare(name, symbol, true);
    }

    public Optional<SymbolTable> declare(String name, Symbol symbol, boolean initialized) {
        return symbols.declare(name, symbol)
                .map(declared -> new SymbolTable(declared, markHere(name, initialized)));
    }

    public SymbolTable assign(String name) {
        return new SymbolTable(symbols, markHere(name, true));
    }

    public Optional<Symbol> read(String name) {
        return symbols.read(name);
    }

    public boolean isInitialized(String name) {
        return assigned.read(name).orElse(true);
    }

    private ScopedValues<Boolean> markHere(String name, boolean value) {
        return assigned.declare(name, value)
                .orElseGet(() -> assigned.replace(name, value).orElseThrow());
    }
}
