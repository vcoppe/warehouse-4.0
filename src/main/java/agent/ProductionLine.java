package agent;

import simulation.Agent;
import simulation.Simulation;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.TreeSet;

public class ProductionLine extends Agent {

    private final Warehouse warehouse;
    private final Stock stock;
    private final int capacity;
    private final ArrayList<Position> startBuffer, endBuffer;
    private final TreeSet<Production> productions;
    private int currentCapacity;

    public ProductionLine(Simulation simulation, Warehouse warehouse, Stock stock, int capacity, ArrayList<Position> startBuffer, ArrayList<Position> endBuffer) {
        super(simulation);
        this.warehouse = warehouse;
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
    }

    public void remove(Production production) {
        this.productions.remove(production);
    }

    public ArrayList<Production> getProductions() {
        return new ArrayList<>(this.productions);
    }

    public Production getStartableProduction() {
        for (Production production : this.productions) {
            boolean startable = true;

            for (Pair<Pallet,Integer> pair : production.getIn()) {
                Pallet pallet = pair.first;
                int quantity = pair.second;

                int count = 0;
                for (Position position : this.startBuffer) {
                    if (this.stock.get(position) != null && this.stock.get(position).getType() == pallet.getType()) {
                        count++;
                    }
                }

                if (count < quantity) {
                    startable = false;
                    break;
                }
            }

            if (startable && production.getCapacity() <= this.currentCapacity) {
                return production;
            }
        }

        return null;
    }

    public void freeCapacity(double capacity) {
        this.currentCapacity += capacity;
    }

    public void reserveCapacity(double capacity) {
        this.currentCapacity -= capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getCurrentCapacity() {
        return this.currentCapacity;
    }

}
