package pathfinding;

import agent.Mobile;
import junit.framework.TestCase;
import util.Vector3D;
import warehouse.Mission;

import java.util.ArrayList;

public class SafeIntervalPathPlanningTest extends TestCase {

    private int n;
    private Vector3D[] positions;
    private Mobile mobile;

    public void setUp() throws Exception {
        super.setUp();

        this.n = 25;
        this.positions = new Vector3D[n];
        for(int i=0; i<this.n; i++) {
            this.positions[i] = new Vector3D(0, i);
        }
        this.mobile = new Mobile(this.positions[0]);
    }

    public void testSimplePath() {
        Graph graph = new Graph();

        for (int i = 0; i < 10; i++) {
            graph.addEdge(this.positions[i], this.positions[i + 1]);
            graph.addEdge(this.positions[i + 1], this.positions[i]);
        }

        this.mobile.start(new Mission(0, null, null, null, this.positions[0], this.positions[10]));
        this.mobile.pickUp();

        ArrayList<Mobile> mobiles = new ArrayList<>();
        mobiles.add(this.mobile);

        SafeIntervalPathPlanning pathFinder = new SafeIntervalPathPlanning(graph, mobiles);

        pathFinder.computePath(0, this.mobile);

        Path path = this.mobile.getPath();

        assertEquals(10 * this.mobile.getSpeed(), path.getEndTimedPosition().second, 1e-3);
    }
}