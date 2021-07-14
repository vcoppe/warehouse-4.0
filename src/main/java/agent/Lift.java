package agent;

import observer.Observable;
import util.Pair;
import util.Vector3D;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Lift extends Observable {

    public static double speed = 0.1; // in second per distance unit
    private static int LIFT_ID = 0;
    private final int id, height;
    private final Vector3D position;
    private final PriorityQueue<Pair<Double, Mobile>>[] queues;

    private final int level;

    public Lift(Vector3D position, int height) {
        super();
        this.id = LIFT_ID++;
        this.position = position;
        this.height = height;
        this.queues = new PriorityQueue[height];
        for (int z = 0; z < height; z++) {
            this.queues[z] = new PriorityQueue<>(Comparator.comparing(Pair::getFirst));
        }
        this.level = 0;
    }

    public int getId() {
        return this.id;
    }

    public Vector3D getPosition() {
        return this.position;
    }

    public int getLevel() {
        return this.level;
    }

    public void notify(double time, Mobile mobile, int level) {
        this.queues[level].add(new Pair<>(time, mobile));
    }
}