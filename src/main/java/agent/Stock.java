package agent;

import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

// need to add all possible positions with Pallet.FREE at the start
public class Stock {

    private final Warehouse warehouse;
    private final HashMap<Integer, Pallet> pallets;

    public Stock(Warehouse warehouse) {
        this.warehouse = warehouse;
        this.pallets = new HashMap<>();
    }

    public int toInt(Position position) {
        return position.getX() + this.warehouse.getWidth() *
                (position.getY() + this.warehouse.getDepth() * position.getZ());
    }

    public Position toPosition(int hash) {
        return new Position(
                hash % this.warehouse.getWidth(),
                (hash / this.warehouse.getWidth()) % this.warehouse.getDepth(),
                hash / (this.warehouse.getWidth() * this.warehouse.getDepth())
                );
    }

    public void add(Position position, Pallet pallet) {
        this.pallets.put(this.toInt(position), pallet);
    }

    public void remove(Position position, Pallet pallet) {
        this.pallets.put(this.toInt(position), Pallet.FREE);
    }

    public Pallet get(Position position) {
        return this.pallets.get(this.toInt(position));
    }

    public boolean isFree(Position position) {
        return this.get(position) == Pallet.FREE;
    }

    public ArrayList<Position> getStartPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Entry<Integer, Pallet> entry : pallets.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getType() == pallet.getType()) {
                positions.add(toPosition(entry.getKey()));
            }
        }
        return positions;
    }

    public ArrayList<Position> getEndPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<>();
        for (Entry<Integer, Pallet> entry : pallets.entrySet()) {
            if (entry.getValue() == Pallet.FREE) {
                positions.add(toPosition(entry.getKey()));
            }
        }
        return positions;
    }

}
