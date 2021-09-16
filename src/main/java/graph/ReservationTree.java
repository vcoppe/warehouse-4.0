package graph;

import java.util.ArrayList;
import java.util.HashSet;

public class ReservationTree {

    private Node root;

    public ReservationTree() {
    }

    private ReservationTree(Node root) {
        this.root = root;
    }

    private static boolean isAvailable(Node node, Reservation reservation) {
        if (node == null) {
            return true;
        }

        if (reservation.conflicts(node.reservation)) {
            return false;
        }

        if (node.left != null && node.left.max > reservation.start) {
            if (!isAvailable(node.left, reservation)) {
                return false;
            }
        }

        if (node.right != null && node.max > reservation.start && node.reservation.start < reservation.end) {
            return isAvailable(node.right, reservation);
        }

        return true;
    }

    public ReservationTree clone() {
        if (this.root == null) {
            return null;
        }
        return new ReservationTree(this.root.clone());
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

        if (node.max < reservation.end) {
            node.max = reservation.end;
        }

        node.height = Math.max(height(node.left), height(node.right));

        int diff = height(node.left) - height(node.right);

        if (diff < -1) {
            if (reservation.start < node.right.reservation.start) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            } else {
                return leftRotate(node);
            }
        } else if (diff > 1) {
            if (reservation.start < node.left.reservation.start) {
                return rightRotate(node);
            } else {
                node.left = leftRotate(node.left);
                return rightRotate(node);
            }
        }

        return node;
    }

    private static Node rightRotate(Node originalParent) {
        Node newParent = originalParent.left;
        Node transferredNode = newParent.right;

        newParent.right = originalParent;
        originalParent.left = transferredNode;

        originalParent.height = Math.max(height(originalParent.left), height(originalParent.right)) + 1;
        newParent.height = Math.max(height(newParent.left), height(newParent.right)) + 1;

        originalParent.max = Math.max(originalParent.reservation.end, Math.max(max(originalParent.left), max(originalParent.right)));
        newParent.max = Math.max(newParent.reservation.end, Math.max(max(newParent.left), max(newParent.right)));

        return newParent;
    }

    private static Node leftRotate(Node originalParent) {
        Node newParent = originalParent.right;
        Node transferredNode = newParent.left;

        newParent.left = originalParent;
        originalParent.right = transferredNode;

        originalParent.height = Math.max(height(originalParent.left), height(originalParent.right)) + 1;
        newParent.height = Math.max(height(newParent.left), height(newParent.right)) + 1;

        originalParent.max = Math.max(originalParent.reservation.end, Math.max(max(originalParent.left), max(originalParent.right)));
        newParent.max = Math.max(newParent.reservation.end, Math.max(max(newParent.left), max(newParent.right)));

        return newParent;
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

    public void insert(Reservation reservation) {
        this.root = insert(this.root, reservation);
    }

    private static void allConflicts(Node node, Reservation reservation, ArrayList<Reservation> result) {
        if (node == null) {
            return;
        }

        if (reservation.conflicts(node.reservation)) {
            result.add(node.reservation);
        }

        if (node.left != null && node.left.max > reservation.start) {
            allConflicts(node.left, reservation, result);
        }

        if (node.right != null && node.max > reservation.start && node.reservation.start < reservation.end) {
            allConflicts(node.right, reservation, result);
        }
    }

    private static void allConflictingMobileIds(Node node, Reservation reservation, HashSet<Integer> result) {
        if (node == null) {
            return;
        }

        if (reservation.conflicts(node.reservation)) {
            result.add(node.reservation.mobileId);
        }

        if (node.left != null && node.left.max > reservation.start) {
            allConflictingMobileIds(node.left, reservation, result);
        }

        if (node.right != null && node.max > reservation.start && node.reservation.start < reservation.end) {
            allConflictingMobileIds(node.right, reservation, result);
        }
    }

    public boolean isAvailable(Reservation reservation) {
        return isAvailable(this.root, reservation);
    }

    public ArrayList<Reservation> allConflicts(Reservation reservation) {
        ArrayList<Reservation> result = new ArrayList<>();
        allConflicts(this.root, reservation, result);
        return result;
    }

    public HashSet<Integer> allConflictingMobileIds(Reservation reservation) {
        HashSet<Integer> result = new HashSet<>();
        allConflictingMobileIds(this.root, reservation, result);
        return result;
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
    }

}
