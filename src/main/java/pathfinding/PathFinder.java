package pathfinding;

import agent.Mobile;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public abstract class PathFinder {

    protected final Graph graph;
    protected final ReservationTable table;
    protected final HashMap<Integer, Pair<Vector3D, Vector3D>> lastRoute;
    protected final HashMap<Integer, HashMap<Vector3D, Vector2D>> resumableDist;
    protected final HashMap<Integer, PriorityQueue<Pair<Vector3D, Vector2D>>> resumablePq;
    protected final HashMap<Integer, HashSet<Vector3D>> resumableClosed;
    protected double nextUpdateTime;

    public PathFinder(Graph graph) {
        this.graph = graph;
        this.table = new ReservationTable();
        this.lastRoute = new HashMap<>();
        this.resumableDist = new HashMap<>();
        this.resumablePq = new HashMap<>();
        this.resumableClosed = new HashMap<>();
    }

    public abstract void computePaths(double time, ArrayList<Mobile> mobiles);

    public double getNextUpdateTime() {
        return this.nextUpdateTime;
    }

    public void addGraphConstraint(GraphConstraint constraint) {
        this.table.addGraphConstraint(constraint);
    }

}
