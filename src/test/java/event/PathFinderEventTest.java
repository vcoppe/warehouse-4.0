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

public class PathFinderEventTest extends TestCase {

    private Configuration configuration;
    private Simulation simulation;
    private Controller controller;
    private Mobile mobile;

    public void setUp() throws Exception {
        super.setUp();

        PathFinderEvent.reset();
        PathUpdateEvent.reset();

        this.configuration = new Configuration();
        this.simulation = this.configuration.simulation;
        this.controller = this.configuration.controller;
        this.mobile = this.configuration.mobiles.get(0);

        PathFinderEvent.enqueue(this.configuration.simulation, 0, this.configuration.controller);
    }

    public void testNoTriggerIfNoMovingMobiles() {
        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.run(0);
        assertEquals(0, this.configuration.simulation.queueSize());
    }

    public void testTriggerPathUpdateEvent() {
        this.controller.remove(this.mobile);

        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.run(0);
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof PathUpdateEvent);
    }

    public void testComputePath() {
        this.mobile.start(new Mission(0, null, new Position(60, 60), new Position(100,100)));
        this.controller.remove(this.mobile);

        assertEquals(1, this.configuration.simulation.queueSize());
        this.simulation.run(0);
        assertEquals(1, this.configuration.simulation.queueSize());
        assertTrue(this.configuration.simulation.nextEvent() instanceof PathUpdateEvent);

        ArrayList<Pair<Position,Double>> path = this.mobile.getPath();
        assertEquals(this.mobile.getPosition(), path.get(0).first);
        assertEquals(this.mobile.getTargetPosition(), path.get(path.size()-1).first);
    }
}