package pathfinding;

import agent.Stock;
import util.Vector3D;
import warehouse.Pallet;

import java.util.HashSet;
import java.util.Stack;

public class AccessibilityChecker {

    private final Stock stock;
    private final Graph graph;

    public AccessibilityChecker(Stock stock, Graph graph) {
        this.stock = stock;
        this.graph = graph;
    }

    public boolean check(Vector3D position) {
        Pallet pallet = this.stock.get(position);
        if (pallet == null) {
            return true;
        } else if (pallet != Pallet.FREE) {
            for (Edge edge : this.graph.getReverseEdges(position)) {
                Vector3D v = edge.to;
                pallet = this.stock.get(v);
                if (pallet == null) { // found exit
                    return true;
                } else if (this.stock.isFree(v)) {
                    if (check(v)) { // check neighbour
                        return true;
                    }
                }
            }
            return false;
        }

        // forbid access to subgraphs where a location is already locked
        // TODO do not forbid but set a precedence constraint
        int accesses = 0, locks = 0;

        Stack<Vector3D> stack = new Stack<>();
        HashSet<Vector3D> visited = new HashSet<>();

        stack.push(position);
        visited.add(position);

        while (!stack.empty()) {
            Vector3D u = stack.pop();

            for (Edge edge : this.graph.getReverseEdges(u)) {
                Vector3D v = edge.to;

                if (!visited.contains(v)) {
                    visited.add(v);
                    pallet = this.stock.get(v);
                    if (pallet == null) {
                        accesses++;
                    } else if (this.stock.isFree(v)) {
                        stack.push(v);
                    } else if (this.stock.isLocked(v)) {
                        locks++;
                    }
                }
            }
        }

        /*boolean nextToPallet = false;
        for (Edge edge : this.graph.getReverseEdges(position)) {
            Vector3D v = edge.to;
            pallet = this.stock.get(v);
            if (pallet != null && pallet != Pallet.FREE) {
                nextToPallet = true;
                break;
            }
        }*/

        return locks < accesses;// && (accesses > 1 || nextToPallet);
    }

}
