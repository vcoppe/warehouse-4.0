package abstraction;

public abstract class Event implements Comparable<Event> {

    protected double time;
    protected Agent agent;

    public int compareTo(Event other) {
        return Double.compare(time, other.time);
    }

    public double getTime() {
        return time;
    }

    public Agent getAgent() {
        return agent;
    }
}
