package graph;

import agent.Lift;
import junit.framework.TestCase;
import util.Vector3D;

public class LiftConstraintTest extends TestCase {

    ReservationTable reservationTable;
    Vector3D[] positions;

    public void setUp() throws Exception {
        super.setUp();
        this.reservationTable = new ReservationTable();
        this.positions = new Vector3D[10];
        for (int i = 0; i < this.positions.length; i++) {
            this.positions[i] = new Vector3D(0, 0, i);
        }
    }

    public void tearDown() throws Exception {
        this.reservationTable = null;
        this.positions = null;
    }

    public void test() {
        LiftConstraint constraint = new LiftConstraint(this.positions);
        this.reservationTable.addGraphConstraint(constraint);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertEquals(0.0, this.reservationTable.nextAvailability(this.positions[0], 0, 1, 0));
        assertEquals(0.0, this.reservationTable.nextAvailability(this.positions[0], 0, 1, 1));

        this.reservationTable.reserve(this.positions[0], 0, 1, 0);

        assertTrue(this.reservationTable.isAvailable(this.positions[0], 0, 1, 0));
        assertTrue(this.reservationTable.isAvailable(this.positions[1], 0, 1, 0));
        assertFalse(this.reservationTable.isAvailable(this.positions[0], 0, 1, 1));
        assertFalse(this.reservationTable.isAvailable(this.positions[1], 0, 1, 1));

        assertEquals(0.0, this.reservationTable.nextAvailability(this.positions[0], 0, 1, 0));

        for (int i = 0; i < this.positions.length; i++) {
            assertEquals(1 + i * Lift.speed + 2 * ReservationTable.timeMargin, this.reservationTable.nextAvailability(this.positions[i], 0, 1, 1));
            assertFalse(this.reservationTable.isAvailable(this.positions[i], -100, -i * Lift.speed - 2 * ReservationTable.timeMargin + 1, 1));
            assertTrue(this.reservationTable.isAvailable(this.positions[i], -100, -i * Lift.speed - 2 * ReservationTable.timeMargin, 1));
        }
    }
}