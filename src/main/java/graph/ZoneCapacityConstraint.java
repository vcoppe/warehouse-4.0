package graph;

import util.Pair;
import util.Vector3D;

import java.util.*;

public class ZoneCapacityConstraint extends GraphConstraint {

    int capacity;

    // TODO create meta reservation tree

    public ZoneCapacityConstraint(Vector3D[] positions, int capacity) {
        super(positions);
        this.capacity = capacity;
    }

    @Override
    public Reservation firstConflictWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        /*if (!reservationTable.isAvailableWithMarginHelper(position, start, end, id)) {
            return false;
        }

        // compute span for each mobile in zone
        HashMap<Integer, Reservation> mobileSpan = new HashMap<>();

        for (int i = 0; i < this.positions.length; i++) {
            if (!reservationTable.reservations.containsKey(this.positions[i])) continue;
            for (Reservation reservation : reservationTable.reservations.get(this.positions[i])) {
                if (reservation.mobileId != id && reservation.start < end && reservation.end > start) {
                    Reservation span = mobileSpan.getOrDefault(reservation.mobileId, new Reservation(position, Double.MAX_VALUE, Double.MIN_VALUE, reservation.mobileId));
                    span.start = Math.min(span.start, reservation.start);
                    span.end = Math.max(span.end, reservation.end);
                    mobileSpan.put(reservation.mobileId, span);
                }
                if (reservation.start >= end) break;
            }
        }

        if (mobileSpan.size() < this.capacity) {
            return true;
        }

        // compute rolling number of mobiles
        ArrayList<Pair<Double, Boolean>> changes = new ArrayList<>();
        for (Reservation reservation : mobileSpan.values()) {
            changes.add(new Pair<>(reservation.start, true));
            changes.add(new Pair<>(reservation.end, false));
        }

        changes.sort(Comparator.comparing(Pair::getFirst));

        int cumul = 0;

        for (Pair<Double, Boolean> change : changes) {
            if (change.second) cumul++;
            else cumul--;

            if (cumul == this.capacity) {
                return false;
            }
        }

        return true;*/

        Reservation positionConflict = reservationTable.firstConflictWithMarginHelper(position, start, end, id);

        HashSet<Integer> conflictMobileIds = new HashSet<>();
        TreeSet<Reservation> conflicts = new TreeSet<>();

        for (int i = 0; i < this.positions.length; i++)
            if (reservationTable.reservations.containsKey(this.positions[i])) {
                for (Reservation reservation : reservationTable.reservations.get(this.positions[i])) {
                    if (reservation.start > end) break;
                    if (reservation.end > start && reservation.start < end && reservation.mobileId != id) {
                        conflicts.add(reservation);
                        conflictMobileIds.add(reservation.mobileId);
                    }
                }
            }

        if (positionConflict != null || conflictMobileIds.size() >= capacity) {
            if (positionConflict == null) {
                return conflicts.first();
            } else if (conflicts.isEmpty()) {
                return positionConflict;
            } else {
                Reservation zoneConflict = conflicts.first();
                if (positionConflict.start <= zoneConflict.start) {
                    return positionConflict;
                } else {
                    return zoneConflict;
                }
            }
        }

        return null;
    }

    /*@Override
    public void reserveWithMargin(ReservationTable reservationTable, Vector3D position, double start, double end, int id) {
        reservationTable.reserveWithMarginHelper(position, start, end, id);
    }*/

    @Override
    public double nextAvailabilityWithMargin(ReservationTable reservationTable, Vector3D position, double from, double duration, int id) {
        // compute span for each mobile in zone
        HashMap<Integer, Reservation> mobileSpan = new HashMap<>();

        for (int i = 0; i < this.positions.length; i++) {
            if (!reservationTable.reservations.containsKey(this.positions[i])) continue;
            for (Reservation reservation : reservationTable.reservations.get(this.positions[i])) {
                if (reservation.mobileId != id) {
                    Reservation span = mobileSpan.getOrDefault(reservation.mobileId, new Reservation(position, Double.MAX_VALUE, Double.MIN_VALUE, reservation.mobileId));
                    span.start = Math.min(span.start, reservation.start);
                    span.end = Math.max(span.end, reservation.end);
                    mobileSpan.put(reservation.mobileId, span);
                }
            }
        }

        if (mobileSpan.size() < this.capacity) {
            return reservationTable.nextAvailabilityWithMarginHelper(position, from, duration, id);
        }

        // compute rolling number of mobiles
        ArrayList<Pair<Double, Boolean>> changes = new ArrayList<>();
        for (Reservation reservation : mobileSpan.values()) {
            changes.add(new Pair<>(reservation.start, true));
            changes.add(new Pair<>(reservation.end, false));
        }

        changes.sort(Comparator.comparing(Pair::getFirst));

        double start = from;
        int cumul = 0;

        for (Pair<Double, Boolean> change : changes) {
            if (cumul < this.capacity && change.first - start >= duration) {
                if (reservationTable.isAvailableWithMarginWithMarginHelper(position, start, start + duration, id)) {
                    return start;
                } else {
                    double otherStart = reservationTable.nextAvailabilityWithMarginHelper(position, start, duration, id);
                    if (otherStart + duration <= change.first) {
                        return otherStart;
                    }
                }
            }

            if (change.second) cumul++;
            else cumul--;

            if (cumul == this.capacity - 1) {
                start = Math.max(from, change.first);
            }
        }

        return reservationTable.nextAvailabilityWithMarginHelper(position, start, duration, id);
    }
}
