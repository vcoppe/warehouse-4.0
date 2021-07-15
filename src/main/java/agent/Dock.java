package agent;

import util.Vector3D;

public class Dock {

    private static int DOCK_ID = 0;
    private final int id;
    private final Vector3D position;
    private final Truck.Type type;
    private Truck truck;

    public Dock(Vector3D position, Truck.Type type) {
        this.id = DOCK_ID++;
        this.position = position;
        this.type = type;
        this.truck = null;
    }

    public int getId() {
        return this.id;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public Truck.Type getType() {
        return this.type;
    }

    public void call(double time, Truck truck) {
        this.truck = truck;
        this.truck.go(time, this);
    }

    public void dismiss(double time) {
        this.truck.leave(time);
        this.truck = null;
    }

    public Truck getTruck() {
        return this.truck;
    }

}
