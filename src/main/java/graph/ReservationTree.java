package graph;

import java.util.ArrayList;
import java.util.HashSet;

public class ReservationTree {

    private Node root;

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

    private static Reservation firstConflict(Node node, Reservation reservation) {
        if (node == null) {
            return null;
        }

        if (node.left != null && node.left.max > reservation.start) {
            Reservation conflict = firstConflict(node.left, reservation);
            if (conflict != null) {
                return conflict;
            }
        }

        if (reservation.conflicts(node.reservation)) {
            return node.reservation;
        }

        if (node.right != null && node.max > reservation.start && node.reservation.start < reservation.end) {
            return firstConflict(node.right, reservation);
        }

        return null;
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

    public void insert(Reservation reservation) {
        this.root = insert(this.root, reservation);
    }

    public Reservation firstConflict(Reservation reservation) {
        return firstConflict(this.root, reservation);
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
        double max;
        int height;

        public Node(Reservation reservation) {
            this.reservation = reservation;
            this.max = reservation.end;
            this.height = 1;
        }
    }

}
