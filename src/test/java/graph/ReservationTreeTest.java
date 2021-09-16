package graph;

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
        Reservation r1 = new Reservation(position, 0, 10, 0);

        this.tree.insert(r1);

        assertNull(this.tree.firstConflict(new Reservation(position, 10, 20, 1)));
        assertEquals(r1, this.tree.firstConflict(new Reservation(position, 2, 8, 1)));

        this.tree.insert(new Reservation(position, 0, 20, 0));
        this.tree.insert(new Reservation(position, 10, 20, 0));

        Reservation r2 = new Reservation(position, -20, -10, 0);
        Reservation r3 = new Reservation(position, -15, 20, 0);

        this.tree.insert(r2);
        this.tree.insert(r3);

        assertEquals(r2, this.tree.firstConflict(new Reservation(position, -15, -5, 1)));
        assertEquals(r3, this.tree.firstConflict(new Reservation(position, -10, 10, 1)));
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