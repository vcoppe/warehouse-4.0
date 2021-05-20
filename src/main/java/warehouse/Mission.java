package warehouse;

import agent.Truck;

public class Mission {

    private enum Status { WAITING, STARTED, CARRYING, DONE };

    private static int MISSION_ID = 0;
    private final int id;
    private final double initTime;
    private final Pallet pallet;
    private final Truck startTruck;
    private final Truck endTruck;
    private final Position startPosition;
    private final Position endPosition;
    private Status status;
    private final ConjunctionCondition startCondition, pickupCondition, dropCondition;

    public Mission(double initTime, Pallet pallet, Truck startTruck, Truck endTruck, Position startPosition, Position endPosition) {
        this.id = MISSION_ID++;
        this.initTime = initTime;
        this.pallet = pallet;
        this.startTruck = startTruck;
        this.endTruck = endTruck;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.status = Status.WAITING;
        this.startCondition = new ConjunctionCondition();
        this.pickupCondition = new ConjunctionCondition();
        this.dropCondition = new ConjunctionCondition();
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

    public boolean canStart() {
        return this.startCondition.satisfied();
    }
    
    public void start() {
        this.status = Status.STARTED;
    }

    public boolean started() {
        return this.status.ordinal() >= Status.STARTED.ordinal();
    }

    public void pickUp() {
        this.status = Status.CARRYING;
    }

    public boolean canPickUp() {
        return this.pickupCondition.satisfied();
    }

    public boolean pickedUp() {
        return this.status.ordinal() >= Status.CARRYING.ordinal();
    }

    public boolean canDrop() {
        return this.dropCondition.satisfied();
    }

    public void drop() {
        this.status = Status.DONE;
    }

    public boolean done() {
        return this.status.ordinal() >= Status.DONE.ordinal();
    }

    public void startAfterStart(Mission mission) {
        this.startCondition.add(new MissionStartedCondition(mission));
    }

    public void startAfterEnd(Mission mission) {
        this.startCondition.add(new MissionDoneCondition(mission));
    }

}
