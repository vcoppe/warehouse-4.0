package event;

import agent.Controller;
import agent.ProductionLine;
import simulation.Event;
import simulation.Simulation;
import util.Pair;
import warehouse.Configuration;
import warehouse.Pallet;
import warehouse.Production;
import warehouse.Scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProductionGeneratorEvent extends Event {

    private final Configuration configuration;
    private final Scenario scenario;
    private final ProductionLine productionLine;
    private final Controller controller;
    private final Random random = new Random(0);

    public ProductionGeneratorEvent(Simulation simulation, double time, Configuration configuration, Scenario scenario, ProductionLine productionLine) {
        super(simulation, time);
        this.configuration = configuration;
        this.scenario = scenario;
        this.productionLine = productionLine;
        this.controller = configuration.controller;
    }

    @Override
    public void run() {
        ArrayList<Pair<Pallet, Integer>> in = new ArrayList<>();
        ArrayList<Pair<Pallet, Integer>> out = new ArrayList<>();

        int nPalletsIn = 1 + random.nextInt(4);
        int nPalletsOut = 1 + random.nextInt(4);

        HashMap<Integer, Integer> takenIn = new HashMap<>();
        for (int i = 0; i < nPalletsIn; i++) {
            int type = Scenario.pickFromDistribution(this.scenario.productionLineInThroughput);
            if (!takenIn.containsKey(type)) {
                takenIn.put(type, 1);
            } else {
                takenIn.put(type, takenIn.get(type) + 1);
            }
        }

        for (Map.Entry<Integer, Integer> entry : takenIn.entrySet()) {
            in.add(new Pair<>(new Pallet(entry.getKey()), entry.getValue()));
        }

        HashMap<Integer, Integer> takenOut = new HashMap<>();
        for (int i = 0; i < nPalletsOut; i++) {
            int type = Scenario.pickFromDistribution(this.scenario.productionLineOutThroughput);
            if (!takenOut.containsKey(type)) {
                takenOut.put(type, 1);
            } else {
                takenOut.put(type, takenOut.get(type) + 1);
            }
        }

        for (Map.Entry<Integer, Integer> entry : takenOut.entrySet()) {
            out.add(new Pair<>(new Pallet(entry.getKey()), entry.getValue()));
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
                this.time + 200 + random.nextInt(200),
                this.configuration,
                this.scenario,
                this.productionLine
        ));
    }
}
