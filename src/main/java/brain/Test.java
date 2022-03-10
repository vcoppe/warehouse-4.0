package brain;

import gurobi.*;
import util.Vector3D;

import java.util.Arrays;
import java.util.Random;

public class Test {

    private final GRBEnv env;
    private final GRBModel model;
    private final GRBVar[][] x, f;

    public Test(int n, int m, int l, int[] S, double[] T, double[][] D, double[][] A) throws GRBException {
        env = new GRBEnv("slap.log");
        model = new GRBModel(env);

        x = new GRBVar[n][l];
        f = new GRBVar[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                //f[i][j] = model.addVar(0, 1, 0, GRB.CONTINUOUS, "f_"+i+"_"+j);
                f[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "f_" + i + "_" + j);
            }

            for (int k = 0; k < l; k++) {
                x[i][k] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + i + "_" + k);
            }
        }

        GRBQuadExpr obj = new GRBQuadExpr();
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < l; k++) {
                for (int j = 0; j < m; j++) {
                    for (int i2 = 0; i2 < n; i2++) {
                        if (i == i2) {
                            obj.addTerm(D[j][k] * T[i] / S[i], x[i][k], f[i][j]);
                        } else {
                            obj.addTerm(D[j][k] * T[i] / S[i] * A[i][i2] * T[i2] / T[i], x[i][k], f[i2][j]);
                        }
                    }
                }
            }
        }
        model.setObjective(obj);

        GRBLinExpr lhs;
        for (int i = 0; i < n; i++) {
            lhs = new GRBLinExpr();
            for (int k = 0; k < l; k++) {
                lhs.addTerm(1, x[i][k]);
            }
            model.addConstr(lhs, GRB.GREATER_EQUAL, S[i], "c1_" + i);
        }

        for (int k = 0; k < l; k++) {
            lhs = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                lhs.addTerm(1, x[i][k]);
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "c2_" + k);
        }

        for (int i = 0; i < n; i++) {
            lhs = new GRBLinExpr();
            for (int j = 0; j < m; j++) {
                lhs.addTerm(1, f[i][j]);
            }
            model.addConstr(lhs, GRB.GREATER_EQUAL, 1, "c3_" + i);
        }
    }

    public static void main(String[] args) throws GRBException {
        int n = 6;
        int m = 10;
        int l = (2 * m + 1) * 10;

        int[] S = new int[n];
        double[] T = new double[n];
        double[][] D = new double[m][l];
        double[][] A = new double[n][n];

        int[] prods;
        double[][] freqs;

        Random random = new Random(0);
        int rem = l;
        for (int i = 0; i < n; i++) {
            do {
                S[i] = i == n - 1 ? rem : random.nextInt(rem / 2 + 1);
            } while (S[i] == 0);
            T[i] = 10 + random.nextInt(20);
            rem -= S[i];

            for (int j = i + 1; j < n; j++) {
                A[i][j] = A[j][i] = random.nextDouble() * random.nextDouble();
            }
        }

        for (int j = 0; j < m; j++) {
            Vector3D pos1 = new Vector3D(1 + j * 2, 0);
            for (int k = 0; k < l; k++) {
                Vector3D pos2 = new Vector3D(k % (2 * m + 1), 2 + k / (2 * m + 1));
                D[j][k] = pos1.manhattanDistance2D(pos2);
            }
        }

        Test t = new Test(n, m, l, S, T, D, A);
        t.solve();

        /*Test2 t2 = new Test2(n, m, l, S, T, D, A);
        t2.solve();*/

        for (int i = 0; i < n; i++) {
            System.out.println(S[i] + " " + T[i] + " -> " + (T[i] / S[i]));
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%.3f ", A[i][j]);
            }
            System.out.println();
        }

        System.out.println("obj : " + t.objVal());
        System.out.println("time: " + t.runTime());

        prods = t.getProducts();
        for (int i = 0; i < prods.length; i++) {
            System.out.print(prods[i] + " ");
            if (i % (2 * m + 1) == 2 * m) System.out.println();
        }
        System.out.println();

        freqs = t.getFrequencies();
        for (int i = 0; i < freqs.length; i++) {
            for (int j = 0; j < freqs[i].length; j++) {
                System.out.print(freqs[i][j] + " ");
            }
            System.out.println();
        }

        /*System.out.println("obj : " + t2.objVal());
        System.out.println("time: " + t2.runTime());

        prods = t2.getProducts();
        for (int i=0; i<prods.length; i++) {
            System.out.print(prods[i] + " ");
            if (i % (2*m+1) == 2*m) System.out.println();
        }
        System.out.println();

        freqs = t2.getFrequencies();
        for (int i=0; i<freqs.length; i++) {
            for (int j=0; j<freqs[i].length; j++) {
                System.out.print(freqs[i][j] + " ");
            }
            System.out.println();
        }*/
    }

    public void solve() throws GRBException {
        solve(Integer.MAX_VALUE);
    }

    public void solve(double timeLimit) throws GRBException {
        model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        model.optimize();
    }

    public double gap() throws GRBException {
        return model.get(GRB.DoubleAttr.MIPGap);
    }

    public double runTime() throws GRBException {
        return model.get(GRB.DoubleAttr.Runtime);
    }

    public double objVal() throws GRBException {
        return model.get(GRB.DoubleAttr.ObjVal);
    }

    public int[] getProducts() throws GRBException {
        int[] sol = new int[x[0].length];
        Arrays.fill(sol, -1);
        for (int j = 0; j < x[0].length; j++) {
            for (int i = 0; i < x.length; i++) {
                int value = (int) Math.round(x[i][j].get(GRB.DoubleAttr.X));
                if (value == 1) {
                    sol[j] = i;
                }
            }
        }
        return sol;
    }

    public double[][] getFrequencies() throws GRBException {
        double[][] sol = new double[f.length][f[0].length];
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[0].length; j++) {
                sol[i][j] = f[i][j].get(GRB.DoubleAttr.X);
            }
        }
        return sol;
    }

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
