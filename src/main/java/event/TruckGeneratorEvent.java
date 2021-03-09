package event;

import agent.Controller;
import agent.Truck;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Position;
import warehouse.Warehouse;

import java.util.ArrayList;
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
        ArrayList<Pair<Position, Pallet>> toLoad = new ArrayList<>();
        ArrayList<Pair<Position, Pallet>> toUnload = new ArrayList<>();

        int nPallets = 18 + random.nextInt(10);
        int nPalletsToLoad = random.nextInt(nPallets);
        int nPalletsToUnload = nPallets - nPalletsToLoad;

        for (int i = 0; i < nPalletsToLoad; i++) {
            toLoad.add(new Pair<>(
                    new Position((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(random.nextInt(10))
            ));
        }

        for (int i = 0; i < nPalletsToUnload; i++) {
            toUnload.add(new Pair<>(
                    new Position((i / 9) * this.configuration.palletSize, (i % 9) * this.configuration.palletSize),
                    new Pallet(random.nextInt(10))
            ));
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
                this.configuration,
                this.random
        ));
    }
}
