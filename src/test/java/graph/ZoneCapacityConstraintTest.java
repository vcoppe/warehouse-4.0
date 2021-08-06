package graph;

import junit.framework.TestCase;
import util.Vector3D;

public class ZoneCapacityConstraintTest extends TestCase {

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

    public void testSingle() {
        ZoneCapacityConstraint zoneCapacityConstraint = new ZoneCapacityConstraint(this.positions, 1);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));

        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));

        this.reservationTable.reserve(this.positions[0], 0, 1, 0);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 1));

        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
    }

    public void testMultiple() {
        ZoneCapacityConstraint zoneCapacityConstraint = new ZoneCapacityConstraint(this.positions, 2);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));

        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 2));

        this.reservationTable.reserve(this.positions[0], 0, 1, 0);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 1));

        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 2));

        this.reservationTable.reserve(this.positions[1], 0, 1, 1);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 1));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 2));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 2));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 2));

        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 2));
        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[1], 0, 1, 2));
        assertEquals(2.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[2], 0, 1, 2));
    }

    public void testMultipleComplex() {
        ZoneCapacityConstraint zoneCapacityConstraint = new ZoneCapacityConstraint(this.positions, 2);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));

        this.reservationTable.reserve(this.positions[0], 0, 10, 0);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 0, 1, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 0, 1, 1));

        this.reservationTable.reserve(this.positions[1], 5, 15, 1);

        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 5, 6, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 5, 6, 0));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 5, 6, 0));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 5, 6, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 5, 6, 1));
        assertTrue(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 5, 6, 1));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[0], 5, 6, 2));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[1], 5, 6, 2));
        assertFalse(zoneCapacityConstraint.isAvailableWithMargin(this.reservationTable, this.positions[2], 5, 6, 2));

        assertEquals(11.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 0, 1, 2));
        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[1], 0, 1, 2));
        assertEquals(0.0, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[2], 0, 1, 2));

        assertEquals(11.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[0], 4, 1, 2));
        assertEquals(16.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[1], 4, 1, 2));
        assertEquals(11.5, zoneCapacityConstraint.nextAvailabilityWithMargin(this.reservationTable, this.positions[2], 4, 1, 2));
    }
}