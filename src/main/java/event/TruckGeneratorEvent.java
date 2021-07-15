package event;

import agent.Controller;
import agent.Stock;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Scenario;
import warehouse.Warehouse;

import java.util.HashMap;
import java.util.Random;

public class TruckGeneratorEvent extends Event {

    private final Configuration configuration;
    private final Scenario scenario;
    private final Warehouse warehouse;
    private final Stock stock;
    private final Controller controller;
    private static final Random random = new Random(0);

    public TruckGeneratorEvent(Simulation simulation, double time, Configuration configuration, Scenario scenario) {
        super(simulation, time);
        this.configuration = configuration;
        this.scenario = scenario;
        this.warehouse = configuration.warehouse;
        this.stock = configuration.stock;
        this.controller = configuration.controller;
    }

    @Override
    public void run() {
        HashMap<Vector3D, Pallet> toLoad = new HashMap<>();
        HashMap<Vector3D, Pallet> toUnload = new HashMap<>();

        HashMap<Integer, Integer> loadQuantities = new HashMap<>();

        int nPallets = 9 + random.nextInt(19);
        int nPalletsToLoad = random.nextInt(nPallets);
        int nPalletsToUnload = nPallets - nPalletsToLoad;

        for (int i = 0; i < nPalletsToLoad; i++) {
            int type;
            do {
                type = Scenario.pickFromDistribution(this.scenario.dockThroughput);
            } while (loadQuantities.getOrDefault(type, 0) == this.stock.getQuantity(type));
            toLoad.put(
                    new Vector3D((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(type)
            );
            loadQuantities.computeIfPresent(type, (key, val) -> val + 1);
            loadQuantities.putIfAbsent(type, 1);
        }

        for (int i = 0; i < nPalletsToUnload; i++) {
            toUnload.put(
                    new Vector3D((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(Scenario.pickFromDistribution(this.scenario.dockThroughput))
            );
        }

        Truck.Type type = Truck.Type.BACK;
        if (random.nextInt(100) > 65) {
            type = Truck.Type.SIDES;
        }

        this.simulation.enqueueEvent(
                new TruckArriveEvent(
                        this.simulation,
                        this.time,
                        this.controller,
                        new Truck(
                                type,
                                new Vector3D(this.warehouse.getWidth() - this.configuration.truckWidth, 2 * this.warehouse.getDepth() - this.configuration.truckDepth),
                                toLoad,
                                toUnload
                        )
                )
        );

        this.simulation.enqueueEvent(new TruckGeneratorEvent(
                this.simulation,
                this.time + 100 + random.nextInt(100),
                this.configuration,
                this.scenario
        ));
    }
}
