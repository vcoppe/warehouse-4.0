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

    private final Warehouse warehouse;
    private final HashMap<Integer, Pallet> pallets;
    private final HashSet<Integer> lock;

    public Stock(Warehouse warehouse) {
        super();
        this.warehouse = warehouse;
        this.pallets = new HashMap<>();
        this.lock = new HashSet<>();
    }

    public int toInt(Position position) {
        return this.warehouse.toInt(position);
    }

    public Position toPosition(int hash) {
        return this.warehouse.toPosition(hash);
    }

    public void add(Position position, Pallet pallet) {
        this.pallets.put(this.toInt(position), pallet);
        this.unlock(position);
        this.changed();
    }

    public void remove(Position position, Pallet pallet) {
        this.pallets.put(this.toInt(position), Pallet.FREE);
        this.unlock(position);
        this.changed();
    }

    public Pallet get(Position position) {
        return this.pallets.get(this.toInt(position));
    }

    public boolean isFree(Position position) {
        return this.get(position) == Pallet.FREE && !this.isLocked(position);
    }

    public boolean isLocked(Position position) {
        return this.lock.contains(this.toInt(position));
    }

    public boolean isLocked(Integer position) {
        return this.lock.contains(position);
    }

    public void lock(Position position) {
        this.lock.add(this.toInt(position));
    }

    public void unlock(Position position) {
        this.lock.remove(this.toInt(position));
    }

    public ArrayList<Position> getStartPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Integer position : this.pallets.keySet()) {
            Pallet stockPallet = this.pallets.get(position);
            if (stockPallet != null
                    && !this.isLocked(position)
                    && stockPallet.getType() == pallet.getType()) {
                positions.add(this.toPosition(position));
            }
        }
        return positions;
    }

    public ArrayList<Position> getEndPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Integer position : this.pallets.keySet()) {
            Pallet stockPallet = this.pallets.get(position);
            if (stockPallet == Pallet.FREE && !this.lock.contains(position)) {
                positions.add(this.toPosition(position));
            }
        }
        return positions;
    }

    public List<Position> getAllPositions() {
        return this.pallets.keySet().stream().map(this::toPosition).collect(Collectors.toList());
    }

}
