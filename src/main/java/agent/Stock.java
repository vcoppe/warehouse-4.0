package agent;

import simulation.Agent;
import simulation.Simulation;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class Stock extends Agent {

    public Stock(Simulation simulation) {
        super(simulation);
    }

    public void remove(Pallet pallet, Position position) {

    }

    public void add(Pallet pallet, Position position) {

    }

    // TODO return valid positions (and based on rules)
    public ArrayList<Position> getStartPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0, 0, 0));
        return positions;
    }

    public ArrayList<Position> getEndPositions(Pallet pallet) {
        ArrayList<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0, 0, 0));
        return positions;
    }

}
