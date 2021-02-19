package agent;

import warehouse.Position;

public class Mobile {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position;

    public Mobile(Position position) {
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
