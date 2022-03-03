package agent;

import observer.Observable;
import util.DoublePrecisionConstraint;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;

import java.util.ArrayList;
import java.util.Random;

public class Mobile extends Observable {

    private static int MOBILE_ID = 0;
    private final int id;
    private static final double speed = 0.05; // in second per distance unit
    private Vector3D position, targetPosition;
    private double pathTime;
    private Mission mission;
    private ArrayList<Pair<Vector3D, Double>> path;
    private static final Random random = new Random(0);

    public Mobile(Vector3D position) {
        super();
        this.id = MOBILE_ID++;
        this.position = position;
        this.targetPosition = position;
        this.mission = null;
    }

    public int getId() {
        return this.id;
    }

    public static double getSpeed() {
        return speed;
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

    public ArrayList<Pair<Vector3D, Double>> getPath() {
        return this.path;
    }

    public double getPathEndTime() {
        if (this.path == null || this.path.isEmpty()) {
            return this.pathTime;
        }

        return this.path.get(this.path.size() - 1).second;
    }

    public double getPathTime() {
        return this.pathTime;
    }

    public Vector3D getCurrentPosition() {
        return this.getPositionAt(this.pathTime);
    }

    public Vector3D getPositionAt(double time) {
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = this.getPositionsAt(time);

        double alpha = (pair.second.second - time) / (pair.second.second - pair.first.second);
        return new Vector3D(
                (int) (alpha * pair.first.first.getX() + (1 - alpha) * pair.second.first.getX()),
                (int) (alpha * pair.first.first.getY() + (1 - alpha) * pair.second.first.getY()),
                (int) (alpha * pair.first.first.getZ() + (1 - alpha) * pair.second.first.getZ())
        );
    }

    public Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> getCurrentPositions() {
        return this.getPositionsAt(this.pathTime);
    }

    public Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> getPositionsAt(double time) {
        if (this.path == null || this.path.size() == 1) {
            return new Pair<>(
                    new Pair<>(this.position, time - 1.0),
                    new Pair<>(this.position, time)
            );
        }

        for (int i = 0; i < this.path.size(); i++) {
            Pair<Vector3D, Double> current = this.path.get(i);
            if (time <= current.second) {
                if (i == 0) {
                    return new Pair<>(
                            new Pair<>(current.first, time - 1.0),
                            new Pair<>(current.first, time)
                    );
                }
                Pair<Vector3D, Double> previous = this.path.get(i - 1);
                return new Pair<>(
                        new Pair<>(previous.first, DoublePrecisionConstraint.round(previous.second)),
                        new Pair<>(current.first, DoublePrecisionConstraint.round(current.second))
                );
            }
        }

        Pair<Vector3D, Double> last = this.path.get(this.path.size() - 1);
        return new Pair<>(
                new Pair<>(last.first, time - 1.0),
                new Pair<>(last.first, time)
        );
    }

    public Mission getMission() {
        return this.mission;
    }

    public void forward(double time) {
        this.pathTime = time;
        if (this.mission == null && this.path != null) {
            Pair<Vector3D, Double> last = this.path.get(this.path.size() - 1);
            if (time >= last.second) {
                this.position = last.first;
                this.targetPosition = last.first;
                this.path = null;
            }
        }
    }

    public void start(Mission mission) {
        this.mission = mission;
        this.mission.start();
        this.targetPosition = mission.getStartPosition();
    }

    public boolean atPickUp() {
        if (this.path != null && this.path.size() > 0){
            Pair<Vector3D, Double> pair = this.path.get(this.path.size() - 1);
            return this.targetPosition.equals(this.mission.getStartPosition()) &&
                    pair.first.equals(this.mission.getStartPosition()) &&
                    this.pathTime >= pair.second;
        }
        return false;
    }

    public void pickUp() {
        this.mission.pickUp();
        this.position = this.mission.getStartPosition();
        this.targetPosition = this.mission.getEndPosition();
    }

    public boolean atDrop() {
        if (this.path != null && this.path.size() > 0){
            Pair<Vector3D, Double> pair = this.path.get(this.path.size() - 1);
            return this.targetPosition.equals(this.mission.getEndPosition()) &&
                    pair.first.equals(this.mission.getEndPosition()) &&
                    this.pathTime >= pair.second;
        }
        return false;
    }

    public void drop() {
        this.mission.drop();
        this.position = this.mission.getEndPosition();
        this.mission = null;
        this.path = null;
        this.changed();
    }

    public void interrupt(double time) {
        this.mission = null;
        Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = this.getPositionsAt(time);
        this.path = new ArrayList<>();
        this.path.add(pair.first);
        this.path.add(pair.second);
        this.pathTime = time;
        this.position = pair.first.first;
        this.targetPosition = pair.second.first;
        this.changed();
    }

    public void replace(Vector3D position) {
        this.targetPosition = position;
    }

    public void setPath(double time, ArrayList<Pair<Vector3D, Double>> path) {
        this.path = path;
        this.pathTime = time;
        this.changed();
    }

}
