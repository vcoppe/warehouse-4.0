package agent;

import observer.Observable;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

// need to add all possible positions with Pallet.FREE at the start
public class Stock extends Observable {

    private final HashMap<Position, Pallet> pallets;
    private final HashSet<Position> lock;

    public Stock() {
        super();
        this.pallets = new HashMap<>();
        this.lock = new HashSet<>();
    }

    public void add(Position position, Pallet pallet) {
        this.pallets.put(position, pallet);
        this.unlock(position);
        this.changed();
    }

    public void remove(Position position, Pallet pallet) {
        this.pallets.put(position, Pallet.FREE);
        this.unlock(position);
        this.changed();
    }

    public Pallet get(Position position) {
        return this.pallets.get(position);
    }

    public boolean isFree(Position position) {
        return this.get(position) == Pallet.FREE && !this.isLocked(position);
    }

    public boolean isLocked(Position position) {
        return this.lock.contains(position);
    }

    public void lock(Position position) {
        this.lock.add(position);
    }

    public void unlock(Position position) {
        this.lock.remove(position);
    }

    // TODO remove production line start and end buffer from output
    public ArrayList<Position> getStartPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Position position : this.pallets.keySet()) {
            Pallet stockPallet = this.pallets.get(position);
            if (stockPallet != null
                    && !this.isLocked(position)
                    && stockPallet.getType() == pallet.getType()) {
                positions.add(position);
            }
        }
        return positions;
    }

    // TODO remove production line start and end buffer from output
    public ArrayList<Position> getEndPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Position position : this.pallets.keySet()) {
            Pallet stockPallet = this.pallets.get(position);
            if (stockPallet == Pallet.FREE && !this.lock.contains(position)) {
                positions.add(position);
            }
        }
        return positions;
    }

    public List<Position> getAllPositions() {
        return new ArrayList<>(this.pallets.keySet());
    }

}
