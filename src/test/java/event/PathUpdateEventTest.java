package event;

import agent.Controller;
import agent.Mobile;
import junit.framework.TestCase;
import simulation.Simulation;
import util.Pair;
import warehouse.Configuration;
import warehouse.Mission;
import warehouse.Position;

import java.util.ArrayList;

public class PathUpdateEventTest extends TestCase {

    private Configuration configuration;
    private Simulation simulation;
    private Controller controller;
    private Mobile mobile;

    public void setUp() throws Exception {
        super.setUp();

        PathFinderEvent.reset();
        PathUpdateEvent.reset();

        this.configuration = new Configuration(1, 1);
        this.simulation = this.configuration.simulation;
        this.controller = this.configuration.controller;
        this.mobile = this.configuration.mobiles.get(0);

        PathUpdateEvent.enqueue(this.configuration.simulation, 10, this.configuration.controller);
    }

    public void testTriggerPathFinderEvent() {
        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.nextEvent().run();
        this.simulation.removeEvent(this.simulation.nextEvent());
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof PathFinderEvent);
    }

    public void testTriggerMobileMissionPickUpEvent() {
        this.mobile.start(new Mission(0, null, new Position(60, 60), new Position(100,100)));

        ArrayList<Pair<Position,Double>> path = new ArrayList<>();
        path.add(new Pair<>(this.mobile.getCurrentPosition(), 0.0));
        path.add(new Pair<>(this.mobile.getTargetPosition(), 10.0));
        this.mobile.setPath(0, path);

        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.nextEvent().run();
        this.simulation.removeEvent(this.simulation.nextEvent());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionPickUpEvent);
    }

    public void testTriggerMobileMissionEndEvent() {
        this.mobile.start(new Mission(0, null, new Position(60, 60), new Position(100,100)));
        this.mobile.pickUp();

        ArrayList<Pair<Position,Double>> path = new ArrayList<>();
        path.add(new Pair<>(this.mobile.getCurrentPosition(), 0.0));
        path.add(new Pair<>(this.mobile.getTargetPosition(), 10.0));
        this.mobile.setPath(0, path);
        this.mobile.forward(10.0);

        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.nextEvent().run();
        this.simulation.removeEvent(this.simulation.nextEvent());
        assertEquals(2, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof MobileMissionEndEvent);
    }
}