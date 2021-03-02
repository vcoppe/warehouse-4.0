package agent;

import observer.Observable;
import warehouse.Position;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position, targetPosition;

    public Mobile(Position position) {
        super();
        this.id = MOBILE_ID++;
        this.position = position;
        this.targetPosition = position;
    }

    public int getId() {
        return this.id;
    }

    public void setPosition(Position position) {
        this.position = position;
        this.changed();
    }

    public Position getPosition() {
        return this.position;
    }

    public void setTargetPosition(Position position) {
        this.targetPosition = position;
        this.changed();
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

}
