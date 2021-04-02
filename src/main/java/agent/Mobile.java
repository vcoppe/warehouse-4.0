package agent;

import observer.Observable;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;

import java.util.List;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position, targetPosition;
    private List<Pair<Position, Double>> path;
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

    public List<Pair<Position, Double>> getPath() {
        return this.path;
    }

    public Mission getMission() {
        return this.mission;
    }

    public void start(Mission mission, List<Pair<Position, Double>> path) {
        this.mission = mission;
        this.targetPosition = mission.getStartPosition();
        this.path = path;
        this.changed();
    }

    public void pickUp(List<Pair<Position, Double>> path) {
        this.position = this.mission.getStartPosition();
        this.targetPosition = this.mission.getEndPosition();
        this.path = path;
        this.changed();
    }

    public void drop() {
        this.position = this.mission.getEndPosition();
        this.path = null;
        this.mission = null;
        this.changed();
    }

}
