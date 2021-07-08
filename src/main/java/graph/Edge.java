package graph;

import agent.Mobile;
import util.Condition;
import util.ConjunctionCondition;
import warehouse.Position;

public class Edge implements Comparable<Edge> {

    protected Position from, to;
    protected double weight;

    ConjunctionCondition condition;

    public Edge(Position from, Position to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.condition = new ConjunctionCondition();
    }

    public Position to() {
        return this.to;
    }

    public double getWeight() {
        return this.weight;
    }

    @Override
    public int compareTo(Edge other) {
        return this.to.compareTo(other.to);
    }

    public void addCrossCondition(Condition condition) {
        this.condition.add(condition);
    }

    public boolean canCross(double time, Mobile mobile) {
        return this.condition.satisfied(time, mobile);
    }

}