package simulation;

public abstract class Event implements Comparable<Event> {

    private static int EVENT_ID = 0;
    protected final int id;
    protected Simulation simulation;
    protected double time;

    public Event(Simulation simulation, double time, int id) {
        this.id = id;
        this.simulation = simulation;
        this.time = time;
    }

    public Event(Simulation simulation, double time) {
        this(simulation, time, EVENT_ID++);
    }

    public int compareTo(Event other) {
        if (this.time == other.time) {
            return Integer.compare(this.id, other.id);
        }
        return Double.compare(this.time, other.time);
    }

    public double getTime() {
        return this.time;
    }

    public abstract void run();

}
