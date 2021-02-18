package simulation;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Simulation {

    public final Logger logger;
    private final TreeSet<Event> eventQueue;
    private double currentTime;

    public Simulation(Level level) {
        this.logger = Logger.getLogger("");
        this.logger.setLevel(level);
        this.logger.getHandlers()[0].setLevel(level);

        this.eventQueue = new TreeSet<>();
        this.currentTime = 0;
    }

    public void run(double horizon) {

        while (!this.eventQueue.isEmpty()) {
            Event event = this.eventQueue.pollFirst();

            if (event.getTime() <= horizon) {
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
}
