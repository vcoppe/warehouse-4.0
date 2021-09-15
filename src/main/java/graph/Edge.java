package graph;

import agent.Mobile;
import util.Condition;
import util.ConjunctionCondition;
import util.Vector2D;
import util.Vector3D;

public class Edge implements Comparable<Edge> {

    protected Vector3D from, to;
    protected Vector2D weight;

    ConjunctionCondition condition;

    public Edge(Vector3D from, Vector3D to) {
        this.from = from;
        this.to = to;
        this.weight = from.manhattanDistance3D(to);
        this.condition = new ConjunctionCondition();
    }

    public Vector3D from() {
        return this.from;
    }

    public Vector3D to() {
        return this.to;
    }

    public Vector2D getWeight() {
        return this.weight;
    }

    @Override
    public int compareTo(Edge other) {
        return this.to.compareTo(other.to);
    }

    public void addCrossCondition(Condition condition) {
        this.condition.add(condition);
    }

    public boolean canCross(Mobile mobile) {
        return this.condition.satisfied(mobile);
    }

}