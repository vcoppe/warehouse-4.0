package warehouse;

import agent.Stock;
import agent.Truck;

public class Mission {

    private static int MISSION_ID = 0;
    private final int id;
    private final Pallet pallet;
    private final Stock stock;
    private final Truck startTruck;
    private final Truck endTruck;
    private final Position startPosition;
    private final Position endPosition;

    public Mission(Pallet pallet, Stock stock, Truck startTruck, Truck endTruck, Position startPosition, Position endPosition) {
        this.id = MISSION_ID++;
        this.pallet = pallet;
        this.stock = stock;
        this.startTruck = startTruck;
        this.endTruck = endTruck;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public Mission(Pallet pallet, Stock stock, Truck truck, Position startPosition, Position endPosition) {
        this(pallet, stock, null, truck, startPosition, endPosition);
    }

    public Mission(Pallet pallet, Truck truck, Stock stock, Position startPosition, Position endPosition) {
        this(pallet, stock, truck, null, startPosition, endPosition);
    }

    public int getId() {
        return this.id;
    }

    public Pallet getPallet() {
        return this.pallet;
    }

    public Stock getStock() {
        return this.stock;
    }

    public Truck getStartTruck() {
        return this.startTruck;
    }

    public Truck getEndTruck() {
        return this.endTruck;
    }

    public Position getStartPosition() {
        return this.startPosition;
    }

    public Position getEndPosition() {
        return this.endPosition;
    }

}
