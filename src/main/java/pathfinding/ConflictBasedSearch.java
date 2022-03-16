package pathfinding;

import agent.Mobile;
import util.Pair;
import util.Vector3D;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictBasedSearch extends PathFinder {

    private final static CollisionDetection<Mobile, Mobile> collisionDetection = new SimpleCollisionDetection();
    private HashMap<Integer, Mobile> mobiles;

    public ConflictBasedSearch(Graph graph) {
        super(graph);
    }

    private static Conflict getFirstConflict(Mobile m1, Path p1, Mobile m2, Path p2) {
        Iterator<Action> it1 = p1.getActions().iterator(), it2 = p2.getActions().iterator();
        if (!it1.hasNext() || !it2.hasNext()) {
            return null;
        }

        Action a1 = it1.next(), a2 = it2.next();
        while (true) {
            while (a1.endTime() < a2.startTime()) {
                if (!it1.hasNext()) {
                    return null;
                }
                a1 = it1.next();
            }
            while (a2.endTime() < a1.startTime()) {
                if (!it2.hasNext()) {
                    return null;
                }
                a2 = it2.next();
            }

            if (collisionDetection.collides(a1, a2)) {
                return new Conflict(m1, a1, m2, a2);
            }

            if (a1.endTime() < a2.endTime()) {
                if (!it1.hasNext()) {
                    return null;
                }
                a1 = it1.next();
            } else {
                if (!it2.hasNext()) {
                    return null;
                }
                a2 = it2.next();
            }
        }
    }

    // add conflict avoidance table
    public static Path safeIntervalPathPlanning(Mobile mobile, ArrayList<Constraint> constraints) {
        return null;
    }

    @Override
    public void computePaths(double time, ArrayList<Mobile> mobiles) {
        this.mobiles = new HashMap<>();

        // init root node with shortest path for each mobile
        ConstraintTreeNode root = new ConstraintTreeNode();
        for (Mobile mobile : mobiles) {
            this.mobiles.put(mobile.getId(), mobile);

            Path path = safeIntervalPathPlanning(mobile, null);
            root.addPath(mobile, path);
            ArrayList<Conflict> newConflicts = this.findConflicts(root, mobile);
            if (newConflicts.size() == 1 && newConflicts.get(0).type == Conflict.Type.INFEASIBLE) {
                System.out.println("CBS: infeasible problem.");
                return;
            }
            root.addConflicts(newConflicts);
        }

        PriorityQueue<ConstraintTreeNode> queue = new PriorityQueue<>();
        queue.add(root);
        while (queue.size() > 0) {
            ConstraintTreeNode node = queue.poll();
            Conflict conflict = node.conflicts.poll();

            if (conflict == null) { // solution found
                this.setPaths(time, mobiles, node);
                return;
            }

            ArrayList<ConstraintTreeNode> children = new ArrayList<>();
            for (Mobile mobile : conflict.mobiles) {
                ConstraintTreeNode child = new ConstraintTreeNode(node);
                child.addConstraint(mobile, conflict.getConstraint(mobile));

                // check/merge constraints?

                Path path = safeIntervalPathPlanning(mobile, child.getAllConstraints(mobile));
                if (path != null) {
                    child.addPath(mobile, path);

                    ArrayList<Conflict> newConflicts = this.findConflicts(child, mobile);
                    if (newConflicts.size() == 1 && newConflicts.get(0).type == Conflict.Type.INFEASIBLE) continue;
                    child.addConflicts(newConflicts);
                    child.addConflicts(node.getConflicts().stream().filter(c -> c.mobiles[0] != mobile && c.mobiles[1] != mobile).collect(Collectors.toCollection(ArrayList::new)));

                    child.updateHeuristic();
                    children.add(child);
                }
            }

            // choose one of the children to get a positive constraint

            queue.addAll(children);
        }

        System.out.println("CBS: infeasible problem.");
    }

    private void setPaths(double time, ArrayList<Mobile> mobiles, ConstraintTreeNode node) {
        HashMap<Integer, Path> paths = node.getAllPaths();
        for (Mobile mobile : mobiles) {
            mobile.setPath(time, paths.get(mobile.getId()));
        }
    }

    private ArrayList<Conflict> findConflicts(ConstraintTreeNode node, Mobile mobile) {
        HashMap<Integer, Path> paths = node.getAllPaths();
        HashMap<Integer, ArrayList<Constraint>> constraints = node.getAllConstraints();

        double[] overcost = new double[2];

        ArrayList<Conflict> conflicts = new ArrayList<>();
        for (Map.Entry<Integer, Path> entry : paths.entrySet()) {
            if (entry.getKey() == mobile.getId()) continue;

            Conflict firstConflict = getFirstConflict(
                    mobile, paths.get(mobile.getId()),
                    mobiles.get(entry.getKey()), entry.getValue()
            );

            if (firstConflict != null) {
                for (int i = 0; i < 2; i++) {
                    Mobile m = firstConflict.mobiles[i];

                    if (!constraints.containsKey(m.getId())) {
                        constraints.put(m.getId(), new ArrayList<>());
                    }
                    ArrayList<Constraint> mobileConstraints = constraints.get(m.getId());
                    mobileConstraints.add(firstConflict.getConstraint(m));
                    Path path = safeIntervalPathPlanning(m, mobileConstraints);
                    mobileConstraints.remove(mobileConstraints.size() - 1);

                    if (path == null) overcost[i] = Double.MAX_VALUE;
                    else overcost[i] = path.getCost() - paths.get(m.getId()).getCost();
                }

                if (Math.min(overcost[0], overcost[1]) == Double.MAX_VALUE) {
                    conflicts.clear();
                    firstConflict.type = Conflict.Type.INFEASIBLE;
                    return conflicts;
                } else {
                    firstConflict.cost = Math.min(overcost[0], overcost[1]);
                    if (Math.min(overcost[0], overcost[1]) > 0) firstConflict.type = Conflict.Type.CARDINAL;
                    else if (Math.max(overcost[0], overcost[1]) == 0) firstConflict.type = Conflict.Type.NONCARDINAL;
                    else firstConflict.type = Conflict.Type.SEMICARDINAL;
                }

                conflicts.add(firstConflict);
            }
        }
        return conflicts;
    }

    private static class Conflict implements Comparable<Conflict> {

        Mobile[] mobiles;
        Action[] actions;
        double cost;
        Type type;

        public Conflict(Mobile m1, Action a1, Mobile m2, Action a2) {
            this.mobiles = new Mobile[]{m1, m2};
            this.actions = new Action[]{a1, a2};
        }

        public Conflict(Mobile m1, Action a1, Mobile m2, Action a2, Type type, double cost) {
            this.mobiles = new Mobile[]{m1, m2};
            this.actions = new Action[]{a1, a2};
            this.type = type;
            this.cost = cost;
        }

        private void swap() {
            Mobile tmpMobile = this.mobiles[0];
            this.mobiles[0] = this.mobiles[1];
            this.mobiles[1] = tmpMobile;
            Action tmpAction = this.actions[0];
            this.actions[0] = this.actions[1];
            this.actions[1] = tmpAction;
        }

        public Constraint getConstraint(Mobile m) {
            if (m == this.mobiles[1]) this.swap();

            Action delayedAction = new Action(
                    new Pair<>(this.actions[0].startPosition(), this.actions[0].startTime()),
                    new Pair<>(this.actions[0].endPosition(), this.actions[0].endTime())
            );

            double lo = this.actions[0].startTime(), hi = this.actions[1].endTime();
            while (hi - lo > 1e-3) {
                double mid = (lo + hi) / 2;
                delayedAction.from.second = mid;
                delayedAction.to.second = mid + this.actions[0].endTime() - this.actions[0].startTime();

                if (collisionDetection.collides(delayedAction, this.actions[1])) lo = mid;
                else hi = mid;
            }

            return new Constraint(this.actions[0].startPosition(), this.actions[0].endPosition(), this.actions[0].startTime(), lo);
        }

        @Override
        public int compareTo(Conflict other) {
            if (this.type == other.type) {
                return Double.compare(this.cost, other.cost);
            }
            return this.type.ordinal() - other.type.ordinal();
        }

        enum Type {
            INFEASIBLE,
            CARDINAL,
            SEMICARDINAL,
            NONCARDINAL
        }

    }

    private static class Constraint {

        boolean positive;
        Vector3D from, to;
        double start, end;

        public Constraint(Vector3D from, Vector3D to, double start, double end, boolean positive) {
            this.from = from;
            this.to = to;
            this.start = start;
            this.end = end;
            this.positive = positive;
        }

        public Constraint(Vector3D from, Vector3D to, double start, double end) {
            this(from, to, start, end, false);
        }

    }

    private class ConstraintTreeNode implements Comparable<ConstraintTreeNode> {
        private final ConstraintTreeNode parent;
        private final HashMap<Integer, Path> paths;
        private final PriorityQueue<Conflict> conflicts;
        private final HashMap<Integer, ArrayList<Constraint>> constraints;
        private double cost, h;

        public ConstraintTreeNode(ConstraintTreeNode parent) {
            this.parent = parent;
            this.paths = new HashMap<>();
            this.conflicts = new PriorityQueue<>();
            this.constraints = new HashMap<>();
            if (parent == null) this.cost = 0;
            else this.cost = parent.cost;
        }

        public ConstraintTreeNode() {
            this(null);
        }

        public void addPath(Mobile mobile, Path path) {
            if (this.paths.containsKey(mobile.getId())) {
                this.cost -= this.paths.get(mobile.getId()).getCost();
            }
            this.paths.put(mobile.getId(), path);
            this.cost += path.getCost();
        }

        public void addConflicts(ArrayList<Conflict> conflicts) {
            this.conflicts.addAll(conflicts);
        }

        public void addConstraint(Mobile mobile, Constraint constraint) {
            if (!this.constraints.containsKey(mobile.getId())) {
                this.constraints.put(mobile.getId(), new ArrayList<>());
            }
            this.constraints.get(mobile.getId()).add(constraint);
        }

        public HashMap<Integer, Path> getAllPaths() {
            HashMap<Integer, Path> paths = new HashMap<>();
            ConstraintTreeNode current = this;
            while (current != null && paths.size() < mobiles.size()) {
                for (Map.Entry<Integer, Path> entry : current.paths.entrySet()) {
                    if (!paths.containsKey(entry.getKey())) {
                        paths.put(entry.getKey(), entry.getValue());
                    }
                }
                current = current.parent;
            }
            return paths;
        }

        public ArrayList<Constraint> getAllConstraints(Mobile mobile) {
            ArrayList<Constraint> constraints = new ArrayList<>();
            ConstraintTreeNode current = this;
            while (current != null) {
                if (current.constraints.containsKey(mobile.getId())) {
                    constraints.addAll(current.constraints.get(mobile.getId()));
                }
                current = current.parent;
            }
            return constraints;
        }

        public HashMap<Integer, ArrayList<Constraint>> getAllConstraints() {
            HashMap<Integer, ArrayList<Constraint>> constraints = new HashMap<>();
            ConstraintTreeNode current = this;
            while (current != null) {
                for (Map.Entry<Integer, ArrayList<Constraint>> entry : current.constraints.entrySet()) {
                    if (!constraints.containsKey(entry.getKey())) {
                        constraints.put(entry.getKey(), new ArrayList<>());
                    }
                    constraints.get(entry.getKey()).addAll(entry.getValue());
                }
                current = current.parent;
            }
            return constraints;
        }

        public void updateHeuristic() {
            // this.h
        }

        public double getEstimate() {
            return this.cost + this.h;
        }

        @Override
        public int compareTo(ConstraintTreeNode other) {
            return Double.compare(this.getEstimate(), other.getEstimate());
        }

        public PriorityQueue<Conflict> getConflicts() {
            return this.conflicts;
        }
    }

}
