package agent;

import observer.Observable;
import util.DoublePrecisionConstraint;
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

    public double getPathTime() {
        return this.pathTime;
    }

    public Position getCurrentPosition() {
        Pair<Pair<Position,Double>,Pair<Position,Double>> pair = this.getCurrentPositions();

        double alpha = pair.second.second / (pair.second.second - pair.first.second);
        return new Position(
                (int) (alpha * pair.first.first.getX() + (1 - alpha) * pair.second.first.getX()),
                (int) (alpha * pair.first.first.getY() + (1 - alpha) * pair.second.first.getY()),
                (int) (alpha * pair.first.first.getZ() + (1 - alpha) * pair.second.first.getZ())
        );
    }

    public Pair<Pair<Position,Double>, Pair<Position,Double>> getCurrentPositions() {
        if (this.path == null || this.path.size() == 1) {
            return new Pair<>(
                    new Pair<>(this.position, -1.0),
                    new Pair<>(this.position, 0.0)
            );
        }

        for (int i=0; i<this.path.size(); i++) {
            Pair<Position,Double> current = this.path.get(i);
            if (this.pathTime <= current.second) {
                if (i == 0) {
                    return new Pair<>(
                            new Pair<>(current.first, -1.0),
                            new Pair<>(current.first, 0.0)
                    );
                }
                Pair<Position,Double> previous = this.path.get(i-1);
                return new Pair<>(
                        new Pair<>(previous.first, DoublePrecisionConstraint.round(previous.second - this.pathTime)),
                        new Pair<>(current.first, DoublePrecisionConstraint.round(current.second - this.pathTime))
                );
            }
        }

        return null;
    }

    public Mission getMission() {
        return this.mission;
    }

    public void forward(double delta) {
        this.pathTime = DoublePrecisionConstraint.round(this.pathTime + delta);
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
        this.changed();
    }

    public void setPath(ArrayList<Pair<Position,Double>> path) {
        this.path = path;
        this.pathTime = 0;
        this.changed();
    }

}
