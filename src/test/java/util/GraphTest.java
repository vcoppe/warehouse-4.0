package util;

import junit.framework.TestCase;

public class GraphTest extends TestCase {

    public void testAddEdge() {
        Graph graph = new Graph();

        assertEquals(0, graph.getVertices().size());
        graph.addEdge(0, 1, 10);
        assertEquals(2, graph.getVertices().size());
        assertEquals(1, graph.getEdges(0).size());
        assertEquals(0, graph.getEdges(1).size()); // graph is directed
    }

    public void testAddEdges() {
        int n = 25;
        Graph graph = new Graph();

        assertEquals(0, graph.getVertices().size());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    graph.addEdge(i, j, 3);
                }
            }
        }
        assertEquals(n, graph.getVertices().size());
        for (int i = 0; i < n; i++) {
            assertEquals(n - 1, graph.getEdges(i).size());
        }

    }

    public void testSimplePath() {
        Graph graph = new Graph();

        for (int i = 0; i < 10; i++) {
            graph.addEdge(i, i + 1, 5);
        }

        assertEquals(11, graph.getShortestPath(0, 10).size());
        assertEquals(0, graph.getShortestPath(10, 0).size());
    }

}