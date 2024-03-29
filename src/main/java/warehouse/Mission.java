package warehouse;

import agent.Mobile;
import agent.Truck;
import scheduling.ConjunctionConstraint;
import scheduling.PrecedenceConstraint;
import util.Vector3D;

import java.util.ArrayList;

public class Mission {

    private static int MISSION_ID = 0;
    private final int id;
    private final double initTime;
    private final Pallet pallet;
    private final Truck startTruck;
    private final Truck endTruck;
    public final ConjunctionConstraint startConstraint, pickupConstraint, dropConstraint;
    private Vector3D startPosition, endPosition;
    private Mobile mobile;
    private double expectedStartTime, expectedPickUpTime, expectedEndTime;
    private int missionPathMaxLength;
    private Status status;

    public Mission(double initTime, Pallet pallet, Truck startTruck, Truck endTruck, Vector3D startPosition, Vector3D endPosition) {
        this.id = MISSION_ID++;
        this.initTime = initTime;
        this.expectedStartTime = -Double.MAX_VALUE;
        this.expectedPickUpTime = -Double.MAX_VALUE;
        this.expectedEndTime = -Double.MAX_VALUE;
        this.missionPathMaxLength = 0;
        this.pallet = pallet;
        this.startTruck = startTruck;
        this.endTruck = endTruck;
        this.mobile = null;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.status = Status.WAITING;
        this.startConstraint = new ConjunctionConstraint();
        this.pickupConstraint = new ConjunctionConstraint();
        this.dropConstraint = new ConjunctionConstraint();
    }

    public Mission(double initTime, Pallet pallet, Vector3D startPosition, Vector3D endPosition) {
        this(initTime, pallet, null, null, startPosition, endPosition);
    }

    public void setStartPosition(Vector3D position) {
        this.startPosition = position;
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

    public double getExpectedStartTime() {
        return this.expectedStartTime;
    }

    public void setExpectedStartTime(double expectedStartTime) {
        this.expectedStartTime = expectedStartTime;
    }

    public double getExpectedPickUpTime() {
        return this.expectedPickUpTime;
    }

    public void setExpectedPickUpTime(double expectedPickUpTime) {
        this.expectedPickUpTime = expectedPickUpTime;
    }

    public double getExpectedEndTime() {
        return this.expectedEndTime;
    }

    public void setExpectedEndTime(double expectedEndTime) {
        this.expectedEndTime = expectedEndTime;
    }

    public int getMissionPathMaxLength() {
        return this.missionPathMaxLength;
    }

    public void setMissionPathMaxLength(int missionPathMaxLength) {
        this.missionPathMaxLength = missionPathMaxLength;
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

    public Mobile getMobile() {
        return this.mobile;
    }

    public void setEndPosition(Vector3D position) {
        this.endPosition = position;
    }

    public Vector3D getEndPosition() {
        return this.endPosition;
    }

    public boolean isComplete() {
        return this.startPosition != null && this.endPosition != null;
    }

    private enum Status {WAITING, STARTED, CARRYING, DONE}

    public boolean canStart() {
        return this.startConstraint.satisfied();
    }

    public void start(Mobile mobile) {
        this.status = Status.STARTED;
        this.mobile = mobile;
    }

    public boolean started() {
        return this.status.ordinal() >= Status.STARTED.ordinal();
    }

    public void pickUp() {
        this.status = Status.CARRYING;
    }

    public boolean canPickUp() {
        return this.pickupConstraint.satisfied();
    }

    public boolean pickedUp() {
        return this.status.ordinal() >= Status.CARRYING.ordinal();
    }

    public boolean canDrop() {
        return this.dropConstraint.satisfied();
    }

    public void drop() {
        this.status = Status.DONE;
    }

    public boolean done() {
        return this.status.ordinal() >= Status.DONE.ordinal();
    }

    public void addStartConstraint(PrecedenceConstraint constraint) {
        this.startConstraint.add(constraint);
    }

    public ConjunctionConstraint getStartConstraint() {
        return this.startConstraint;
    }

    public ConjunctionConstraint getPickupConstraint() {
        return this.pickupConstraint;
    }

    public ConjunctionConstraint getDropConstraint() {
        return this.dropConstraint;
    }

    public ArrayList<Mission> getPrecedingMissions() {
        ArrayList<Mission> precedingMissions = new ArrayList<>();
        precedingMissions.addAll(this.startConstraint.getPrecedingMissions());
        precedingMissions.addAll(this.pickupConstraint.getPrecedingMissions());
        precedingMissions.addAll(this.dropConstraint.getPrecedingMissions());
        return precedingMissions;
    }

}
