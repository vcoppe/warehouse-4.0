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
    private int id;
    private HashMap<Vector3D, Integer> component;
    private HashMap<Integer, Boolean> accessible;

    public AccessibilityChecker(Stock stock, Graph graph) {
        this.id = 0;
        this.stock = stock;
        this.graph = graph;
        this.component = new HashMap<>();
        this.accessible = new HashMap<>();
    }

    private int getId() {
        int currentId = this.id;
        this.id++;
        if (this.id < 0) this.id = 0;
        return currentId;
    }

    private void resetSet(Vector3D position) {
        if (this.component.containsKey(position)) {
            int component = this.component.get(position);
            this.accessible.remove(component);
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
        Pallet pallet = this.stock.get(position);
        if (pallet == null) {
            return true;
        } else if (pallet != Pallet.FREE) {
            for (Edge edge : this.graph.getReverseEdges(position)) {
                Vector3D v = edge.to;
                pallet = this.stock.get(v);
                if (pallet == null) {
                    return true;
                } else if (this.stock.isFree(v)) {
                    if (check(v)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /*if (this.component.containsKey(position)) {
            int component = this.component.get(position);
            Boolean access = this.accessible.get(component);
            if (access != null) {
                return access;
            }
        }*/

        //boolean accessible = false;
        //int component = this.getId();
        int accesses = 0;
        int locks = 0;


        Stack<Vector3D> stack = new Stack<>();
        HashSet<Vector3D> visited = new HashSet<>();

        stack.push(position);
        visited.add(position);
        //this.component.put(position, component);

        while (!stack.empty()) {
            Vector3D u = stack.pop();

            for (Edge edge : this.graph.getReverseEdges(u)) {
                Vector3D v = edge.to;

                //Integer currentComponent = this.component.get(v);

                if (!visited.contains(v)/*currentComponent == null || currentComponent != component*/) {
                    /*if (currentComponent != null) {
                        this.resetSet(v);
                    }*/
                    visited.add(v);
                    pallet = this.stock.get(v);
                    if (pallet == null) { // found exit
                        //accessible = true;
                        accesses++;
                    } else if (this.stock.isFree(v)) { // TODO do something if locked?
                        stack.push(v);
                        //this.component.put(v, component);
                    } else if (this.stock.isLocked(v)) {
                        locks++;
                    }
                }
            }
        }

        //this.accessible.put(component, accessible);

        //return accessible;
        return locks < accesses;
    }

}
