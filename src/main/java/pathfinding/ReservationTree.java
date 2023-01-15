package pathfinding;

import java.util.ArrayList;
import java.util.TreeSet;

public class ReservationTree {

    TreeSet<Reservation> reservations;

    public ReservationTree() {
        this.reservations = new TreeSet<>();
    }

    public void remove(Reservation reservation) {
        this.reservations.remove(reservation);
    }

    public void insert(Reservation reservation) {
        this.reservations.add(reservation);
    }

    public ArrayList<Interval> getSafeIntervals(int id) {
        ArrayList<Interval> safeIntervals = new ArrayList<>();
        double start = -Double.MAX_VALUE;

        for (Reservation reservation : this.reservations) {
            if (reservation.mobileId == id) continue;
            if (start < reservation.start) {
                safeIntervals.add(new Interval(start, reservation.start));
                start = reservation.end;
            } else if (start <= reservation.end) {
                start = reservation.end;
            }
        }

        if (start < Double.MAX_VALUE) {
            safeIntervals.add(new Interval(start, Double.MAX_VALUE));
        }

        return safeIntervals;
    }

    /*private Node root;

    public ReservationTree() {}

    private ReservationTree(Node root) {
        this.root = root;
    }

    private static Node insert(Node node, Reservation reservation) {
        if (node == null) {
            node = new Node(reservation);
            return node;
        }

        if (reservation.start < node.reservation.start) {
            node.left = insert(node.left, reservation);
        } else {
            node.right = insert(node.right, reservation);
        }

        updateHeightAndMax(node);

        return rebalance(node);
    }

    public void insert(Reservation reservation) {
        this.root = insert(this.root, reservation);
    }

    private static Node remove(Node node, Reservation reservation) {
        if (node == null) {
            return null;
        }

        if (node.reservation == reservation) {
            if (node.left == null && node.right == null) {
                return null;
            } else if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            } else {
                Node minimum = findMinimum(node.right);
                node.reservation = minimum.reservation;
                node.right = remove(node.right, minimum.reservation);
            }
        } else if (reservation.start < node.reservation.start) {
            node.left = remove(node.left, reservation);
        } else {
            node.right = remove(node.right, reservation);
        }

        updateHeightAndMax(node);

        return rebalance(node);
    }

    public void remove(Reservation reservation) {
        this.root = remove(this.root, reservation);
    }

    private static Node rebalance(Node node) {
        int balance = balance(node);

        if (balance < -1) {
            if (balance(node.left) <= 0) {
                return rightRotate(node);
            } else {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        } else if (balance > 1) {
            if (balance(node.right) >= 0) {
                return leftRotate(node);
            } else {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }
        }

        return node;
    }

    private static Node rightRotate(Node originalParent) {
        Node newParent = originalParent.left;
        Node transferredNode = newParent.right;

        newParent.right = originalParent;
        originalParent.left = transferredNode;

        updateHeightAndMax(originalParent);
        updateHeightAndMax(newParent);

        return newParent;
    }

    private static Node leftRotate(Node originalParent) {
        Node newParent = originalParent.right;
        Node transferredNode = newParent.left;

        newParent.left = originalParent;
        originalParent.right = transferredNode;

        updateHeightAndMax(originalParent);
        updateHeightAndMax(newParent);

        return newParent;
    }

    private static Node findMinimum(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private static void updateHeightAndMax(Node node) {
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        node.max = Math.max(node.reservation.end, Math.max(max(node.left), max(node.right)));
    }

    private static int height(Node node) {
        if (node == null) {
            return 0;
        }
        return node.height;
    }

    private static double max(Node node) {
        if (node == null) {
            return Double.MIN_VALUE;
        }
        return node.max;
    }

    private static int balance(Node node) {
        return height(node.right) - height(node.left);
    }

    public ReservationTree clone() {
        if (this.root == null) {
            return null;
        }
        return new ReservationTree(this.root.clone());
    }

    public ArrayList<Interval> getSafeIntervals() {
        ArrayList<Interval> collisionIntervals = this.getCollisionIntervals();

        ArrayList<Interval> safeIntervals = new ArrayList<>();
        double current = - Double.MAX_VALUE;
        for (Interval interval : collisionIntervals) {
            if (current < interval.start) {
                safeIntervals.add(new Interval(current, interval.start));
            }
            current = interval.end;
        }

        if (current < Double.MAX_VALUE) {
            safeIntervals.add(new Interval(current, Double.MAX_VALUE));
        }

        return safeIntervals;
    }

    public ArrayList<Interval> getCollisionIntervals() {
        ArrayList<Interval> collisionIntervals = new ArrayList<>();

        Optional<Interval> last = computeCollisionIntervals(this.root, Optional.empty(), collisionIntervals);

        if (last.isPresent()) {
            collisionIntervals.add(last.get());
        }

        return collisionIntervals;
    }

    private static Optional<Interval> computeCollisionIntervals(Node node, Optional<Interval> interval, ArrayList<Interval> collisionIntervals) {
        if (node == null) {
            return interval;
        }

        interval = computeCollisionIntervals(node.left, interval, collisionIntervals);

        Interval fromNode = new Interval(node.reservation.start, node.reservation.end);
        if (interval.isEmpty()) {
            interval = Optional.of(fromNode);
        } else {
            Interval current = interval.get();

            if (current.overlaps(fromNode)) {
                current.merge(fromNode);
                interval = Optional.of(current);
            } else {
                collisionIntervals.add(current);
                interval = Optional.of(fromNode);
            }
        }

        interval = computeCollisionIntervals(node.right, interval, collisionIntervals);

        return interval;
    }

    static class Node {
        Node left, right;
        Reservation reservation;
        int height;
        double max;

        public Node(Reservation reservation) {
            this.reservation = reservation;
            this.height = 1;
            this.max = reservation.end;
        }

        private Node(Reservation reservation, Node left, Node right, int height, double max) {
            this.reservation = reservation;
            this.left = left;
            this.right = right;
            this.height = height;
            this.max = max;
        }

        public Node clone() {
            Node left = this.left != null ? this.left.clone() : null;
            Node right = this.right != null ? this.right.clone() : null;
            return new Node(this.reservation, left, right, this.height, this.max);
        }
    }*/

}
