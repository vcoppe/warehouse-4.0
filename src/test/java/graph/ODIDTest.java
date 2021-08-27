package graph;

import agent.Mobile;
import graph.ODID.ODIDState;
import junit.framework.TestCase;
import util.Pair;
import util.Vector3D;

import java.util.HashMap;

public class ODIDTest extends TestCase {

    public void testState() {

        HashMap<Integer, Pair<Vector3D, Double>> positions1 = new HashMap<>();
        HashMap<Integer, Pair<Vector3D, Double>> positions2 = new HashMap<>();
        HashMap<Integer, Pair<Vector3D, Double>> positions3 = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            Mobile mobile = new Mobile(new Vector3D(i, 0, 0));
            ODID.map.put(mobile.getId(), mobile);
            positions1.put(mobile.getId(), new Pair<>(new Vector3D(0, i, 0), 0.0));
            positions2.put(mobile.getId(), new Pair<>(new Vector3D(0, i, 0), 10.0));
            positions3.put(mobile.getId(), new Pair<>(new Vector3D(0, 2 * i, 0), 10.0));
        }

        ODIDState state1 = new ODIDState(positions1, null);
        ODIDState state2 = new ODIDState(positions2, null);
        ODIDState state3 = new ODIDState(positions3, null);

        assertTrue(state1.equals(state2));
        assertTrue(state1.hashCode() == state2.hashCode());
        assertFalse(state1.equals(state3));
        assertTrue(state1.hashCode() != state3.hashCode());

    }

}