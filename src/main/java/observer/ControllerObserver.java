package observer;

import agent.Controller;
import agent.Truck;

public class ControllerObserver implements Observer<Controller> {

    private final Observer<Truck> truckObserver;

    public ControllerObserver(Observer<Truck> truckObserver) {
        this.truckObserver = truckObserver;
    }

    @Override
    public void update(Controller controller) {
        // update all mobiles, missions, trucks, docks

        for (Truck truck : controller.getTrucks()) {
            this.truckObserver.update(truck);
        }

    }

}
