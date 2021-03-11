package agent;

import observer.Observable;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class Truck extends Observable {

    private static int TRUCK_ID = 0;
    private final int id;
    private static final double speed = 50;
    private Dock dock;
    private Position position, targetPosition;
    private final ArrayList<Pair<Position, Pallet>> toLoad, toUnload, currentLoad;
    private double arrivalTime, departureTime;

    public Truck(Dock dock, Position position, ArrayList<Pair<Position, Pallet>> toLoad, ArrayList<Pair<Position, Pallet>> toUnload) {
        super();
        this.id = TRUCK_ID++;
        this.dock = dock;
        this.position = position;
        this.targetPosition = position;
        this.toLoad = toLoad;
        this.toUnload = toUnload;
        this.currentLoad = new ArrayList<>(toUnload);
    }

    public Truck(Position position, ArrayList<Pair<Position, Pallet>> toLoad, ArrayList<Pair<Position, Pallet>> toUnload) {
        this(null, position, toLoad, toUnload);
    }

    public int getId() {
        return this.id;
    }

    public static double getSpeed() {
        return speed;
    }

    public ArrayList<Pair<Position, Pallet>> getToLoad() {
        return this.toLoad;
    }

    public ArrayList<Pair<Position, Pallet>> getToUnload() {
        return this.toUnload;
    }

    public ArrayList<Pair<Position, Pallet>> getCurrentLoad() {
        return this.currentLoad;
    }

    public void setDock(Dock dock) {
        this.dock = dock;
        this.changed();
    }

    public Dock getDock() {
        return this.dock;
    }

    public void setPosition(Position position) {
        this.position = position;
        this.changed();
    }

    public Position getPosition() {
        return this.position;
    }

    public void setTargetPosition(Position position) {
        this.targetPosition = position;
        this.changed();
    }

    public Position getTargetPosition() {
        return this.targetPosition;
    }

    public void add(Position position, Pallet pallet) {
        for (int i = 0; i < this.toLoad.size(); i++) {
            Pair<Position, Pallet> pair = this.toLoad.get(i);
            if (position.equals(pair.first.add(this.position)) && pallet.getType() == pair.second.getType()) {
                this.toLoad.remove(i);
                this.currentLoad.add(pair);
                break;
            }
        }

        this.changed();
    }

    public void remove(Position position, Pallet pallet) {
        for (int i = 0; i < this.toUnload.size(); i++) {
            Pair<Position, Pallet> pair = this.toUnload.get(i);
            if (position.equals(pair.first.add(this.position)) && pallet.getType() == pair.second.getType()) {
                this.toUnload.remove(i);
                this.currentLoad.remove(pair);
                break;
            }
        }
        this.changed();
    }

    public boolean done() {
        return (this.toUnload.size() + this.toLoad.size()) == 0;
    }

    public double getArrivalTime() {
        return this.arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getDepartureTime() {
        return this.departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getWaitingTime() {
        return this.departureTime - this.arrivalTime;
    }

}
