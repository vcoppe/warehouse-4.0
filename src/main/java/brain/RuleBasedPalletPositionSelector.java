package brain;

import agent.Stock;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Rule;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RuleBasedPalletPositionSelector implements PalletPositionSelector {

    private final PriorityQueue<Rule> rules; // one list for start positions and one for end positions ?

    public RuleBasedPalletPositionSelector() {
        this.rules = new PriorityQueue<>();
    }

    public void addRule(Rule rule) {
        this.rules.add(rule);
    }

    public void removeRule(Rule rule) {
        this.rules.remove(rule);
    }

    // select start pallet based on a rule ?
    // from the possible positions, how do we select one ?
    @Override
    public Position selectStartPosition(Pallet pallet, Position endPosition, Stock stock) {
        boolean found = false;
        int priority = 0;

        ArrayList<Position> positions = new ArrayList<>();

        for (Rule rule : this.rules) {
            if (found && rule.getPriority() > priority) {
                return positions.get(0);
            }

            if (rule.matches(pallet)) {
                found = true;
                priority = rule.getPriority();

                for (Position position : rule.positions()) {
                    if (stock.get(position).getType() == pallet.getType()) {
                        positions.add(position);
                    }
                }
            }
        }

        return positions.get(0);
    }

    @Override
    public Position selectEndPosition(Pallet pallet, Position startPosition, Stock stock) {
        boolean found = false;
        int priority = 0;

        ArrayList<Position> positions = new ArrayList<>();

        for (Rule rule : this.rules) {
            if (found && rule.getPriority() > priority) {
                return positions.get(0);
            }

            if (rule.matches(pallet)) {
                found = true;
                priority = rule.getPriority();

                for (Position position : rule.positions()) {
                    if (stock.isFree(position)) {
                        positions.add(position);
                    }
                }
            }
        }

        return positions.get(0);
    }

}
