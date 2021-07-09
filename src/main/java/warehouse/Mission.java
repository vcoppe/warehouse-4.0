package warehouse;

import agent.Truck;
import util.*;

public class Mission {

    private final Vector3D startPosition;

    private static int MISSION_ID = 0;
    private final int id;
    private final double initTime;
    private final Pallet pallet;
    private final Truck startTruck;
    private final Truck endTruck;
    private final Vector3D endPosition;

    public Mission(double initTime, Pallet pallet, Truck startTruck, Truck endTruck, Vector3D startPosition, Vector3D endPosition) {
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

    private Status status;
    private final ConjunctionCondition startCondition, pickupCondition, dropCondition;

    public Mission(double initTime, Pallet pallet, Vector3D startPosition, Vector3D endPosition) {
        this(initTime, pallet, null, null, startPosition, endPosition);
    }

    public Vector3D getStartPosition() {
        return this.startPosition;
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

    public Vector3D getEndPosition() {
        return this.endPosition;
    }

    private enum Status {WAITING, STARTED, CARRYING, DONE}

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

    public void startAfterPickup(Mission mission) {
        this.startCondition.add(new MissionPickedUpCondition(mission));
    }

    public void startAfterEnd(Mission mission) {
        this.startCondition.add(new MissionDoneCondition(mission));
    }

}
