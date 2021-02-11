package agent;

import abstraction.Agent;
import warehouse.Pallet;

import java.util.ArrayList;

public class Truck implements Agent {

    private ArrayList<Pallet> toLoad;
    private ArrayList<Pallet> toUnload;
    private boolean atDock = false;

    public Truck(ArrayList<Pallet> tl, ArrayList<Pallet> tu) {
        toLoad = tl;
        toUnload = tu;
    }

    public void act() {
        // go to dock
        // or leave dock
    }

    public boolean isAvailable() {
        return !atDock;
    }
}
