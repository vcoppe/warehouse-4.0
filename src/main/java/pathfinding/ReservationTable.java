package pathfinding;

import warehouse.Position;

import java.util.HashMap;
import java.util.TreeSet;

public class ReservationTable {

    public final static double timeMargin = 2;

    private final HashMap<Position, TreeSet<Reservation>> reservations;

    public ReservationTable() {
        this.reservations = new HashMap<>();
    }

    public void clear() {
        this.reservations.clear();
    }

    public void reserve(Position position, double time, int id) {
        this.reserveWithMargin(position, time - timeMargin, time + timeMargin, id);
    }

    public void reserve(Position position, double start, double end, int id) {
        this.reserveWithMargin(position, start - timeMargin, end + timeMargin, id);
    }

    private void reserveWithMargin(Position position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            this.reservations.put(position, new TreeSet<>());
        }
        Reservation reservation = new Reservation(start, end, id);
        this.reservations.get(position).add(reservation);
    }

    public boolean isAvailable(Position position, double time, int id) {
        return this.isAvailableWithMargin(position, time - timeMargin, time + timeMargin, id);
    }

    public boolean isAvailable(Position position, double start, double end, int id) {
        return this.isAvailableWithMargin(position, start - timeMargin, end + timeMargin, id);
    }

    private boolean isAvailableWithMargin(Position position, double start, double end, int id) {
        if (!this.reservations.containsKey(position)) {
            return true;
        }

        Reservation key = new Reservation(start, end, id);
        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        if (before != null && before.mobileId != id && (before.start == start || before.end > start)) {
            return false;
        }

        if (after != null && after.mobileId != id && after.start < end) {
            return false;
        }

        return true;
    }

    public double nextAvailability(Position position, double time, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, time - timeMargin, 2 * timeMargin, id);
        return nextTime + timeMargin;
    }

    public double nextAvailability(Position position, double from, double duration, int id) {
        double nextTime = this.nextAvailabilityWithMargin(position, from - timeMargin, duration + 2 * timeMargin, id);
        return nextTime + timeMargin;
    }

    private double nextAvailabilityWithMargin(Position position, double from, double duration, int id) {
        if (!this.reservations.containsKey(position) || this.reservations.get(position).isEmpty()) {
            return from;
        }

        Reservation key = new Reservation(from, from + duration, id);

        Reservation before = this.reservations.get(position).floor(key);
        Reservation after = this.reservations.get(position).higher(key);

        while (true) {
            while (after != null && after.mobileId == id) {
                after = this.reservations.get(position).higher(after);
            }

            if (before == null || before.mobileId == id) {
                if (after == null || after.start - from >= duration) {
                    return from;
                }
            } else if (after == null || after.start - Math.max(before.end, from) >= duration) {
                return Math.max(before.end, from);
            }

            before = after;
            after = this.reservations.get(position).higher(after);
        }
    }
}
