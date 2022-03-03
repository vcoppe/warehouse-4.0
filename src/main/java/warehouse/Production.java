package warehouse;

import agent.ProductionLine;
import util.Pair;

import java.util.ArrayList;

public class Production implements Comparable<Production> {

    private static int PRODUCTION_ID = 0;
    private final int id;
    private final ProductionLine productionLine;
    private final ArrayList<Pair<Pallet, Integer>> in, out;
    private final double duration, dueTime;
    private final int capacity;

    public Production(ProductionLine productionLine, ArrayList<Pair<Pallet, Integer>> in, ArrayList<Pair<Pallet, Integer>> out, double duration, int capacity, double dueTime) {
        this.id = PRODUCTION_ID++;
        this.productionLine = productionLine;
        this.in = in;
        this.out = out;
        this.duration = duration;
        this.capacity = capacity;
        this.dueTime = dueTime;
    }

    public int getId() {
        return this.id;
    }

    public ProductionLine getProductionLine() {
        return this.productionLine;
    }

    public ArrayList<Pair<Pallet, Integer>> getIn() {
        return this.in;
    }

    public ArrayList<Pair<Pallet, Integer>> getOut() {
        return this.out;
    }

    public double getDuration() {
        return this.duration;
    }

    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public int compareTo(Production other) {
        double latestStartTime = this.dueTime - this.duration;
        double otherLatestStartTime = other.dueTime - other.duration;
        if (latestStartTime == otherLatestStartTime) {
            return Integer.compare(this.id, other.id);
        }
        return Double.compare(latestStartTime, otherLatestStartTime);
    }

}
