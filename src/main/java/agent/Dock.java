package agent;

import simulation.Agent;
import simulation.Simulation;
import warehouse.Position;

public class Dock extends Agent {

    private static int DOCK_ID = 0;
    private final int id;
    private final Position position;

    public Dock(Simulation simulation, Position position) {
        super(simulation);
        this.id = DOCK_ID++;
        this.position = position;
    }

    public int getId() {
        return this.id;
    }

    public Position getPosition() {
        return this.position;
    }

}
