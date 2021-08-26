package graph;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.*;

public class ODID extends PathFinder {

    private static int H = 40;
    private static int TOL = 0;
    private static int END_MARGIN = 5;

    private static HashMap<Integer, Mobile> map = new HashMap<>();
    private static ReservationTable avoidanceTable;

    public ODID(Graph graph) {
        super(graph);
    }

    @Override
    public void computePaths(double time, ArrayList<Mobile> mobiles) {
        this.table.clear();
        map.clear();
        avoidanceTable = this.table.clone();

        ArrayList<ODIDGroup> groups = new ArrayList<>();
        //groups.add(new ODIDGroup(mobiles)); // create only one group to test algo

        for (Mobile mobile : mobiles) {
            map.put(mobile.getId(), mobile);

            // create one group for each mobile
            groups.add(new ODIDGroup(mobile));

            // reserve current position
            Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
            if (pair.first.first.equals(pair.second.first)) {
                if (pair.second.first.equals(mobile.getTargetPosition()) && mobile.isAvailable()) {
                    this.table.reserve(pair.first.first, pair.first.second, Double.MAX_VALUE / 2, mobile.getId());
                } else {
                    this.table.reserve(pair.first.first, pair.first.second, pair.second.second, mobile.getId());
                }
            } else {
                this.table.reserve(pair.first.first, pair.first.second, mobile.getId());
                if (pair.second.first.equals(mobile.getTargetPosition()) && mobile.isAvailable()) {
                    this.table.reserve(pair.second.first, pair.second.second, Double.MAX_VALUE / 2, mobile.getId());
                } else {
                    this.table.reserve(pair.second.first, pair.second.second, mobile.getId());
                }
            }
        }

        ODIDNode node = new ODIDNode(time, groups);

        for (ODIDGroup group : groups) {
            if (!this.computePath(time, node, group, this.table.clone(), Double.MAX_VALUE)) {
                System.out.println("ODID: No greedy path found.");
                System.exit(0);
            }

            this.setAndCheckPaths(time, node, group, avoidanceTable);
        }

        HashSet<ODIDConflict> pastConflicts = new HashSet<>();

        while (true) {
            // set and check paths
            avoidanceTable = this.table.clone();
            ODIDConflict conflict = this.setAndCheckPaths(time, node, avoidanceTable);

            if (conflict == null /*||
                    (conflict.reservations.first.end > time + H)  ||
                    (conflict.reservations.second.end > time + H)*/) {
                break;
            } else {
                System.out.println("Found conflict:");
                System.out.printf("Mobile %d at position %s from %f to %f\n", conflict.reservations.first.mobileId, conflict.reservations.first.position, conflict.reservations.first.start, conflict.reservations.first.end);
                System.out.printf("Mobile %d at position %s from %f to %f\n", conflict.reservations.second.mobileId, conflict.reservations.second.position, conflict.reservations.second.start, conflict.reservations.second.end);

                ODIDGroup groupA = node.groups.stream().filter(group -> group.contains(conflict.reservations.first.mobileId)).findFirst().orElse(null);
                ODIDGroup groupB = node.groups.stream().filter(group -> group.contains(conflict.reservations.second.mobileId)).findFirst().orElse(null);

                if (groupA.equals(groupB)) {
                    System.out.println("ODID: Conflict between mobiles of same group");
                    System.exit(0);
                }

                if (pastConflicts.contains(conflict)) {
                    System.out.println("ODID: Found past conflict, merging A and B");
                    ODIDGroup merged = node.merge(groupA, groupB);
                    this.computePath(time, node, merged, this.table.clone(), Double.MAX_VALUE);

                    pastConflicts.clear();
                } else {
                    pastConflicts.add(conflict);

                    // replan shorter path first
                    if (node.dist.get(groupA).get(node.end.get(groupA)).time > node.dist.get(groupB).get(node.end.get(groupB)).time) {
                        ODIDGroup tmp = groupA;
                        groupA = groupB;
                        groupB = tmp;
                    }

                    System.out.println("Mobiles in groups:");
                    System.out.print("\tA:");
                    for (Mobile mobile : groupA) System.out.print(" " + mobile.getId());
                    System.out.println();
                    System.out.print("\tB:");
                    for (Mobile mobile : groupB) System.out.print(" " + mobile.getId());
                    System.out.println();

                    // try to replan group A
                    ReservationTable constraintsB = this.table.clone();
                    this.setAndCheckPaths(time, node, groupB, constraintsB);
                    ODIDNode replanA = node.clone();

                    System.out.println("Trying to replan group A");
                    if (this.computePath(time, replanA, groupA, constraintsB, node.dist.get(groupA).get(node.end.get(groupA)).time + TOL * groupA.size())) {
                        node = replanA;
                    } else {
                        // try to replan group B
                        ReservationTable constraintsA = this.table.clone();
                        this.setAndCheckPaths(time, node, groupA, constraintsA);
                        ODIDNode replanB = node.clone();

                        System.out.println("Trying to replan group B");
                        if (this.computePath(time, replanB, groupB, constraintsA, node.dist.get(groupB).get(node.end.get(groupB)).time + TOL * groupB.size())) {
                            node = replanB;
                        } else { // failed both replanning attempts
                            System.out.println("Merging A and B");
                            ODIDGroup merged = node.merge(groupA, groupB);
                            this.computePath(time, node, merged, this.table.clone(), Double.MAX_VALUE);

                            pastConflicts.clear();
                        }
                    }
                }
            }
        }
    }

    private boolean computePath(double time, ODIDNode node, ODIDGroup group, ReservationTable table, double maxTime) {
        boolean debug = false;

        HashMap<ODIDState, ODIDCost> dist = node.dist.get(group);
        HashMap<ODIDState, ODIDCost> h = node.h.get(group);
        HashMap<ODIDState, ODIDState> prev = node.prev.get(group);

        dist.clear();
        h.clear();
        prev.clear();

        double groupEstimate = 0;
        for (Mobile mobile : group) {
            this.initReverseResumableAStar(mobile);

            Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);

            Vector2D dist2D = this.reverseResumableAStar(mobile, pair.second.first, mobile.getPosition());
            groupEstimate += DoublePrecisionConstraint.round(dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
        }

        ODIDState prevState = node.initial.get(group);
        ODIDState startState = node.start.get(group);
        ODIDState endState = node.end.get(group);

        PriorityQueue<Pair<ODIDState, ODIDCost>> pq = new PriorityQueue<>(Comparator.comparing(Pair::getSecond));

        dist.put(startState, new ODIDCost(0, 0));
        prev.put(startState, prevState);
        h.put(startState, new ODIDCost(groupEstimate, 0));
        startState.table = table;

        pq.add(new Pair<>(startState, new ODIDCost(groupEstimate, 0)));

        if (debug) {
            System.out.println("path for group with mobiles:");
            for (Mobile mobile : group) {
                System.out.println(mobile.getId() + " at pos " + startState.positions.get(mobile.getId()).first +
                        ", needs to reach " + endState.positions.get(mobile.getId()).first);
            }
            System.out.println();
        }

        while (!pq.isEmpty()) {
            Pair<ODIDState, ODIDCost> p = pq.poll();

            ODIDState u = p.first;
            ODIDCost estimateU = p.second;

            if (estimateU.compareTo(h.get(u)) > 0) { // not the shortest path anymore
                if (debug) System.out.println("skipped");
                continue;
            }

            if (u.equals(endState)) {
                if (debug) System.out.println("reached end state");
                return true;
            }

            ODIDCost costU = dist.get(u);
            int mobileId = u.mobileId;
            Mobile mobile = map.get(mobileId);
            Vector3D positionU = u.positions.get(mobileId).first;
            double timeU = u.positions.get(mobileId).second;

            if (timeU > maxTime) {
                if (debug) System.out.println("Could not find path with time constraint");
                return false;
            }

            if (debug) System.out.println("mobile " + mobileId + " reached pos " + positionU);

            for (Edge edge : this.graph.getEdges(positionU)) {
                Vector3D positionV = edge.to;
                Vector2D w = edge.weight;

                if (!edge.canCross(timeU, mobile)) {
                    if (debug) System.out.println("cannot cross edge to pos " + positionV);
                    continue;
                }

                double delta = DoublePrecisionConstraint.round(w.getX() * mobile.getSpeed() + w.getY() * Lift.speed);
                double timeV = DoublePrecisionConstraint.round(timeU + delta);
                double endTimeV = timeV;

                boolean target = positionV.equals(endState.positions.get(mobileId).first);
                if (target) {
                    endTimeV = DoublePrecisionConstraint.round(endTimeV + END_MARGIN);
                }

                int nConflicts = costU.nConflicts;
                //if (timeV < time + H) {
                    if (!u.table.isAvailable(positionV, timeV, endTimeV, mobileId)) { // position already occupied at that time
                        if (debug) System.out.println("pos " + positionV + " is occupied at time " + timeV);
                        timeV = u.table.nextAvailability(positionV, timeV, endTimeV, mobileId); // get soonest available time
                        if (debug) System.out.println("soonest time to go there : " + timeV);
                        if (!u.table.isAvailable(positionU, timeU, DoublePrecisionConstraint.round(timeV - delta), mobileId)) {
                            if (debug) System.out.println("distU wait long enough to reach " + positionV);
                            continue; // mobile cannot wait in current position until the next position is available
                        }
                        Reservation conflict = avoidanceTable.firstConflict(positionU, timeU, DoublePrecisionConstraint.round(timeV - delta), mobileId);
                        if (conflict != null && !node.group.get(conflict.mobileId).equals(group)) {
                            nConflicts += node.group.get(conflict.mobileId).size();
                        }
                    }
                //}

                ODIDState v = u.successor(mobileId, new Pair<>(positionV, timeV), target);
                if (DoublePrecisionConstraint.round(timeV - timeU - delta) > 0)
                    v.table.reserve(positionU, timeU, DoublePrecisionConstraint.round(timeV - delta), mobileId);
                Reservation conflict = avoidanceTable.firstConflict(positionV, timeV, endTimeV, mobileId);
                if (conflict != null && !node.group.get(conflict.mobileId).equals(group)) {
                    nConflicts += node.group.get(conflict.mobileId).size();
                }

                ODIDCost costV = new ODIDCost(DoublePrecisionConstraint.round(costU.time + timeV - timeU), nConflicts);

                if (!dist.containsKey(v) || costV.compareTo(dist.get(v)) < 0) {
                    Vector2D prevDist2D = this.reverseResumableAStar(mobile, positionU, mobile.getPosition());
                    Vector2D dist2D = this.reverseResumableAStar(mobile, positionV, mobile.getPosition());
                    ODIDCost estimateV = new ODIDCost(DoublePrecisionConstraint.round(
                            estimateU.time + timeV - timeU +
                                    (dist2D.getX() - prevDist2D.getX()) * mobile.getSpeed() +
                                    (dist2D.getY() - prevDist2D.getY()) * Lift.speed), nConflicts);

                    dist.put(v, costV);
                    h.put(v, estimateV);
                    prev.put(v, u);
                    pq.add(new Pair<>(v, estimateV));
                }
            }
        }

        return false; // no path was found
    }

    private ODIDConflict setAndCheckPaths(double time, ODIDNode node, ReservationTable table) {
        ODIDConflict firstConflict = null;
        this.nextUpdateTime = Double.MAX_VALUE; //time + W;

        for (ODIDGroup group : node.groups) {
            ODIDConflict conflict = this.setAndCheckPaths(time, node, group, table);
            if (conflict != null && (firstConflict == null || conflict.reservations.first.start < firstConflict.reservations.first.start)) {
                firstConflict = conflict;
            }
        }

        return firstConflict;
    }

    private ODIDConflict setAndCheckPaths(double time, ODIDNode node, ODIDGroup group, ReservationTable table) {
        ODIDConflict firstConflict = null;

        HashMap<ODIDState, ODIDState> prev = node.prev.get(group);

        /*for (Map.Entry<ODIDState,ODIDState> entry : prev.entrySet()) {
            System.out.println("Prev entry:");
            for (Mobile mobile : group) {
                System.out.println("Mobile " + mobile.getId() + ": " +
                        entry.getKey().positions.get(mobile.getId()).first + " ==> " +
                        entry.getValue().positions.get(mobile.getId()).first
                );
            }
        }*/

        ODIDState initialState = node.initial.get(group);
        ODIDState startState = node.start.get(group);
        ODIDState endState = node.end.get(group);

        for (ODIDState state : node.prev.get(group).keySet()) {
            if (state.equals(endState)) {
                endState = state;
                break;
            }
        }

        HashMap<Integer, ArrayList<Pair<Vector3D, Double>>> paths = new HashMap<>();

        for (Mobile mobile : group) {
            ArrayList<Pair<Vector3D, Double>> path = new ArrayList<>();
            path.add(endState.positions.get(mobile.getId()));
            paths.put(mobile.getId(), path);
        }

        ODIDState currentState = endState;
        while (!currentState.equals(startState)) {
            currentState = prev.get(currentState);

            for (Mobile mobile : group) {
                Vector3D currentPosition = paths.get(mobile.getId()).get(0).first;
                Pair<Vector3D, Double> pair = currentState.positions.get(mobile.getId());
                if (pair.first.equals(currentPosition)) { // stayed at position
                    paths.get(mobile.getId()).get(0).second = pair.second;
                } else {
                    paths.get(mobile.getId()).add(0, pair);
                }
            }
        }

        for (Mobile mobile : group) {
            ArrayList<Pair<Vector3D, Double>> path = paths.get(mobile.getId());

            Pair<Vector3D, Double> pair = initialState.positions.get(mobile.getId());
            if (!pair.first.equals(startState.positions.get(mobile.getId()).first)) {
                path.add(0, pair);
            }

            // reserve path in table
            for (int i = path.size() - 1; i >= 0; i--) {
                Vector3D u = path.get(i).first;
                double timeU = path.get(i).second;
                if (i < path.size() - 1) {
                    Vector3D v = path.get(i + 1).first;
                    double timeV = path.get(i + 1).second;
                    Vector2D w = this.graph.getWeight(u, v);

                    if (DoublePrecisionConstraint.round(timeV - timeU - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed) > 0) {
                        double timeLeaveU = DoublePrecisionConstraint.round(timeV - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed);
                        path.add(i + 1, new Pair<>(u, timeLeaveU));
                        Reservation conflict = table.firstConflict(u, timeU, timeLeaveU, mobile.getId());
                        Reservation reservation = table.reserve(u, timeU, timeLeaveU, mobile.getId());
                        if (conflict != null && (firstConflict == null || conflict.start < firstConflict.reservations.first.start)) {
                            firstConflict = new ODIDConflict(conflict, reservation);
                        }
                    } else {
                        Reservation conflict = table.firstConflict(u, timeU, mobile.getId());
                        Reservation reservation = table.reserve(u, timeU, mobile.getId());
                        if (conflict != null && (firstConflict == null || conflict.start < firstConflict.reservations.first.start)) {
                            firstConflict = new ODIDConflict(conflict, reservation);
                        }
                    }
                } else {
                    if (timeU > time) this.nextUpdateTime = Math.min(this.nextUpdateTime, timeU);
                    Reservation conflict = table.firstConflict(u, timeU, timeU + END_MARGIN, mobile.getId());
                    Reservation reservation = table.reserve(u, timeU, timeU + END_MARGIN, mobile.getId());
                    //Reservation conflict = table.firstConflict(u, timeU, mobile.getId());
                    //Reservation reservation = table.reserve(u, timeU, mobile.getId());
                    if (conflict != null && (firstConflict == null || conflict.start < firstConflict.reservations.first.start)) {
                        firstConflict = new ODIDConflict(conflict, reservation);
                    }
                }
            }

            mobile.setPath(time, path);
        }

        return firstConflict;
    }

    private class ODIDNode {

        public final double time;
        public final ArrayList<ODIDGroup> groups;
        public final HashMap<Integer, ODIDGroup> group;
        public final HashMap<ODIDGroup, HashMap<ODIDState, ODIDCost>> dist;
        public final HashMap<ODIDGroup, HashMap<ODIDState, ODIDCost>> h;
        public final HashMap<ODIDGroup, HashMap<ODIDState, ODIDState>> prev;
        public final HashMap<ODIDGroup, ODIDState> initial;
        public final HashMap<ODIDGroup, ODIDState> start;
        public final HashMap<ODIDGroup, ODIDState> end;

        public ODIDNode(double time, ArrayList<ODIDGroup> groups) {
            this.time = time;
            this.groups = groups;
            this.group = new HashMap<>();
            this.dist = new HashMap<>();
            this.h = new HashMap<>();
            this.prev = new HashMap<>();
            this.initial = new HashMap<>();
            this.start = new HashMap<>();
            this.end = new HashMap<>();

            for (ODIDGroup group : groups) {
                this.initGroup(group);
            }
        }

        public void initGroup(ODIDGroup group) {
            this.dist.put(group, new HashMap<>());
            this.h.put(group, new HashMap<>());
            this.prev.put(group, new HashMap<>());

            HashMap<Integer, Pair<Vector3D, Double>> prevPositions = new HashMap<>();
            HashMap<Integer, Pair<Vector3D, Double>> startPositions = new HashMap<>();
            HashMap<Integer, Pair<Vector3D, Double>> endPositions = new HashMap<>();

            for (Mobile mobile : group) {
                this.group.put(mobile.getId(), group);
                Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(this.time);

                prevPositions.put(mobile.getId(), new Pair<>(pair.first.first, pair.first.second));
                startPositions.put(mobile.getId(), new Pair<>(pair.second.first, pair.second.second));
                endPositions.put(mobile.getId(), new Pair<>(mobile.getTargetPosition(), 0.0));
            }

            this.initial.put(group, new ODIDState(prevPositions, null));
            this.start.put(group, new ODIDState(startPositions, null));
            this.end.put(group, new ODIDState(endPositions, null));
        }

        public ODIDGroup merge(ODIDGroup group1, ODIDGroup group2) {
            this.groups.remove(group1);
            this.groups.remove(group2);
            this.dist.remove(group1);
            this.dist.remove(group2);
            this.h.remove(group1);
            this.h.remove(group2);
            this.prev.remove(group1);
            this.prev.remove(group2);
            this.initial.remove(group1);
            this.initial.remove(group2);
            this.start.remove(group1);
            this.start.remove(group2);
            this.end.remove(group1);
            this.end.remove(group2);

            ODIDGroup merged = group1.merge(group2);

            this.groups.add(merged);
            this.initGroup(merged);

            return merged;
        }

        public ODIDNode clone() {
            ODIDNode node = new ODIDNode(this.time, this.groups);

            for (ODIDGroup group : this.groups) {
                node.dist.put(group, new HashMap<>(this.dist.get(group)));
                node.h.put(group, new HashMap<>(this.h.get(group)));
                node.prev.put(group, new HashMap<>(this.prev.get(group)));
            }

            return node;
        }
    }

    class ODIDGroup implements Iterable<Mobile> {

        ArrayList<Mobile> mobiles;
        int[] ids;

        public ODIDGroup(ArrayList<Mobile> mobiles) {
            this.mobiles = new ArrayList<>(mobiles);
            this.mobiles.sort(Comparator.comparing(Mobile::getId));
            this.ids = new int[this.mobiles.size()];
            for (int i = 0; i < this.mobiles.size(); i++) this.ids[i] = this.mobiles.get(i).getId();
        }

        public ODIDGroup(Mobile mobile) {
            this.mobiles = new ArrayList<>();
            this.mobiles.add(mobile);
            this.ids = new int[]{mobile.getId()};
        }

        public ODIDGroup merge(ODIDGroup other) {
            ArrayList<Mobile> mobiles = new ArrayList<>(this.mobiles);
            mobiles.addAll(other.mobiles);
            return new ODIDGroup(mobiles);
        }

        public boolean contains(Mobile mobile) {
            return this.mobiles.contains(mobile);
        }

        public boolean contains(int mobileId) {
            for (int i = 0; i < this.ids.length; i++) {
                if (this.ids[i] == mobileId) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ODIDGroup other = (ODIDGroup) o;

            if (this.mobiles.size() != other.mobiles.size()) return false;

            for (int i = 0; i < this.ids.length; i++) {
                if (this.ids[i] != other.ids[i]) return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.ids);
        }

        @Override
        public Iterator<Mobile> iterator() {
            return this.mobiles.iterator();
        }

        public int size() {
            return this.mobiles.size();
        }
    }

    class ODIDState {

        HashMap<Integer, Pair<Vector3D, Double>> positions;
        ReservationTable table;
        int hash, mobileId;

        public ODIDState(HashMap<Integer, Pair<Vector3D, Double>> positions, ReservationTable table) {
            this.positions = positions;
            this.table = table;
            this.hash = -1;

            double minTime = Double.MAX_VALUE;
            for (Map.Entry<Integer, Pair<Vector3D, Double>> entry : this.positions.entrySet()) {
                if (!entry.getValue().first.equals(ODID.map.get(entry.getKey()).getTargetPosition()) && entry.getValue().second < minTime) {
                    minTime = entry.getValue().second;
                    this.mobileId = entry.getKey();
                }
            }
        }

        public ODIDState successor(int mobileId, Pair<Vector3D, Double> position, boolean target) {
            HashMap<Integer, Pair<Vector3D, Double>> positions = new HashMap<>(this.positions);
            positions.put(mobileId, position);

            ReservationTable table = this.table.clone();
            if (target) table.reserve(position.first, position.second, position.second + END_MARGIN, mobileId);
            else table.reserve(position.first, position.second, mobileId);

            return new ODIDState(positions, table);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ODIDState other = (ODIDState) o;

            if (this.positions.size() != other.positions.size()) return false;

            for (int id : this.positions.keySet()) {
                if (!other.positions.containsKey(id)) return false;

                Pair<Vector3D, Double> thisPair = this.positions.get(id);
                Pair<Vector3D, Double> otherPair = other.positions.get(id);

                if (!thisPair.first.equals(otherPair.first)) return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            if (this.hash == -1) {
                int[] partialHash = new int[this.positions.size()];

                int i = 0;
                for (Map.Entry<Integer, Pair<Vector3D, Double>> entry : this.positions.entrySet()) {
                    partialHash[i++] = Objects.hash(
                            entry.getKey(),
                            entry.getValue().getFirst().getX(),
                            entry.getValue().getFirst().getY(),
                            entry.getValue().getFirst().getZ()/*,
                            entry.getValue().getSecond()*/
                    );
                }
                Arrays.sort(partialHash);

                this.hash = Arrays.hashCode(partialHash);
            }
            return this.hash;
        }
    }

    class ODIDCost implements Comparable<ODIDCost> {

        double time;
        int nConflicts;

        public ODIDCost(double time, int nConflicts) {
            this.time = time;
            this.nConflicts = nConflicts;
        }

        @Override
        public int compareTo(ODIDCost other) {
            /*if (this.nConflicts == other.nConflicts) {
                return Double.compare(this.time, other.time);
            }
            return this.nConflicts - other.nConflicts;*/
            if (this.time == other.time) {
                return this.nConflicts - other.nConflicts;
            }
            return Double.compare(this.time, other.time);
            //return Double.compare(this.time + this.nConflicts, other.time + other.nConflicts);
        }
    }

    class ODIDConflict {

        public final Pair<Reservation, Reservation> reservations;

        public ODIDConflict(Reservation r1, Reservation r2) {
            if (r1.start < r2.start) this.reservations = new Pair<>(r1, r2);
            else if (r1.start > r2.start) this.reservations = new Pair<>(r2, r1);
            else if (r1.end < r2.end) this.reservations = new Pair<>(r1, r2);
            else if (r1.end > r2.end) this.reservations = new Pair<>(r2, r1);
            else if (r1.mobileId < r2.mobileId) this.reservations = new Pair<>(r1, r2);
            else this.reservations = new Pair<>(r2, r1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ODIDConflict other = (ODIDConflict) o;

            if (this.reservations.first.start != other.reservations.first.start) return false;
            if (this.reservations.first.end != other.reservations.first.end) return false;
            if (this.reservations.first.mobileId != other.reservations.first.mobileId) return false;
            if (!this.reservations.first.position.equals(other.reservations.first.position)) return false;
            if (this.reservations.second.start != other.reservations.second.start) return false;
            if (this.reservations.second.end != other.reservations.second.end) return false;
            if (this.reservations.second.mobileId != other.reservations.second.mobileId) return false;
            if (!this.reservations.second.position.equals(other.reservations.second.position)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    this.reservations.first.start,
                    this.reservations.first.end,
                    this.reservations.first.mobileId,
                    this.reservations.first.position.getX(),
                    this.reservations.first.position.getY(),
                    this.reservations.first.position.getZ(),
                    this.reservations.second.start,
                    this.reservations.second.end,
                    this.reservations.second.mobileId,
                    this.reservations.second.position.getX(),
                    this.reservations.second.position.getY(),
                    this.reservations.second.position.getZ()
            );
        }

    }

}
