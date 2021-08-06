package agent;

import graph.AccessibilityChecker;
import graph.Graph;
import observer.Observable;
import util.Vector3D;
import warehouse.Pallet;
import warehouse.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Stock extends Observable {

    private final ArrayList<Vector3D> stockPositions, bufferPositions;
    private final HashMap<Vector3D, Pallet> pallets;
    private final HashSet<Vector3D> lock;
    private final HashMap<Integer, Integer> quantities;
    private final AccessibilityChecker accessibilityChecker;
    public final RuleBasedPalletPositionFilter filter;

    public Stock(Graph graph) {
        super();
        this.pallets = new HashMap<>();
        this.lock = new HashSet<>();
        this.accessibilityChecker = new AccessibilityChecker(this, graph);
        this.filter = new RuleBasedPalletPositionFilter(this);
        this.stockPositions = new ArrayList<>();
        this.bufferPositions = new ArrayList<>();
        this.quantities = new HashMap<>();
    }

    public void addStockPosition(Vector3D position) {
        this.stockPositions.add(position);
        this.add(position, Pallet.FREE);
    }

    public void addBufferPosition(Vector3D position) {
        this.bufferPositions.add(position);
        this.add(position, Pallet.FREE);
    }

    public void add(Vector3D position, Pallet pallet) {
        if (pallet == null) {
            return;
        }
        this.pallets.put(position, pallet);
        this.unlock(position);
        if (pallet != Pallet.FREE) {
            this.quantities.computeIfPresent(pallet.getType(), (key, val) -> val + 1);
            this.quantities.putIfAbsent(pallet.getType(), 0);
        }
        this.accessibilityChecker.add(position);
        this.changed();
    }

    public void remove(Vector3D position, Pallet pallet) {
        if (pallet == null) {
            return;
        }
        this.pallets.put(position, Pallet.FREE);
        this.unlock(position);
        this.accessibilityChecker.remove(position);
        this.changed();
    }

    public Pallet get(Vector3D position) {
        return this.pallets.get(position);
    }

    public int getQuantity(int type) {
        return this.quantities.getOrDefault(type, 0);
    }

    public boolean isFree(Vector3D position) {
        return this.get(position) == Pallet.FREE && !this.isLocked(position);
    }

    public boolean isLocked(Vector3D position) {
        return this.lock.contains(position);
    }

    public void lock(Vector3D position) {
        this.lock.add(position);
        Pallet pallet = this.pallets.get(position);
        if (pallet != null && pallet != Pallet.FREE) {
            this.quantities.computeIfPresent(pallet.getType(), (key, val) -> val - 1);
        }
        this.accessibilityChecker.lock(position);
    }

    public void unlock(Vector3D position) {
        this.lock.remove(position);
    }

    public ArrayList<Vector3D> getStartPositions(Pallet pallet) {
        return this.filter.getStartPositions(pallet);
    }

    public ArrayList<Vector3D> getEndPositions(Pallet pallet) {
        return this.filter.getEndPositions(pallet);
    }

    public ArrayList<Vector3D> getAllPositions() {
        return new ArrayList<>(this.pallets.keySet());
    }

    public ArrayList<Vector3D> getStockPositions() {
        return this.stockPositions;
    }

    public ArrayList<Vector3D> getBufferPositions() {
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

        public ArrayList<Vector3D> getStartPositions(Pallet pallet) {
            boolean found = false;
            int priority = 0;

            ArrayList<Vector3D> positions = new ArrayList<>();

            for (Rule rule : this.rules) {
                if (found && rule.getPriority() > priority) {
                    return positions;
                }

                if (rule.matches(pallet)) {
                    for (Vector3D position : rule.getPositions()) {
                        Pallet stockPallet = this.stock.get(position);
                        if (stockPallet != null &&
                                stockPallet.getType() == pallet.getType() &&
                                !this.stock.isLocked(position) &&
                                this.stock.accessibilityChecker.check(position)) {
                            positions.add(position);
                        }
                    }

                    if (!positions.isEmpty()) {
                        found = true;
                        priority = rule.getPriority();

                        if (rule.isBlocking()) {
                            return positions;
                        }
                    }
                }
            }

            // if no match, return any possible position
            if (positions.isEmpty()) {
                for (Vector3D position : this.stock.getStockPositions()) {
                    Pallet stockPallet = this.stock.get(position);
                    if (stockPallet != null &&
                            stockPallet.getType() == pallet.getType() &&
                            !this.stock.isLocked(position) &&
                            this.stock.accessibilityChecker.check(position)) {
                        positions.add(position);
                    }
                }
            }

            return positions;
        }

        public ArrayList<Vector3D> getEndPositions(Pallet pallet) {
            boolean found = false;
            int priority = 0;

            ArrayList<Vector3D> positions = new ArrayList<>();

            for (Rule rule : this.rules) {
                if (found && rule.getPriority() > priority) {
                    return positions;
                }

                if (rule.matches(pallet)) {
                    for (Vector3D position : rule.getPositions()) {
                        if (this.stock.isFree(position) &&
                                this.stock.accessibilityChecker.check(position)) {
                            positions.add(position);
                        }
                    }

                    if (!positions.isEmpty()) {
                        found = true;
                        priority = rule.getPriority();

                        if (rule.isBlocking()) {
                            return positions;
                        }
                    }
                }
            }

            // if no match, return any possible position
            if (positions.isEmpty()) {
                for (Vector3D position : this.stock.getStockPositions()) {
                    if (this.stock.isFree(position) &&
                            this.stock.accessibilityChecker.check(position)) {
                        positions.add(position);
                    }
                }
            }

            return positions;
        }

    }

}
