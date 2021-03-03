package simulation;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Simulation {

    public final Logger logger;
    private final TreeSet<Event> eventQueue;
    private double currentTime;

    public Simulation(Level level) {
        this.logger = Logger.getLogger("warehouse");
        this.logger.setLevel(level);

        this.eventQueue = new TreeSet<>();
        this.currentTime = 0;
    }

    public void run(double horizon) {

        while (this.hasNextEvent()) {
            Event event = this.eventQueue.first();

            if (event.getTime() <= horizon) {
                this.eventQueue.pollFirst();
                this.currentTime = event.getTime();
                event.run();
            } else {
                this.currentTime = horizon;
                return;
            }
        }

    }

    public void enqueueEvent(Event event) {
        this.eventQueue.add(event);
    }

    public double getCurrentTime() {
        return this.currentTime;
    }

    public int queueSize() {
        return this.eventQueue.size();
    }

    public Event nextEvent() {
        return this.eventQueue.first();
    }

    public boolean hasNextEvent() {
        return !this.eventQueue.isEmpty();
    }
}
