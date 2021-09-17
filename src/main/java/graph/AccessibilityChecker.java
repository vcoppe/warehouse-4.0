package graph;

import agent.Stock;
import util.Vector3D;
import warehouse.Pallet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class AccessibilityChecker {

    private final Stock stock;
    private final Graph graph;
    private final HashMap<Vector3D, Vector3D> component;
    private final HashMap<Vector3D, Boolean> accessible;

    public AccessibilityChecker(Stock stock, Graph graph) {
        this.stock = stock;
        this.graph = graph;
        this.component = new HashMap<>();
        this.accessible = new HashMap<>();
    }

    private void resetSet(Vector3D position) {
        if (this.component.containsKey(position)) {
            this.accessible.remove(this.component.get(position));
            this.component.remove(position);
        }
    }

    // must split components (add a new pallet -> can block)
    public void add(Vector3D position) {
        this.resetSet(position);
    }

    // must join components
    public void remove(Vector3D position) {
        this.resetSet(position);
    }

    public void lock(Vector3D position) {
        this.resetSet(position);
    }

    public boolean check(Vector3D position) {
        if (this.component.containsKey(position)) {
            if (this.accessible.containsKey(this.component.get(position))) {
                return this.accessible.get(this.component.get(position));
            } else {
                this.component.remove(position);
            }
        }

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
        this.component.put(position, position);

        while (!stack.empty()) {
            Vector3D u = stack.pop();

            for (Edge edge : this.graph.getReverseEdges(u)) {
                Vector3D v = edge.to;

                if (!visited.contains(v)) {
                    visited.add(v);
                    this.component.put(v, position);
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

        boolean accessible = locks < accesses;// && (accesses > 1 || nextToPallet);

        this.accessible.put(position, accessible);

        return accessible;
    }

}
