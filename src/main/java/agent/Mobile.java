package agent;

import observer.Observable;
import warehouse.Mission;
import warehouse.Position;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position, targetPosition;
    private Mission mission;
    private final double speed;

    public Mobile(Position position) {
        super();
        this.id = MOBILE_ID++;
        this.position = position;
        this.targetPosition = position;
        this.mission = null;
        this.speed = 10;
    }

    public int getId() {
        return this.id;
    }

    public double getSpeed() {
        return this.speed;
    }

    public Position getPosition() {
        return this.position;
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

    public Mission getMission() {
        return this.mission;
    }

    public void start(Mission mission) {
        this.mission = mission;
        this.targetPosition = mission.getStartPosition();
        this.changed();
    }

    public void pickUp() {
        this.position = this.mission.getStartPosition();
        this.targetPosition = this.mission.getEndPosition();
        this.changed();
    }

    public void drop() {
        this.position = this.mission.getEndPosition();
        this.mission = null;
        this.changed();
    }

}
