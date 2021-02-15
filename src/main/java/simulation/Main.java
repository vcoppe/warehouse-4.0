package simulation;

import agent.Truck;
import brain.NaiveSelector;
import event.TruckArriveEvent;
import util.Configuration;
import warehouse.Pallet;
import warehouse.Position;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        NaiveSelector selector = new NaiveSelector();
        Configuration configuration = new Configuration(1, 3, selector, selector, selector);

        ArrayList<Pallet> pallets = new ArrayList<Pallet>();
        for (int i=0; i<10; i++) pallets.add(new Pallet(i));
        Truck truck = new Truck(configuration.simulation, new Position(0, -20, 0), pallets, new ArrayList<Pallet>());
        Event event = new TruckArriveEvent(configuration.simulation,
                1,
                configuration.controller,
                configuration.warehouse,
                truck
                );
        configuration.simulation.enqueueEvent(event);

        configuration.simulation.run(1000);

    }

}
