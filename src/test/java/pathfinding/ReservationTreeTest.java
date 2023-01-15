package pathfinding;

import junit.framework.TestCase;
import util.Vector3D;

import java.util.ArrayList;

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

    public void testSafeIntervals() {
        ArrayList<Interval> safeIntervals = null;
        Interval interval = null;

        this.tree.insert(new Reservation(position, 0, 10, 0));

        safeIntervals = this.tree.getSafeIntervals(0);

        assertEquals(2, safeIntervals.size());

        if (safeIntervals.size() == 2) {
            interval = safeIntervals.get(0);
            assertEquals(-Double.MAX_VALUE, interval.start, 1e-3);
            assertEquals(0, interval.end, 1e-3);

            interval = safeIntervals.get(1);
            assertEquals(10, interval.start, 1e-3);
            assertEquals(Double.MAX_VALUE, interval.end, 1e-3);
        }

        this.tree.insert(new Reservation(position, 10, 20, 0));

        safeIntervals = this.tree.getSafeIntervals(0);

        assertEquals(2, safeIntervals.size());

        if (safeIntervals.size() == 2) {
            interval = safeIntervals.get(0);
            assertEquals(-Double.MAX_VALUE, interval.start, 1e-3);
            assertEquals(0, interval.end, 1e-3);

            interval = safeIntervals.get(1);
            assertEquals(20, interval.start, 1e-3);
            assertEquals(Double.MAX_VALUE, interval.end, 1e-3);
        }

        this.tree.insert(new Reservation(position, 30, 40, 0));

        safeIntervals = this.tree.getSafeIntervals(0);

        assertEquals(3, safeIntervals.size());

        if (safeIntervals.size() == 3) {
            interval = safeIntervals.get(0);
            assertEquals(-Double.MAX_VALUE, interval.start, 1e-3);
            assertEquals(0, interval.end, 1e-3);

            interval = safeIntervals.get(1);
            assertEquals(20, interval.start, 1e-3);
            assertEquals(30, interval.end, 1e-3);

            interval = safeIntervals.get(2);
            assertEquals(40, interval.start, 1e-3);
            assertEquals(Double.MAX_VALUE, interval.end, 1e-3);
        }
    }
}