package event;

import agent.Controller;
import agent.ProductionLine;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Production;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class ProductionGeneratorEvent extends Event {

    private final Configuration configuration;
    private final ProductionLine productionLine;
    private final Controller controller;
    private final Random random;

    public ProductionGeneratorEvent(Simulation simulation, double time, Configuration configuration, ProductionLine productionLine, Random random) {
        super(simulation, time);
        this.configuration = configuration;
        this.productionLine = productionLine;
        this.controller = configuration.controller;
        this.random = random;
    }

    public ProductionGeneratorEvent(Simulation simulation, double time, Configuration configuration, ProductionLine productionLine) {
        this(simulation, time, configuration, productionLine, new Random(0));
    }

    @Override
    public void run() {
        ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet, Integer>> out = new ArrayList<>();

        int nPalletsIn = 1 + random.nextInt(4);
        int nPalletsOut = 1 + random.nextInt(4);

        HashSet<Integer> takenIn = new HashSet<>();
        for (int i = 0; i < nPalletsIn; ) {
            int type;
            do {
                type = random.nextInt(10);
            } while (takenIn.contains(type));
            takenIn.add(type);
            int number = 1 + random.nextInt(3);
            if (i + number > nPalletsIn) number = nPalletsIn - i;
            in.add(new Pair<>(new Pallet(type), number));
            i += number;
        }

        HashSet<Integer> takenOut = new HashSet<>();
        for (int i = 0; i < nPalletsOut; i++) {
            int type;
            do {
                type = random.nextInt(10);
            } while (takenOut.contains(type));
            takenOut.add(type);
            int number = 1 + random.nextInt(3);
            if (i + number > nPalletsOut) number = nPalletsOut - i;
            out.add(new Pair<>(new Pallet(type), number));
            i += number;
        }

        this.simulation.enqueueEvent(
                new ProductionInitEvent(
                        this.simulation,
                        this.time,
                        this.controller,
                        this.productionLine,
                        new Production(in, out, 20 + random.nextInt(50), 1, this.time + 100)
                )
        );
        this.simulation.enqueueEvent(new ProductionGeneratorEvent(
                this.simulation,
                this.time + 100 + random.nextInt(200),
                this.configuration,
                this.productionLine,
                this.random
        ));
    }
}
