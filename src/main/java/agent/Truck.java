package agent;

import observer.Observable;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Truck extends Observable {

    private static int TRUCK_ID = 0;
    private final int id;
    private static final double speed = 50;
    private Dock dock;
    private Position position, targetPosition;
    private final HashMap<Position, Pallet> toLoad, toUnload, currentLoad;
    private double arrivalTime, calledTime, departureTime;

    public Truck(Position position, HashMap<Position, Pallet> toLoad, HashMap<Position, Pallet> toUnload) {
        super();
        this.id = TRUCK_ID++;
        this.position = position;
        this.targetPosition = position;
        this.toLoad = toLoad;
        this.toUnload = toUnload;
        this.currentLoad = new HashMap<>(toUnload);
        this.arrivalTime = -1;
        this.calledTime = -1;
        this.departureTime = -1;
    }

    public int getId() {
        return this.id;
    }

    public static double getSpeed() {
        return speed;
    }

    public HashMap<Position, Pallet> getToLoad() {
        return this.toLoad;
    }

    public HashMap<Position, Pallet> getToUnload() {
         return this.toUnload;
    }

    public HashMap<Position, Pallet> getCurrentLoad() {
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

    public Position getPosition() {
        return this.position;
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

    public void add(Position position, Pallet pallet) {
        Position positionInTruck = position.subtract(this.position);
        Pallet palletInTruck = this.toLoad.get(positionInTruck);
        if (pallet.getType() == palletInTruck.getType()) {
            this.toLoad.remove(positionInTruck);
            this.currentLoad.put(positionInTruck, pallet);
            this.changed();
        }
    }

    public void remove(Position position, Pallet pallet) {
        Position positionInTruck = position.subtract(this.position);
        Pallet palletInTruck = this.toUnload.get(positionInTruck);
        if (pallet.getType() == palletInTruck.getType()) {
            this.toUnload.remove(positionInTruck);
            if (pallet.getType() == this.currentLoad.get(positionInTruck).getType()) {
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
