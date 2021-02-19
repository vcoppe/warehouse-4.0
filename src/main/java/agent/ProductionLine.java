package agent;

import observer.Observable;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class ProductionLine extends Observable {

    private final Stock stock;
    private final int capacity;
    private final ArrayList<Position> startBuffer, endBuffer;
    private final TreeSet<Production> productions;
    private int currentCapacity;

    public ProductionLine(Stock stock, int capacity, ArrayList<Position> startBuffer, ArrayList<Position> endBuffer) {
        super();
        this.stock = stock;
        this.capacity = capacity;
        this.startBuffer = startBuffer;
        this.endBuffer = endBuffer;
        this.productions = new TreeSet<>();
        this.currentCapacity = capacity;
    }

    public ArrayList<Position> getStartBuffer() {
        return this.startBuffer;
    }

    public ArrayList<Position> getEndBuffer() {
        return this.endBuffer;
    }

    public Position getStartBufferPosition() {
        for (Position position : this.startBuffer) {
            if (this.stock.isFree(position)) {
                return position;
            }
        }

        return null;
    }

    public Position getEndBufferPosition() {
        for (Position position : this.endBuffer) {
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

            for (Pair<Pallet,Integer> pair : production.getIn()) {
                Pallet pallet = pair.first;
                int quantity = pair.second;

                int count = 0;
                for (Position position : this.startBuffer) {
                    Pallet bufferPallet = this.stock.get(position);
                    if (bufferPallet != null
                            && bufferPallet.getType() == pallet.getType()
                            && !usedPallets.contains(bufferPallet.getId())
                            && count < quantity) {
                        count++;
                        productionPallets.add(bufferPallet.getId());
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

}
