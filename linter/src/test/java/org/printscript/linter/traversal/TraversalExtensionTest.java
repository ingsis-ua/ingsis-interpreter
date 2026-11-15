package org.printscript.linter.traversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.ast.Block;
import org.printscript.ast.Identifier;
import org.printscript.ast.PrintlnStatement;
import org.printscript.common.Position;
import org.printscript.common.Range;

// agregar un nodo al linter es sumar sus hijos al registro: el buscador no cambia
class TraversalExtensionTest {

    private static final Range ANY = new Range(new Position(1, 1), new Position(1, 1));

    private final Identifier x = new Identifier("x", ANY);
    private final PrintlnStatement println = new PrintlnStatement(x, ANY);

    @Test
    void aNewEntryChangesWhatIsFoundWithoutTouchingTheSearch() {
        NodeChildren withPrintln =
                NodeChildren.none().with(PrintlnStatement.class, p -> List.of(p.expression()));

        assertEquals(List.of(x), new NodeSearch(withPrintln).all(println, Identifier.class));
        assertEquals(List.of(), new NodeSearch(NodeChildren.none()).all(println, Identifier.class));
    }

    @Test
    void aNodeCannotBeRegisteredTwice() {
        NodeChildren standard = NodeChildren.standard();

        assertThrows(
                IllegalArgumentException.class, () -> standard.with(Block.class, b -> List.of()));
    }

    @Test
    void outermostDoesNotLookInsideAMatch() {
        Block inner = new Block(List.of(println), ANY);
        Block outer = new Block(List.of(inner), ANY);
        NodeSearch search = new NodeSearch(NodeChildren.standard());

        assertEquals(List.of(outer, inner), search.all(outer, Block.class));
        assertEquals(List.of(outer), search.outermost(outer, Block.class));
    }
}
