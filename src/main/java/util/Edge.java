package util;

import java.util.ArrayList;
import java.util.Collections;

public class Edge implements Comparable<Edge> {

    private final static double margin = 5;

    int to;
    double w;
    private final ArrayList<Reservation> reservations;

    public Edge(int to, double w) {
        this.to = to;
        this.w = w;
        this.reservations = new ArrayList<>();
    }

    public double getWeight() {
        return this.w;
    }

    public void reserve(double time) {
        this.reserveWithMargin(time - margin, time + margin);
    }

    public void reserve(double start, double end) {
        this.reserveWithMargin(start - margin, end + margin);
    }

    private void reserveWithMargin(double start, double end) {
        Reservation reservation = new Reservation(start, end);
        int index = Collections.binarySearch(this.reservations, reservation);
        if (index < 0) {
            index = -index - 1;
            this.reservations.add(index, reservation);
        }
    }

    public boolean isAvailable(double time) {
        return this.isAvailableWithMargin(time - margin, time - margin);
    }

    public boolean isAvailable(double start, double end) {
        return this.isAvailableWithMargin(start - margin, end - margin);
    }

    private boolean isAvailableWithMargin(double start, double end) {
        Reservation key = new Reservation(start, end);
        int index = Collections.binarySearch(this.reservations, key);
        if (index >= 0) { // found an exact match
            return false;
        }

        index = -index - 1;
        if (index > 0) {
            Reservation before = this.reservations.get(index - 1);
            if (before.end > start) {
                return false;
            }
        }

        if (index < this.reservations.size()) {
            Reservation after = this.reservations.get(index);
            if (after.start < end) {
                return false;
            }
        }

        return true;
    }

    public double nextAvailability(double time) {
        double nextTime = this.nextAvailabilityWithMargin(time - margin, 2 * margin);
        return nextTime + margin;
    }

    public double nextAvailability(double from, double duration) {
        double nextTime = this.nextAvailabilityWithMargin(from - margin, duration + 2 * margin);
        return nextTime + margin;
    }

    private double nextAvailabilityWithMargin(double from, double duration) {
        if (this.reservations.isEmpty()) {
            return from;
        }

        Reservation key = new Reservation(from, from + duration);
        int index = Collections.binarySearch(this.reservations, key);
        if (index < 0) {
            index = -index - 1;
        }

        for (int i = index; i < this.reservations.size(); i++) { // check if possible to put between 2 reservations
            if (i - 1 >= 0) {
                Reservation before = this.reservations.get(i - 1);
                Reservation after = this.reservations.get(i);

                if (after.start - Math.max(before.end, from) >= duration) {
                    return Math.max(before.end, from);
                }
            }
        }

        return this.reservations.get(this.reservations.size() - 1).end;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.to, other.to);
    }

    static class Reservation implements Comparable<Reservation> {

        double start, end;

        public Reservation(double start, double end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(Reservation other) {
            if (this.start == other.start) {
                return Double.compare(this.end, other.end);
            }
            return Double.compare(this.start, other.start);
        }
    }

}