package graph;

import agent.Mobile;
import junit.framework.TestCase;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;

import java.util.ArrayList;

public class WHCAStarTest extends TestCase {

    private int n;
    private Position[] positions;
    private Mobile mobile;

    public void setUp() throws Exception {
        super.setUp();

        this.n = 25;
        this.positions = new Position[n];
        for(int i=0; i<this.n; i++) {
            this.positions[i] = new Position(0, i);
        }
        this.mobile = new Mobile(this.positions[0]);
    }

    public void testSimplePath() {
        Graph graph = new Graph();

        for (int i = 0; i < 10; i++) {
            graph.addEdge(this.positions[i], this.positions[i + 1], 5);
        }

        this.mobile.start(new Mission(0, null, null, null, this.positions[0], this.positions[10]));
        this.mobile.pickUp();

        WHCAStar pathFinder = new WHCAStar();
        ArrayList<Mobile> mobiles = new ArrayList<>();
        mobiles.add(this.mobile);

        pathFinder.computePaths(0, mobiles, graph);

        ArrayList<Pair<Position,Double>> path = this.mobile.getPath();

        assertEquals(50 * this.mobile.getSpeed(), path.get(path.size()-1).second);
    }
}