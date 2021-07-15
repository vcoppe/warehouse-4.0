package agent;

import observer.Observable;
import util.Vector3D;

public class Lift extends Observable {

    public static double speed = 0.1; // in second per distance unit
    private static int LIFT_ID = 0;
    private final int id, height;
    private final Vector3D position;

    public Lift(Vector3D position, int height) {
        super();
        this.id = LIFT_ID++;
        this.position = position;
        this.height = height;
    }

    public int getId() {
        return this.id;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public int getHeight() {
        return this.height;
    }
}
