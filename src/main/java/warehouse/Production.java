package warehouse;

import util.Pair;

import java.util.ArrayList;

public class Production implements Comparable<Production> {

    private static int PRODUCTION_ID = 0;
    private final int id;
    private final ArrayList<Pair<Pallet,Integer>> in, out;
    private final double time, dueTime;
    private final int capacity;

    public Production(ArrayList<Pair<Pallet,Integer>> in, ArrayList<Pair<Pallet,Integer>> out, double time, int capacity, double dueTime) {
        this.id = PRODUCTION_ID++;
        this.in = in;
        this.out = out;
        this.time = time;
        this.capacity = capacity;
        this.dueTime = dueTime;
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<Pair<Pallet,Integer>> getIn() {
        return this.in;
    }

    public ArrayList<Pair<Pallet,Integer>> getOut() {
        return this.out;
    }

    public double getTime() {
        return this.time;
    }

    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int compareTo(Production other) {
        double latestStartTime = this.dueTime - this.time;
        double otherLatestStartTime = other.dueTime - other.time;
        if (latestStartTime == otherLatestStartTime) {
            return Integer.compare(this.id, other.id);
        }
        return Double.compare(latestStartTime, otherLatestStartTime);
    }

}
