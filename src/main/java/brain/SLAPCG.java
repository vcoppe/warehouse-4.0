package brain;

import gurobi.*;
import util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class SLAPCG {

    private GRBEnv env;
    private GRBModel master;
    private ArrayList<GRBVar> lambda;

    private int n, m, l;
    private int[] S, C;
    private double[] T;
    private double[][] cost;

    private ArrayList<int[][]> patterns;
    private HashSet<Integer> patternsHash;
    private GRBConstr[] locationConstraints, capacityConstraints;

    public SLAPCG(int n, int m, int l, int[] S, double[] T, int[] C, double[][] P, double[][] D) throws GRBException {
        env = new GRBEnv("slap.log");
        master = new GRBModel(env);

        this.patternsHash = new HashSet<>();

        this.n = n;
        this.m = m;
        this.l = l;
        this.S = S;
        this.T = T;
        this.C = C;

        cost = new double[n][l];
        for (int i=0; i<n; i++) {
            for (int k=0; k<l; k++) {
                for (int j=0; j<m; j++) {
                    cost[i][k] += P[i][j] * D[k][j];
                }
            }
        }

        patterns = greedyPatterns();

        // create model
        lambda = new ArrayList<>();

        for (int p=0; p<patterns.size(); p++) {
            int[][] pattern = patterns.get(p);
            patternsHash.add(Arrays.deepHashCode(pattern));
            lambda.add(master.addVar(0, 1, this.patternWeight(pattern), GRB.BINARY, "lambda_"+p));
        }

        master.update();

        GRBLinExpr lhs;

        locationConstraints = new GRBConstr[l];
        for (int k=0; k<l; k++) {
            lhs = new GRBLinExpr();

            for (int p=0; p<patterns.size(); p++) {
                int[][] pattern = patterns.get(p);
                for (int i=0; i<n; i++) if (pattern[i][k] == 1) {
                    lhs.addTerm(1, lambda.get(p));
                }
            }

            locationConstraints[k] = master.addConstr(lhs, GRB.LESS_EQUAL, 1, "loc_"+k);
        }

        capacityConstraints = new GRBConstr[n];
        for (int i=0; i<n; i++) {
            lhs = new GRBLinExpr();

            for (int p=0; p<patterns.size(); p++) {
                int[][] pattern = patterns.get(p);
                for (int k=0; k<l; k++) if (pattern[i][k] == 1) {
                    lhs.addTerm(C[k], lambda.get(p));
                }
            }

            capacityConstraints[i] = master.addConstr(lhs, GRB.GREATER_EQUAL, S[i], "cap_"+i);
        }

        master.update();
    }

    public void solve() throws GRBException {
        long start = System.currentTimeMillis();

        int it = 0;
        for (; ; it++) {
            GRBModel relax = master.relax();
            relax.set(GRB.IntParam.OutputFlag, 0);
            relax.optimize();

            //System.out.print("pi: ");
            double[] pi = new double[l];
            for (int k = 0; k < l; k++) {
                pi[k] = relax.getConstrByName("loc_" + k).get(GRB.DoubleAttr.Pi);
                //System.out.print(pi[k] + " ");
            }
            //System.out.println();

            //System.out.print("rho: ");
            double[] rho = new double[n];
            for (int i=0; i<n; i++) {
                rho[i] = relax.getConstrByName("cap_"+i).get(GRB.DoubleAttr.Pi);
                //System.out.print(rho[i] + " ");
            }
            //System.out.println();

            boolean newColumn = false;
            ArrayList<int[][]> newPatterns = newPatterns(pi, rho);
            for (int p=0; p<newPatterns.size(); p++) {
                int[][] pattern = newPatterns.get(p);

                int hash = Arrays.deepHashCode(pattern);
                if (patternsHash.contains(hash)) {
                    //System.out.println("pattern already in model");
                    continue;
                }
                patternsHash.add(hash);

                double reducedCost = 0;
                int capacity = 0, patternType = 0;

                for (int i=0; i<n; i++) {
                    for (int k=0; k<l; k++) if (pattern[i][k] == 1) {
                        reducedCost += pattern[i][k] * (T[i] / S[i] * C[k] * cost[i][k] - pi[k] - C[k] * rho[i]);
                        capacity++;
                        patternType = i;
                    }
                }

                if (reducedCost < 0) { // add epsilon ?
                    newColumn = true;
                    patterns.add(pattern);

                    GRBColumn column = new GRBColumn();

                    for (int k = 0; k < l; k++) {
                        if (pattern[patternType][k] == 1) {
                            column.addTerm(1, locationConstraints[k]);
                        }
                    }

                    column.addTerm(capacity, capacityConstraints[patternType]);

                    //System.out.println("adding pattern: " + lambda.size() + " of cost=" + this.patternWeight(pattern));
                    lambda.add(master.addVar(0, 1, this.patternWeight(pattern), GRB.BINARY, column, "lambda_" + lambda.size()));
                }
            }

            if (!newColumn) {
                System.out.println("No column with negative reduced cost found");
                break;
            }

            master.update();

            if (it % 10 == 0) System.out.println("Iteration " + it + ": " + patterns.size() + " patterns");
        }

        System.out.println("Solving master problem");
        master.optimize();

        long end = System.currentTimeMillis();

        System.out.println("Column generation summary");
        System.out.println(it + " iterations");
        System.out.println(patterns.size() + " patterns");
        System.out.println(((end - start) / 1000) + " seconds");
        double cost = 0;
        for (int i = 0; i < n; i++) cost += patternWeight(patterns.get(i));
        System.out.println(cost + " initial obj");
        System.out.println(master.get(GRB.DoubleAttr.ObjVal) + " final obj");
    }

    private ArrayList<int[][]> greedyPatterns() {
        // generate initial patterns
        ArrayList<Pair<Integer, Double>> typeOrder = new ArrayList<>();
        for (int i = 0; i < n; i++) typeOrder.add(new Pair<>(i, T[i] / S[i]));
        typeOrder.sort((t1, t2) -> Double.compare(t2.second, t1.second)); // high throughput first

        boolean[] taken = new boolean[l];
        ArrayList<int[][]> newPatterns = new ArrayList<>();
        for (Pair<Integer, Double> pair : typeOrder) {
            int i = pair.first;

            ArrayList<Pair<Integer, Double>> locationOrder = new ArrayList<>();
            for (int k = 0; k < l; k++)
                locationOrder.add(new Pair<>(k, T[i] / S[i] * C[k] * cost[i][k]));
            locationOrder.sort(Comparator.comparingDouble(Pair::getSecond));

            int reservedCapacity = 0;
            int[][] pattern = new int[n][l];
            for (Pair<Integer, Double> pair1 : locationOrder) {
                int k = pair1.first;
                if (!taken[k]) {
                    taken[k] = true;
                    pattern[i][k] = 1;
                    reservedCapacity += C[k];

                    if (reservedCapacity >= S[i]) break;
                }
            }

            newPatterns.add(pattern);
        }

        return newPatterns;
    }

    private ArrayList<int[][]> generatePatterns(double[] pi, double[] rho) {
        // generate initial patterns
        ArrayList<Pair<Integer, Double>> typeOrder = new ArrayList<>();
        for (int i = 0; i < n; i++) typeOrder.add(new Pair<>(i, T[i] / S[i]));
        typeOrder.sort((t1, t2) -> Double.compare(t2.second, t1.second)); // high throughput first

        ArrayList<int[][]> newPatterns = new ArrayList<>();
        for (int first = 0; first < n; first++) {
            if (typeOrder.size() == n) typeOrder.add(0, new Pair<>(first, 0.0));
            else typeOrder.set(0, new Pair<>(first, 0.0));

            boolean[] taken = new boolean[l];
            boolean isFirst = true;

            for (Pair<Integer, Double> pair : typeOrder) {
                int i = pair.first;
                if (i == first) {
                    if (isFirst) isFirst = false;
                    else continue;
                }

                ArrayList<Pair<Integer, Double>> locationOrder = new ArrayList<>();
                for (int k = 0; k < l; k++)
                    locationOrder.add(new Pair<>(k, T[i] / S[i] * C[k] * cost[i][k] - pi[k] - rho[i] * C[k]));
                locationOrder.sort(Comparator.comparingDouble(Pair::getSecond));

                int reservedCapacity = 0;
                int[][] pattern = new int[n][l];
                for (Pair<Integer, Double> pair1 : locationOrder) {
                    int k = pair1.first;
                    if (!taken[k]) {
                        taken[k] = true;
                        pattern[i][k] = 1;
                        reservedCapacity += C[k];

                        if (reservedCapacity >= S[i]) break;
                    }
                }

                newPatterns.add(pattern);
            }
        }

        return newPatterns;
    }

    private ArrayList<int[][]> newPatterns(double[] pi, double[] rho) {
        ArrayList<int[][]> newPatterns = new ArrayList<>();

        for (int i = 0; i < n; i++) { // create one pattern for each product type
            ArrayList<Pair<Integer, Double>> locationOrder = new ArrayList<>();
            for (int k = 0; k < l; k++)
                locationOrder.add(new Pair<>(k, T[i] / S[i] * C[k] * cost[i][k] - pi[k] - rho[i] * C[k]));
            locationOrder.sort(Comparator.comparingDouble(Pair::getSecond));

            int reservedCapacity = 0;
            int[][] pattern = new int[n][l];
            for (Pair<Integer, Double> pair1 : locationOrder) {
                int k = pair1.first;
                pattern[i][k] = 1;
                reservedCapacity += C[k];

                if (reservedCapacity >= S[i]) break;
            }

            newPatterns.add(pattern);
        }

        return newPatterns;
    }

    private double patternWeight(int[][] pattern) {
        double weight = 0;
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < l; k++) {
                weight += T[i] / S[i] * C[k] * pattern[i][k] * cost[i][k];
            }
        }
        return weight;
    }

    public double gap() throws GRBException {
        return master.get(GRB.DoubleAttr.MIPGap);
    }

    public double runTime() throws GRBException {
        return master.get(GRB.DoubleAttr.Runtime);
    }

    public double objVal() throws GRBException {
        return master.get(GRB.DoubleAttr.ObjVal);
    }

    public int[] getSolution() throws GRBException {
        int[] sol = new int[l];
        Arrays.fill(sol, -1);

        for (int p=0; p<patterns.size(); p++) {
            int value = (int) lambda.get(p).get(GRB.DoubleAttr.X);
            if (value == 1) {
                int[][] pattern = patterns.get(p);
                for (int i=0; i<n; i++) {
                    for (int k=0; k<l; k++) {
                        if (pattern[i][k] == 1) {
                            sol[k] = i;
                        }
                    }
                }
            }
        }

        return sol;
    }

    public void dispose() throws GRBException {
        master.dispose();
        env.dispose();
    }

}
