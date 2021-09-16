package graph;

import junit.framework.TestCase;
import util.Vector3D;

public class ReservationTableTest extends TestCase {

    private int n;
    private Vector3D[] positions;

    public void setUp() throws Exception {
        super.setUp();

        this.n = 25;
        this.positions = new Vector3D[this.n];
        for(int i=0; i<this.n; i++) {
            this.positions[i] = new Vector3D(0, i);
        }
    }

    public void testReservation() {
        ReservationTable table = new ReservationTable();

        assertTrue(table.isAvailable(this.positions[0], 0, 0));

        table.reserve(this.positions[0], 0, 1);

        assertTrue(table.isAvailable(this.positions[0], 0, 1));
        assertFalse(table.isAvailable(this.positions[0], 0, 0));

        table.reserve(this.positions[0], 3 * ReservationTable.timeMargin, 1);

        assertFalse(table.isAvailable(this.positions[0], 2 * ReservationTable.timeMargin, 0));

        table.reserve(this.positions[0], 7 * ReservationTable.timeMargin, 1);

        assertTrue(table.isAvailable(this.positions[0], 5 * ReservationTable.timeMargin, 0));
    }

}