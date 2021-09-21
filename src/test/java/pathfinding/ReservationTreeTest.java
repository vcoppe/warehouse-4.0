package pathfinding;

import junit.framework.TestCase;
import util.Vector3D;

public class ReservationTreeTest extends TestCase {

    private static final Vector3D position = new Vector3D(0, 0, 0);
    ReservationTree tree;

    public void setUp() throws Exception {
        super.setUp();

        this.tree = new ReservationTree();
    }

    public void tearDown() throws Exception {
        super.tearDown();

        this.tree = null;
    }

    public void testFirstConflicts() {
        this.tree.insert(new Reservation(position, 0, 10, 0));
        this.tree.insert(new Reservation(position, 20, 30, 0));
        this.tree.insert(new Reservation(position, 40, 50, 0));

        assertTrue(this.tree.isAvailable(new Reservation(position, -10, 60, 0)));

        assertTrue(this.tree.isAvailable(new Reservation(position, -10, 0, 1)));
        assertFalse(this.tree.isAvailable(new Reservation(position, 0, 10, 1)));
        assertTrue(this.tree.isAvailable(new Reservation(position, 10, 20, 1)));
        assertFalse(this.tree.isAvailable(new Reservation(position, 20, 30, 1)));
        assertTrue(this.tree.isAvailable(new Reservation(position, 30, 40, 1)));
        assertFalse(this.tree.isAvailable(new Reservation(position, 40, 50, 1)));
        assertTrue(this.tree.isAvailable(new Reservation(position, 50, 60, 1)));
    }

    public void testAllConflicts() {
        this.tree.insert(new Reservation(position, 0, 10, 0));

        assertEquals(0, this.tree.allConflicts(new Reservation(position, 10, 20, 1)).size());
        assertEquals(1, this.tree.allConflicts(new Reservation(position, 2, 8, 1)).size());

        this.tree.insert(new Reservation(position, 0, 20, 0));
        this.tree.insert(new Reservation(position, 10, 20, 0));

        assertEquals(2, this.tree.allConflicts(new Reservation(position, 2, 8, 1)).size());
        assertEquals(3, this.tree.allConflicts(new Reservation(position, 5, 15, 1)).size());

        this.tree.insert(new Reservation(position, -20, -10, 0));
        this.tree.insert(new Reservation(position, -20, 20, 0));

        assertEquals(2, this.tree.allConflicts(new Reservation(position, -15, -5, 1)).size());
        assertEquals(4, this.tree.allConflicts(new Reservation(position, -15, 10, 1)).size());
    }
}