package agent;

import observer.Observable;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Rule;

import java.util.*;

public class Stock extends Observable {

    private final ArrayList<Position> stockPositions, bufferPositions;
    private final HashMap<Position, Pallet> pallets;
    private final HashSet<Position> lock;
    private final RuleBasedPalletPositionFilter filter;

    public Stock() {
        super();
        this.pallets = new HashMap<>();
        this.lock = new HashSet<>();
        this.filter = new RuleBasedPalletPositionFilter(this);
        this.stockPositions = new ArrayList<>();
        this.bufferPositions = new ArrayList<>();
    }

    public void addStockPosition(Position position) {
        this.stockPositions.add(position);
        this.add(position, Pallet.FREE);
    }

    public void addBufferPosition(Position position) {
        this.bufferPositions.add(position);
        this.add(position, Pallet.FREE);
    }

    public void add(Position position, Pallet pallet) {
        if (pallet == null) {
            return;
        }
        this.pallets.put(position, pallet);
        this.unlock(position);
        this.changed();
    }

    public void remove(Position position, Pallet pallet) {
        if (pallet == null) {
            return;
        }
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

    public ArrayList<Position> getStartPositions(Pallet pallet) {
        return this.filter.getStartPositions(pallet);
    }

    public ArrayList<Position> getEndPositions(Pallet pallet) {
        return this.filter.getEndPositions(pallet);
    }

    public ArrayList<Position> getAllPositions() {
        return new ArrayList<>(this.pallets.keySet());
    }

    public ArrayList<Position> getStockPositions() {
        return this.stockPositions;
    }

    public ArrayList<Position> getBufferPositions() {
        return this.bufferPositions;
    }

    public class RuleBasedPalletPositionFilter {

        private final Stock stock;
        private final PriorityQueue<Rule> rules;

        public RuleBasedPalletPositionFilter(Stock stock) {
            this.stock = stock;
            this.rules = new PriorityQueue<>();
        }

        public void add(Rule rule) {
            this.rules.add(rule);
        }

        public void remove(Rule rule) {
            this.rules.remove(rule);
        }

        public ArrayList<Position> getStartPositions(Pallet pallet) {
            boolean found = false;
            int priority = 0;

            ArrayList<Position> positions = new ArrayList<>();

            for (Rule rule : this.rules) {
                if (found && rule.getPriority() > priority) {
                    return positions;
                }

                if (rule.matches(pallet)) {
                    found = true;
                    priority = rule.getPriority();

                    for (Position position : rule.getPositions()) {
                        Pallet stockPallet = this.stock.get(position);
                        if (stockPallet != null && stockPallet.getType() == pallet.getType() && !this.stock.isLocked(position)) {
                            positions.add(position);
                        }
                    }

                    if (rule.isBlocking()) {
                        return positions;
                    }
                }
            }

            // if no match, return any possible position
            if (positions.isEmpty()) {
                for (Position position : this.stock.getStockPositions()) {
                    Pallet stockPallet = this.stock.get(position);
                    if (stockPallet != null && stockPallet.getType() == pallet.getType() && !this.stock.isLocked(position)) {
                        positions.add(position);
                    }
                }
            }

            return positions;
        }

        public ArrayList<Position> getEndPositions(Pallet pallet) {
            boolean found = false;
            int priority = 0;

            ArrayList<Position> positions = new ArrayList<>();

            for (Rule rule : this.rules) {
                if (found && rule.getPriority() > priority) {
                    return positions;
                }

                if (rule.matches(pallet)) {
                    found = true;
                    priority = rule.getPriority();

                    for (Position position : rule.getPositions()) {
                        if (this.stock.isFree(position)) {
                            positions.add(position);
                        }
                    }

                    if (rule.isBlocking()) {
                        return positions;
                    }
                }
            }

            // if no match, return any possible position
            if (positions.isEmpty()) {
                for (Position position : this.stock.getStockPositions()) {
                    if (this.stock.isFree(position)) {
                        positions.add(position);
                    }
                }
            }

            return positions;
        }

    }

}
