package agent;

import observer.Observable;
import util.Vector3D;
import warehouse.Pallet;

import java.util.HashMap;

public class Truck extends Observable {

    private final Type type;
    private static int TRUCK_ID = 0;
    private final int id;
    private static final double speed = 50;

    public Truck(Type type, Vector3D position, HashMap<Vector3D, Pallet> toLoad, HashMap<Vector3D, Pallet> toUnload) {
        super();
        this.id = TRUCK_ID++;
        this.type = type;
        this.position = position;
        this.targetPosition = position;
        this.toLoad = toLoad;
        this.toUnload = toUnload;
        this.currentLoad = new HashMap<>(toUnload);
        this.arrivalTime = -1;
        this.calledTime = -1;
        this.departureTime = -1;
    }

    private Dock dock;
    private final HashMap<Vector3D, Pallet> toLoad, toUnload, currentLoad;
    private Vector3D position, targetPosition;
    private double arrivalTime, calledTime, departureTime;

    public Truck(Vector3D position, HashMap<Vector3D, Pallet> toLoad, HashMap<Vector3D, Pallet> toUnload) {
        this(Type.BACK, position, toLoad, toUnload);
    }

    public Type getType() {
        return this.type;
    }

    public int getId() {
        return this.id;
    }

    public enum Type {
        BACK,
        SIDES
    }

    public static double getSpeed() {
        return speed;
    }

    public HashMap<Vector3D, Pallet> getToLoad() {
        return this.toLoad;
    }

    public HashMap<Vector3D, Pallet> getToUnload() {
        return this.toUnload;
    }

    public HashMap<Vector3D, Pallet> getCurrentLoad() {
        return this.currentLoad;
    }

    public void arrive(double time) {
        this.arrivalTime = time;
    }

    public void go(double time, Dock dock) {
        this.calledTime = time;
        this.dock = dock;
        this.targetPosition = dock.getPosition();
        this.changed();
    }

    public void dock() {
        this.position = this.dock.getPosition();
        this.changed();
    }

    public void leave(double time) {
        this.dock = null;
        this.targetPosition = null;
        this.departureTime = time;
        this.changed();
    }

    public Dock getDock() {
        return this.dock;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public Vector3D getTargetPosition() {
        return this.targetPosition;
    }

    public void add(Vector3D position, Pallet pallet) {
        Vector3D positionInTruck = position.subtract(this.position);
        Pallet palletInTruck = this.toLoad.get(positionInTruck);
        if (pallet.getProduct() == palletInTruck.getProduct()) {
            this.toLoad.remove(positionInTruck);
            this.currentLoad.put(positionInTruck, pallet);
            this.changed();
        }
    }

    public void remove(Vector3D position, Pallet pallet) {
        Vector3D positionInTruck = position.subtract(this.position);
        Pallet palletInTruck = this.toUnload.get(positionInTruck);
        if (pallet.getProduct() == palletInTruck.getProduct()) {
            this.toUnload.remove(positionInTruck);
            if (pallet.getProduct() == this.currentLoad.get(positionInTruck).getProduct()) {
                this.currentLoad.remove(positionInTruck);
            }
            this.changed();
        }
    }

    public boolean done() {
        return (this.toUnload.size() + this.toLoad.size()) == 0;
    }

    public boolean left() {
        return this.departureTime != -1;
    }

    public double getArrivalTime() {
        return this.arrivalTime;
    }

    public double getCalledTime() {
        return this.calledTime;
    }

    public double getDepartureTime() {
        return this.departureTime;
    }

    public double getWaitingTime() {
        return this.departureTime - this.arrivalTime;
    }

}
