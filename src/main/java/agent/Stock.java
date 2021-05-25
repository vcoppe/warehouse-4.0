package agent;

import observer.Observable;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Rule;
import warehouse.Warehouse;

import java.util.*;
import java.util.stream.Collectors;

public class Stock extends Observable {

    private final HashMap<Position, Pallet> pallets;
    private final HashSet<Position> lock;
    private final RuleBasedPalletPositionFilter filter;

    public Stock(ArrayList<Position> positions) {
        super();
        this.pallets = new HashMap<>();
        this.lock = new HashSet<>();
        this.filter = new RuleBasedPalletPositionFilter(this);
        this.filter.add(new Rule(Integer.MAX_VALUE, false, positions));

        for (Position position : positions) {
            this.add(position, Pallet.FREE);
        }
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

    public List<Position> getAllPositions() {
        return new ArrayList<>(this.pallets.keySet());
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

            return positions;
        }

    }

}
