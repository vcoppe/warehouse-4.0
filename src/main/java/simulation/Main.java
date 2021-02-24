package simulation;

import agent.Truck;
import brain.NaiveSelector;
import event.ProductionInitEvent;
import event.TruckArriveEvent;
import warehouse.Configuration;
import util.Pair;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Production;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        NaiveSelector selector = new NaiveSelector();
        Configuration configuration = new Configuration(100, 100 , 1, 3, selector, selector, selector);

        ArrayList<Pallet> pallets = new ArrayList<>();
        for (int i=0; i<10; i++) {
            pallets.add(new Pallet(i));
            configuration.stock.add(new Position(3, i), new Pallet(i));
        }
        Truck truck = new Truck(new Position(0, -20), pallets, new ArrayList<>());
        Event event1 = new TruckArriveEvent(configuration.simulation, 1, configuration.controller, truck);
        configuration.simulation.enqueueEvent(event1);

        ArrayList<Pair<Pallet,Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet,Integer>> out = new ArrayList<>();
        for (int i=0; i<3; i++) {
            in.add(new Pair<>(new Pallet(i), 1));
            out.add(new Pair<>(new Pallet(3 + i), 1));
            configuration.stock.add(new Position(4, i), new Pallet(i));
        }
        Production production = new Production(in, out, 10, 1, 250);
        Event event2 = new ProductionInitEvent(configuration.simulation, 200, configuration.controller, production);
        configuration.simulation.enqueueEvent(event2);

        configuration.simulation.run(1000);

    }

}
