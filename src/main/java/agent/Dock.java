package agent;

import warehouse.Position;

public class Dock {

    private static int DOCK_ID = 0;
    private final int id;
    private final Position position;

    public Dock(Position position) {
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
