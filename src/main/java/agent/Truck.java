package agent;

import observer.Observable;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class Truck extends Observable {

    private static int TRUCK_ID = 0;
    private final int id;
    private Dock dock;
    private Position position;
    private final ArrayList<Pallet> toLoad;
    private final ArrayList<Pallet> toUnload;
    private int countDone;

    public Truck(Dock dock, Position position, ArrayList<Pallet> toLoad, ArrayList<Pallet> toUnload) {
        super();
        this.id = TRUCK_ID++;
        this.dock = dock;
        this.position = position;
        this.toLoad = toLoad;
        this.toUnload = toUnload;
        this.countDone = 0;
    }

    public Truck(Position position, ArrayList<Pallet> toLoad, ArrayList<Pallet> toUnload) {
        this(null, position, toLoad, toUnload);
    }

    public int getId() {
        return this.id;
    }

    public ArrayList<Pallet> getToLoad() {
        return this.toLoad;
    }

    public ArrayList<Pallet> getToUnload() {
        return this.toUnload;
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

    public void add(Pallet pallet) {
        this.countDone++;
        this.changed();
    }

    public void remove(Pallet pallet) {
        this.countDone++;
        this.changed();
    }

    public boolean done() {
        return this.countDone == (this.toUnload.size() + this.toLoad.size());
    }


}
