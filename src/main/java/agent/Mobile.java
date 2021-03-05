package agent;

import observer.Observable;
import warehouse.Position;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position, targetPosition;
    private final double speed;

    public Mobile(Position position) {
        super();
        this.id = MOBILE_ID++;
        this.position = position;
        this.targetPosition = position;
        this.speed = 10;
    }

    public int getId() {
        return this.id;
    }

    public double getSpeed() {
        return this.speed;
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
