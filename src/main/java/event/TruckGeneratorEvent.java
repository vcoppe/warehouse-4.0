package event;

import agent.Controller;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.Random;

public class TruckGeneratorEvent extends Event {

    private final Warehouse warehouse;
    private final Controller controller;
    private final Random random;

    public TruckGeneratorEvent(Simulation simulation, double time, Warehouse warehouse, Controller controller) {
        super(simulation, time);
        this.warehouse = warehouse;
        this.controller = controller;
        this.random = new Random(0);
    }

    @Override
    public void run() {
        ArrayList<Pallet> toLoad = new ArrayList<>();
        ArrayList<Pallet> toUnload = new ArrayList<>();

        int nPallets = 20 + random.nextInt(20);
        int nPalletsToLoad = random.nextInt(nPallets);
        int nPalletsToUnload = nPallets - nPalletsToLoad;

        for (int i = 0; i < nPalletsToLoad; i++) {
            toLoad.add(new Pallet(random.nextInt(10)));
        }

        for (int i = 0; i < nPalletsToUnload; i++) {
            toUnload.add(new Pallet(random.nextInt(10)));
        }

        this.simulation.enqueueEvent(
                new TruckArriveEvent(
                        this.simulation,
                        this.time,
                        this.controller,
                        new Truck(
                                new Position(this.warehouse.getWidth(), this.warehouse.getDepth() + 50),
                                toLoad,
                                toUnload
                        )
                )
        );
        this.simulation.enqueueEvent(new TruckGeneratorEvent(
                this.simulation,
                this.time + 100 + random.nextInt(50),
                this.warehouse,
                this.controller
        ));
    }
}
