package graph;

import junit.framework.TestCase;
import warehouse.Position;

public class GraphTest extends TestCase {

    private int n;
    private Position [] positions;

    public void setUp() throws Exception {
        super.setUp();

        this.n = 25;
        this.positions = new Position[this.n];
        for(int i=0; i<this.n; i++) {
            this.positions[i] = new Position(0, i);
        }
    }

    public void testAddEdge() {
        Graph graph = new Graph();

        assertEquals(0, graph.getVertices().size());
        graph.addEdge(this.positions[0], this.positions[1], 10);
        assertEquals(2, graph.getVertices().size());
        assertEquals(1, graph.getEdges(this.positions[0]).size());
        assertEquals(0, graph.getEdges(this.positions[1]).size()); // graph is directed
    }

    public void testAddEdges() {
        Graph graph = new Graph();

        assertEquals(0, graph.getVertices().size());
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.n; j++) {
                if (i != j) {
                    graph.addEdge(this.positions[i], this.positions[j], 3);
                }
            }
        }
        assertEquals(this.n, graph.getVertices().size());
        for (int i = 0; i < this.n; i++) {
            assertEquals(this.n - 1, graph.getEdges(this.positions[i]).size());
        }

    }

    public void testSimplePath() {
        Graph graph = new Graph();

        for (int i = 0; i < 10; i++) {
            graph.addEdge(this.positions[i], this.positions[i + 1], 5);
        }

        assertEquals(50.0, graph.getShortestPath(this.positions[0], this.positions[10]));
    }

}