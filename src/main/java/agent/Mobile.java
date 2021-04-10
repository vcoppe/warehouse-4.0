package agent;

import observer.Observable;
import util.Pair;
import warehouse.Mission;
import warehouse.Position;

import java.util.ArrayList;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private Position position, targetPosition;
    private ArrayList<Pair<Position, Double>> path;
    private double pathTime;
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

    public boolean isAvailable() {
        return this.mission == null;
    }

    public Position getPosition() {
        return this.position;
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

    public ArrayList<Pair<Position, Double>> getPath() {
        return this.path;
    }

    public Position getCurrentPosition() {
        Position position = this.position;
        if (this.path != null) {
            for (Pair<Position, Double> pair : this.path) {
                if (this.pathTime >= pair.second) {
                    position = pair.first;
                } else {
                    break;
                }
            }
        }
        return position;
    }

    public Mission getMission() {
        return this.mission;
    }

    public void forward(double delta) {
        this.pathTime += delta;
    }

    public void start(Mission mission) {
        this.mission = mission;
        this.targetPosition = mission.getStartPosition();
    }

    public boolean atPickUp() {
        if (this.path != null && this.path.size() > 0){
            Pair<Position,Double> pair = this.path.get(this.path.size()-1);
            return this.targetPosition.equals(this.mission.getStartPosition()) &&
                    pair.first.equals(this.mission.getStartPosition()) &&
                    this.pathTime >= pair.second;
        }
        return false;
    }

    public void pickUp() {
        this.position = this.mission.getStartPosition();
        this.targetPosition = this.mission.getEndPosition();
    }

    public boolean atDrop() {
        if (this.path != null && this.path.size() > 0){
            Pair<Position,Double> pair = this.path.get(this.path.size()-1);
            return this.targetPosition.equals(this.mission.getEndPosition()) &&
                    pair.first.equals(this.mission.getEndPosition()) &&
                    this.pathTime >= pair.second;
        }
        return false;
    }

    public void drop() {
        this.position = this.mission.getEndPosition();
        this.mission = null;
        this.path = null;
    }

    public void setPath(ArrayList<Pair<Position,Double>> path) {
        this.path = path;
        this.pathTime = 0;
        this.changed();
    }

}
