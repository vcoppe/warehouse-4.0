package simulation;

import event.ControllerEvent;

public abstract class Event implements Comparable<Event> {

    private static int EVENT_ID = 0;
    private final int id;

    protected Simulation simulation;
    protected double time;

    public Event(Simulation simulation, double time) {
        this.id = EVENT_ID++;
        this.simulation = simulation;
        this.time = time;
    }

    public int compareTo(Event other) {
        if (this.time == other.time) {
            // enqueue ControllerEvents after all other events at the same time + enqueue only one
            if (this instanceof ControllerEvent && other instanceof ControllerEvent) return 0;
            if (this instanceof ControllerEvent) return 1;
            if (other instanceof ControllerEvent) return 0;
            return Integer.compare(this.id, other.id);
        }
        return Double.compare(this.time, other.time);
    }

    public double getTime() {
        return this.time;
    }

    public abstract void run();

}
