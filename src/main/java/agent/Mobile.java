package agent;

import observer.Observable;
import pathfinding.Path;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;

import java.util.Random;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private static final double speed = 0.05; // in second per distance unit
    private static final double loadedSpeedFactor = 2; // time factor when loaded
    private final Vector3D chargingPosition;
    private Vector3D position, targetPosition;
    private double pathTime;
    private Mission mission;
    private Path path;
    private static final Random random = new Random(0);

    public Mobile(Vector3D position) {
        super();
        this.id = MOBILE_ID++;
        this.position = position;
        this.targetPosition = position;
        this.chargingPosition = position;
        this.mission = null;
    }

    public int getId() {
        return this.id;
    }

    public double getSpeed(boolean loaded) {
        if (loaded) {
            return speed * loadedSpeedFactor;
        } else {
            return speed;
        }
    }

    public double getSpeed() {
        if (this.mission == null || !this.mission.pickedUp()) {
            return this.getSpeed(false);
        } else {
            return this.getSpeed(true);
        }
    }

    public boolean isAvailable() {
        return this.mission == null;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public Vector3D getTargetPosition() {
        return this.targetPosition;
    }

    public Vector3D getChargingPosition() {
        return this.chargingPosition;
    }

    public Path getPath() {
        return this.path;
    }

    public double getPathEndTime() {
        if (this.path == null || this.path.isEmpty()) {
            return this.pathTime;
        }

        return this.path.getEndTimedPosition().second;
    }

    public double getPathTime() {
        return this.pathTime;
    }

    public Vector3D getCurrentPosition() {
        return this.getPositionAt(this.pathTime);
    }

    public Vector3D getPositionAt(double time) {
        if (this.path == null) {
            return this.position;
        } else {
            return this.path.getPositionAt(time);
        }
    }

    public Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> getTimedPositionsAt(double time) {
        if (this.path == null) {
            return new Pair<>(
                    new Pair<>(this.position, time - 1.0),
                    new Pair<>(this.position, time)
            );
        } else {
            return this.path.getTimedPositionsAt(time);
        }
    }

    public Mission getMission() {
        return this.mission;
    }

    public void start(Mission mission) {
        this.mission = mission;
        this.mission.start(this);
        this.targetPosition = mission.getStartPosition();
    }

    public void pickUp() {
        this.mission.pickUp();
        this.position = this.mission.getStartPosition();
        this.targetPosition = this.mission.getEndPosition();
    }

    public void drop() {
        this.mission.drop();
        this.position = this.mission.getEndPosition();
        this.mission = null;
        this.path = null;
        this.changed();
    }

    public void replace(Vector3D position) {
        this.targetPosition = position;
    }

    public void setPath(double time, Path path) {
        this.path = path;
        this.pathTime = time;
        this.changed();
    }

}
