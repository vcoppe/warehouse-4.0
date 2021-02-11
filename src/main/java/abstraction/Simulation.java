package abstraction;

import java.util.PriorityQueue;

public class Simulation {

    private PriorityQueue<Event> eventQueue = new PriorityQueue<Event>();
    private double currentTime = 0;

    public void run(double horizon) {

        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();

            if (event.getTime() <= horizon) {
                currentTime = event.getTime();
                Agent agent = event.getAgent();
                agent.act();
            } else {
                currentTime = horizon;
                return;
            }
        }

    }

    public void enqueueEvent(Event event) {
        eventQueue.add(event);
    }

    public double getCurrentTime() {
        return currentTime;
    }
}
