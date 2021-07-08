package agent;

import warehouse.Position;

public class Dock {

    private static int DOCK_ID = 0;
    private final int id;
    private final Position position;
    private Truck truck;

    public Dock(Position position) {
        this.id = DOCK_ID++;
        this.position = position;
        this.truck = null;
    }

    public int getId() {
        return this.id;
    }

    public Position getPosition() {
        return this.position;
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
