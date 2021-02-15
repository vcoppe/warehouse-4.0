package agent;

import simulation.Agent;
import simulation.Simulation;
import warehouse.Position;

public class Mobile extends Agent {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position;

    public Mobile(Simulation simulation, Position position) {
        super(simulation);
        this.id = MOBILE_ID++;
        this.position = position;
    }

    public int getId() {
        return this.id;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

}
