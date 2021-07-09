package event;

import agent.Controller;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Vector3D;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Warehouse;

import java.util.HashMap;
import java.util.Random;

public class TruckGeneratorEvent extends Event {

    private final Configuration configuration;
    private final Warehouse warehouse;
    private final Controller controller;
    private final Random random;

    public TruckGeneratorEvent(Simulation simulation, double time, Configuration configuration, Random random) {
        super(simulation, time);
        this.configuration = configuration;
        this.warehouse = configuration.warehouse;
        this.controller = configuration.controller;
        this.random = random;
    }

    public TruckGeneratorEvent(Simulation simulation, double time, Configuration configuration) {
        this(simulation, time, configuration, new Random(0));
    }

    @Override
    public void run() {
        HashMap<Vector3D, Pallet> toLoad = new HashMap<>();
        HashMap<Vector3D, Pallet> toUnload = new HashMap<>();

        int nPallets = 9 + random.nextInt(19);
        int nPalletsToLoad = random.nextInt(nPallets);
        int nPalletsToUnload = nPallets - nPalletsToLoad;

        for (int i = 0; i < nPalletsToLoad; i++) {
            toLoad.put(
                    new Vector3D((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(random.nextInt(10))
            );
        }

        for (int i = 0; i < nPalletsToUnload; i++) {
            toUnload.put(
                    new Vector3D((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(random.nextInt(10))
            );
        }

        this.simulation.enqueueEvent(
                new TruckArriveEvent(
                        this.simulation,
                        this.time,
                        this.controller,
                        new Truck(
                                new Vector3D(this.warehouse.getWidth() - this.configuration.truckWidth, 2 * this.warehouse.getDepth() - this.configuration.truckDepth),
                                toLoad,
                                toUnload
                        )
                )
        );
        this.simulation.enqueueEvent(new TruckGeneratorEvent(
                this.simulation,
                this.time + 50 + random.nextInt(100),
                this.configuration,
                this.random
        ));
    }
}
