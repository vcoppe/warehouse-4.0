package agent;

import observer.Observable;
import util.Pair;
import util.Vector3D;
import warehouse.Pallet;
import warehouse.Production;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class ProductionLine extends Observable {

    private final Stock stock;
    private final int capacity;
    private final ArrayList<Vector3D> startBuffer, endBuffer;
    private final TreeSet<Production> productions;
    private final Vector3D position;
    private final int width, depth;
    private int currentCapacity;

    public ProductionLine(Stock stock, Vector3D position, int width, int depth, int capacity, ArrayList<Vector3D> startBuffer, ArrayList<Vector3D> endBuffer) {
        super();
        this.stock = stock;
        this.position = position;
        this.width = width;
        this.depth = depth;
        this.capacity = capacity;
        this.startBuffer = startBuffer;
        this.endBuffer = endBuffer;
        this.productions = new TreeSet<>();
        this.currentCapacity = capacity;
    }

    public ArrayList<Vector3D> getStartBuffer() {
        return this.startBuffer;
    }

    public ArrayList<Vector3D> getEndBuffer() {
        return this.endBuffer;
    }

    public Vector3D getStartBufferPosition() {
        for (Vector3D position : this.startBuffer) {
            if (this.stock.isFree(position)) {
                return position;
            }
        }

        return null;
    }

    public Vector3D getEndBufferPosition() {
        for (Vector3D position : this.endBuffer) {
            if (this.stock.isFree(position)) {
                return position;
            }
        }

        return null;
    }

    public void add(Production production) {
        this.productions.add(production);
        this.changed();
    }

    public void remove(Production production) {
        this.productions.remove(production);
        this.changed();
    }

    public ArrayList<Production> getProductions() {
        return new ArrayList<>(this.productions);
    }

    public ArrayList<Production> getStartableProductions() {
        ArrayList<Production> startableProductions = new ArrayList<>();
        HashSet<Integer> usedPallets = new HashSet<>();
        int cumulCapacity = 0;
        for (Production production : this.productions) {
            boolean startable = true;
            ArrayList<Integer> productionPallets = new ArrayList<>();

            for (Pair<Pallet, Integer> pair : production.getIn()) {
                Pallet pallet = pair.first;
                int quantity = pair.second;

                int count = 0;
                for (Vector3D position : this.startBuffer) {
                    if (!this.stock.isLocked(position)) {
                        Pallet bufferPallet = this.stock.get(position);
                        if (bufferPallet != null
                                && bufferPallet.getProduct() == pallet.getProduct()
                                && !usedPallets.contains(bufferPallet.getId())
                                && count < quantity) {
                            count++;
                            productionPallets.add(bufferPallet.getId());
                        }
                    }
                }

                if (count < quantity) {
                    startable = false;
                    break;
                }
            }

            if (startable && production.getCapacity() + cumulCapacity <= this.currentCapacity) {
                cumulCapacity += production.getCapacity();
                startableProductions.add(production);
                usedPallets.addAll(productionPallets);
            }
        }

        return startableProductions;
    }

    public void lockProductionPallets(Production production) {
        for (Pair<Pallet, Integer> pair : production.getIn()) {
            Pallet pallet = pair.first;
            int quantity = pair.second;

            int count = 0;
            for (Vector3D position : this.startBuffer) {
                if (!this.stock.isLocked(position)) {
                    Pallet bufferPallet = this.stock.get(position);
                    if (bufferPallet != null && bufferPallet.getProduct() == pallet.getProduct()) {
                        this.stock.lock(position);
                        count++;
                        if (count == quantity) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void freeCapacity(double capacity) {
        this.currentCapacity += capacity;
        this.changed();
    }

    public void reserveCapacity(double capacity) {
        this.currentCapacity -= capacity;
        this.changed();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getCurrentCapacity() {
        return this.currentCapacity;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public int getWidth() {
        return this.width;
    }

    public int getDepth() {
        return this.depth;
    }

}
