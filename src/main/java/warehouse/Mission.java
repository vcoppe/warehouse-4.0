package warehouse;

import agent.Truck;

public class Mission {

    private static int MISSION_ID = 0;
    private final int id;
    private final double initTime;
    private final Pallet pallet;
    private final Truck startTruck;
    private final Truck endTruck;
    private final Position startPosition;
    private final Position endPosition;

    public Mission(double initTime, Pallet pallet, Truck startTruck, Truck endTruck, Position startPosition, Position endPosition) {
        this.id = MISSION_ID++;
        this.initTime = initTime;
        this.pallet = pallet;
        this.startTruck = startTruck;
        this.endTruck = endTruck;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public Mission(double initTime, Pallet pallet, Position startPosition, Position endPosition) {
        this(initTime, pallet, null, null, startPosition, endPosition);
    }

    public int getId() {
        return this.id;
    }

    public double getInitTime() {
        return this.initTime;
    }

    public Pallet getPallet() {
        return this.pallet;
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
