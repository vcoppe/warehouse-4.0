package pathfinding;

import junit.framework.TestCase;
import util.Vector3D;

public class ZoneConstraintTest extends TestCase {

    ReservationTable reservationTable;
    Vector3D[] positions;

    public void setUp() throws Exception {
        super.setUp();
        this.reservationTable = new ReservationTable();
        this.positions = new Vector3D[10];
        for (int i = 0; i < this.positions.length; i++) {
            this.positions[i] = new Vector3D(i, 0, 0);
        }
    }

    public void tearDown() throws Exception {
        this.reservationTable = null;
        this.positions = null;
    }

    /*public void testSingle() {
        ZoneConstraint constraint = new ZoneConstraint(this.positions, 1);
        this.reservationTable.addGraphConstraint(constraint);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));

        this.reservationTable.reserve(this.positions[0], 0, 1, 0);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 1));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 0, 1, 1));
    }

    public void testMultiple() {
        ZoneConstraint constraint = new ZoneConstraint(this.positions, 2);
        this.reservationTable.addGraphConstraint(constraint);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));

        this.reservationTable.reserve(this.positions[0], 0, 1, 0);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 1));

        this.reservationTable.reserve(this.positions[1], 0, 1, 1);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 1));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 2));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 0, 1, 2));
        assertFalse(this.reservationTable.isAvailable(this.positions[2], 0, 1, 2));
    }

    public void testMultipleComplex() {
        ZoneConstraint constraint = new ZoneConstraint(this.positions, 2);
        this.reservationTable.addGraphConstraint(constraint);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));

        this.reservationTable.reserve(this.positions[0], 0, 10, 0);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 0, 1, 1));

        this.reservationTable.reserve(this.positions[1], 5, 15, 1);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 5, 6, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 5, 6, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 5, 6, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 5, 6, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 5, 6, 1));
        assertTrue(this.reservationTable.isAvailable(this.positions[2], 5, 6, 1));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 5, 6, 2));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 5, 6, 2));
        assertFalse(this.reservationTable.isAvailable(this.positions[2], 5, 6, 2));
    }*/
}