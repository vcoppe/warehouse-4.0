package graph;

import agent.Lift;
import agent.Mobile;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector2D;
import util.Vector3D;

import java.util.*;

public class CBS extends PathFinder {

    public static final int W = 30;

    public CBS(Graph graph) {
        super(graph);
    }

    @Override
    public void computePaths(double time, ArrayList<Mobile> mobiles) {
        this.table.clear();

        for (Mobile mobile : mobiles) { // reserve current position
            Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
            if (pair.first.first.equals(pair.second.first)) {
                this.table.reserve(pair.first.first, pair.first.second, pair.second.second, mobile.getId());
            } else {
                this.table.reserve(pair.first.first, pair.first.second, mobile.getId());
                this.table.reserve(pair.second.first, pair.second.second, mobile.getId());
            }
        }

        HashMap<Integer, Mobile> map = new HashMap<>();
        CBSNode root = new CBSNode(mobiles, this.table.clone());

        for (Mobile mobile : mobiles) {
            map.put(mobile.getId(), mobile);
            if (!this.computePath(time, mobile, root)) {
                System.out.println("CBS: Failed to compute path at root node.");
                System.exit(0);
            }
        }

        TreeSet<CBSNode> pq = new TreeSet<>();
        pq.add(root);

        while (!pq.isEmpty()) {
            CBSNode node = pq.pollFirst();

            System.out.println("Popped new node, with constraints:");
            for (Map.Entry<Vector3D, TreeSet<Reservation>> entry : node.table.reservations.entrySet()) {
                System.out.println("\tFor location " + entry.getKey());
                for (Reservation reservation : entry.getValue()) {
                    System.out.println("\tMobile " + reservation.mobileId + ": from " + reservation.start + " until " + reservation.end);
                }
            }

            CBSConflict conflict = this.firstConflict(time, node);
            if (conflict == null || conflict.reservations.first.start >= time + W || conflict.reservations.second.start >= time + W) {
                // set next update time to earliest destination time
                this.nextUpdateTime = time + W;
                for (Mobile mobile : mobiles) {
                    double endTime = mobile.getPath().get(mobile.getPath().size() - 1).second;
                    if (endTime > time) this.nextUpdateTime = Math.min(this.nextUpdateTime, endTime);
                }
                return;
            }

            Mobile[] conflictMobiles = {
                    map.get(conflict.reservations.first.mobileId),
                    map.get(conflict.reservations.second.mobileId)
            };

            Reservation[] conflictReservations = {
                    conflict.reservations.first,
                    conflict.reservations.second
            };

            System.out.println("Conflict found:");
            System.out.println("\tMobile " + conflictMobiles[0].getId() + ": " + conflictReservations[0].position + " from " + conflictReservations[0].start + " until " + conflictReservations[0].end);
            System.out.println("\tMobile " + conflictMobiles[1].getId() + ": " + conflictReservations[1].position + " from " + conflictReservations[1].start + " until " + conflictReservations[1].end);

            for (int i = 0; i < conflictMobiles.length; i++) {
                if (node.table.reservations.containsKey(conflictReservations[1 - i].position) &&
                        node.table.reservations.get(conflictReservations[1 - i].position).contains(conflictReservations[1 - i]))
                    continue;

                CBSNode succ = node.clone();
                succ.table.reserveWithMarginHelper(
                        conflictReservations[i].position,
                        conflictReservations[i].start,
                        conflictReservations[i].end,
                        conflictReservations[i].mobileId);

                // try to plan conflict mobiles with new constraint
                boolean hasSolution = this.computePath(time, conflictMobiles[1 - i], succ);

                if (hasSolution) {
                    System.out.println("added successor!");
                    pq.add(succ);
                }
            }
        }

        System.out.println("CBS: No solution found.");
        System.exit(0);
    }

    private boolean computePath(double time, Mobile mobile, CBSNode node) {
        boolean debug = false;

        this.initReverseResumableAStar(mobile);

        Vector3D startPosition = mobile.getPosition();
        Vector3D endPosition = mobile.getTargetPosition();

        HashMap<Vector3D, Double> dist = node.dist.get(mobile.getId());
        HashMap<Vector3D, Double> h = node.h.get(mobile.getId());
        HashMap<Vector3D, Vector3D> prev = node.prev.get(mobile.getId());

        dist.clear();
        h.clear();
        prev.clear();

        PriorityQueue<Pair<Vector3D, Double>> pq = new PriorityQueue<>(Comparator.comparing(Pair::getSecond));

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);

        dist.put(pair.second.first, pair.second.second);
        prev.put(pair.second.first, pair.first.first);
        Vector2D dist2D = this.reverseResumableAStar(mobile, pair.second.first, startPosition);
        h.put(pair.second.first, pair.second.second + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
        pq.add(new Pair<>(pair.second.first, h.get(pair.second.first)));

        if (debug) System.out.println("path for mobile " + mobile.getId());

        while (!pq.isEmpty()) {
            Pair<Vector3D, Double> p = pq.poll();

            Vector3D u = p.first;
            double estimateU = p.second;

            if (estimateU > h.get(u)) { // not the shortest path anymore
                continue;
            }

            if (debug) System.out.println("reached pos " + u);

            if (u.equals(endPosition)) {
                return true;
            }

            double distU = dist.get(u);

            for (Edge edge : this.graph.getEdges(u)) {
                Vector3D v = edge.to;
                Vector2D w = edge.weight;

                if (!edge.canCross(distU, mobile)) {
                    if (debug) System.out.println("cannot cross edge to pos " + v);
                    continue;
                }

                double edgeDist = DoublePrecisionConstraint.round(w.getX() * mobile.getSpeed() + w.getY() * Lift.speed);
                double otherDist = DoublePrecisionConstraint.round(distU + edgeDist);

                if (otherDist < time + W) { // check for collisions only within the time window
                    if (!node.table.isAvailable(v, otherDist, mobile.getId())) { // position already occupied at that time
                        if (debug) System.out.println("pos " + v + " is occupied at time " + otherDist);
                        otherDist = node.table.nextAvailability(v, otherDist, mobile.getId()); // get soonest available time
                        if (debug) System.out.println("soonest time to go there : " + otherDist);
                        if (!node.table.isAvailable(u, dist.get(u), DoublePrecisionConstraint.round(otherDist - edgeDist), mobile.getId())) {
                            if (debug) System.out.println("cannot wait long enough to reach " + v);
                            continue; // mobile cannot wait in current position until the next position is available
                        }
                    }
                }

                if (!dist.containsKey(v) || otherDist < dist.get(v)) {
                    dist2D = this.reverseResumableAStar(mobile, v, startPosition);
                    double estimateV = DoublePrecisionConstraint.round(otherDist + dist2D.getX() * mobile.getSpeed() + dist2D.getY() * Lift.speed);
                    dist.put(v, otherDist);
                    h.put(v, estimateV);
                    prev.put(v, u);
                    pq.add(new Pair<>(v, estimateV));
                }
            }
        }

        return false; // no path was found
    }

    private CBSConflict firstConflict(double time, CBSNode node) {
        this.table.clear();

        CBSConflict firstConflict = null;

        for (Mobile mobile : node.mobiles) {
            CBSConflict conflict = this.setAndCheckPath(time, mobile, node.dist.get(mobile.getId()), node.prev.get(mobile.getId()));
            if (conflict != null && (firstConflict == null || conflict.reservations.first.start < firstConflict.reservations.first.start)) {
                firstConflict = conflict;
            }
        }

        return firstConflict;
    }

    private CBSConflict setAndCheckPath(double time, Mobile mobile, HashMap<Vector3D, Double> dist, HashMap<Vector3D, Vector3D> prev) {
        CBSConflict firstConflict = null;

        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getPositionsAt(time);
        Vector3D startPosition = pair.second.first;
        Vector3D endPosition = mobile.getTargetPosition();

        ArrayList<Pair<Vector3D, Double>> path = new ArrayList<>();

        Vector3D u = endPosition;
        path.add(new Pair<>(u, dist.get(u)));
        while (!u.equals(startPosition)) {
            u = prev.get(u);
            path.add(0, new Pair<>(u, dist.get(u)));
        }

        if (!pair.first.first.equals(startPosition)) {
            path.add(0, new Pair<>(pair.first.first, pair.first.second));
        }

        // reserve path in table
        for (int i = path.size() - 1; i >= 0; i--) {
            u = path.get(i).first;
            double timeU = path.get(i).second;
            if (i < path.size() - 1) {
                Vector3D v = path.get(i + 1).first;
                double timeV = path.get(i + 1).second;
                Vector2D w = this.graph.getWeight(u, v);

                if (DoublePrecisionConstraint.round(timeV - timeU - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed) > 0) {
                    double timeLeaveU = DoublePrecisionConstraint.round(timeV - w.getX() * mobile.getSpeed() - w.getY() * Lift.speed);
                    path.add(i + 1, new Pair<>(u, timeLeaveU));
                    Reservation conflict = this.table.firstConflict(u, timeU, timeLeaveU, mobile.getId());
                    Reservation reservation = this.table.reserve(u, timeU, timeLeaveU, mobile.getId());
                    if (conflict != null) {
                        firstConflict = new CBSConflict(conflict, reservation);
                    }
                } else {
                    Reservation conflict = this.table.firstConflict(u, timeU, mobile.getId());
                    Reservation reservation = this.table.reserve(u, timeU, mobile.getId());
                    if (conflict != null) {
                        firstConflict = new CBSConflict(conflict, reservation);
                    }
                }
            } else {
                Reservation conflict = this.table.firstConflict(u, timeU + W, mobile.getId());
                Reservation reservation = this.table.reserve(u, timeU + W, mobile.getId());
                if (conflict != null) {
                    firstConflict = new CBSConflict(conflict, reservation);
                }
            }
        }

        mobile.setPath(time, path);

        return firstConflict;
    }

    private static class CBSNode implements Comparable<CBSNode> {

        public final ArrayList<Mobile> mobiles;
        public final ReservationTable table;
        public final HashMap<Integer, HashMap<Vector3D, Double>> dist;
        public final HashMap<Integer, HashMap<Vector3D, Double>> h;
        public final HashMap<Integer, HashMap<Vector3D, Vector3D>> prev;

        public double value;

        public CBSNode(ArrayList<Mobile> mobiles, ReservationTable table) {
            this.mobiles = mobiles;
            this.table = table;
            this.dist = new HashMap<>();
            this.h = new HashMap<>();
            this.prev = new HashMap<>();

            for (Mobile mobile : mobiles) {
                this.dist.put(mobile.getId(), new HashMap<>());
                this.h.put(mobile.getId(), new HashMap<>());
                this.prev.put(mobile.getId(), new HashMap<>());
            }

            this.value = -1;
        }

        public double getValue() {
            if (this.value == -1) this.computeValue();
            return this.value;
        }

        public void computeValue() {
            this.value = Double.MIN_VALUE;
            for (Mobile mobile : mobiles) {
                double endTime = this.dist.get(mobile.getId()).get(mobile.getTargetPosition());
                this.value = Math.max(this.value, endTime);
            }
        }

        public CBSNode clone() {
            CBSNode node = new CBSNode(this.mobiles, this.table.clone());

            for (Mobile mobile : this.mobiles) {
                node.dist.put(mobile.getId(), new HashMap<>(this.dist.get(mobile.getId())));
                node.h.put(mobile.getId(), new HashMap<>(this.h.get(mobile.getId())));
                node.prev.put(mobile.getId(), new HashMap<>(this.prev.get(mobile.getId())));
            }

            return node;
        }

        @Override
        public int compareTo(CBSNode other) {
            if (this.value != other.value) {
                return Double.compare(this.value, other.value);
            }

            if (this.table.reservations.size() != other.table.reservations.size()) {
                return this.table.reservations.size() - other.table.reservations.size();
            }

            for (Map.Entry<Vector3D, TreeSet<Reservation>> entry : this.table.reservations.entrySet()) {
                if (!other.table.reservations.containsKey(entry.getKey())) {
                    return 1;
                }

                TreeSet<Reservation> thisReservations = entry.getValue();
                TreeSet<Reservation> otherReservations = other.table.reservations.get(entry.getKey());

                if (thisReservations.size() != otherReservations.size()) {
                    return thisReservations.size() - otherReservations.size();
                }

                Iterator<Reservation> thisIterator = thisReservations.iterator();
                Iterator<Reservation> otherIterator = otherReservations.iterator();

                while (thisIterator.hasNext()) {
                    Reservation thisReservation = thisIterator.next();
                    Reservation otherReservation = otherIterator.next();

                    if (thisReservation.start != otherReservation.start) {
                        return Double.compare(thisReservation.start, otherReservation.start);
                    }

                    if (thisReservation.end != otherReservation.end) {
                        return Double.compare(thisReservation.end, otherReservation.end);
                    }

                    if (thisReservation.mobileId != otherReservation.mobileId) {
                        return Integer.compare(thisReservation.mobileId, otherReservation.mobileId);
                    }
                }
            }

            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CBSNode other = (CBSNode) o;

            if (this.value != other.value) {
                return false;
            }

            if (this.table.reservations.size() != other.table.reservations.size()) {
                return false;
            }

            for (Map.Entry<Vector3D, TreeSet<Reservation>> entry : this.table.reservations.entrySet()) {
                if (!other.table.reservations.containsKey(entry.getKey())) {
                    return false;
                }

                TreeSet<Reservation> thisReservations = entry.getValue();
                TreeSet<Reservation> otherReservations = other.table.reservations.get(entry.getKey());

                if (thisReservations.size() != otherReservations.size()) {
                    return false;
                }

                Iterator<Reservation> thisIterator = thisReservations.iterator();
                Iterator<Reservation> otherIterator = otherReservations.iterator();

                while (thisIterator.hasNext()) {
                    Reservation thisReservation = thisIterator.next();
                    Reservation otherReservation = otherIterator.next();

                    if (thisReservation.start != otherReservation.start) {
                        return false;
                    }

                    if (thisReservation.end != otherReservation.end) {
                        return false;
                    }

                    if (thisReservation.mobileId != otherReservation.mobileId) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    private static class CBSConflict {

        public final Pair<Reservation, Reservation> reservations;

        public CBSConflict(Reservation r1, Reservation r2) {
            if (r1.start <= r2.start) this.reservations = new Pair<>(r1, r2);
            else this.reservations = new Pair<>(r2, r1);
        }

    }

}
