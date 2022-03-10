package brain;

import gurobi.*;

import java.util.Arrays;

public class Test2 {

    private final GRBEnv env;
    private final GRBModel model;
    private final GRBVar[][][] x;
    private final GRBVar[][] y;
    private final int[] S;

    public Test2(int n, int m, int l, int[] S, double[] T, double[][] D, double[][] A) throws GRBException {
        env = new GRBEnv("slap.log");
        model = new GRBModel(env);

        x = new GRBVar[n][m][l];
        y = new GRBVar[n][m];
        this.S = S;

        /*GRBLinExpr obj1 = new GRBLinExpr();
        GRBQuadExpr obj2 = new GRBQuadExpr();*/

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < l; k++) {
                    x[i][j][k] = model.addVar(0, 1, T[i] / S[i] * D[j][k], GRB.BINARY, "x_" + i + "_" + j + "_" + k);
                    //obj1.addTerm(T[i]/S[i] * D[j][k], x[i][j][k]);
                }
                y[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + i + "_" + j);
            }
        }

        GRBLinExpr lhs, rhs;
        for (int i = 0; i < n; i++) {
            lhs = new GRBLinExpr();
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < l; k++) {
                    lhs.addTerm(1, x[i][j][k]);
                }
            }
            model.addConstr(lhs, GRB.GREATER_EQUAL, S[i], "c1_" + i);
        }

        for (int k = 0; k < l; k++) {
            lhs = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    lhs.addTerm(1, x[i][j][k]);
                }
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "c2_" + k);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                lhs = new GRBLinExpr();
                for (int k = 0; k < l; k++) {
                    lhs.addTerm(1, x[i][j][k]);
                }

                rhs = new GRBLinExpr();
                rhs.addTerm(l, y[i][j]);
                model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "c3_" + i + "_" + j);

                // minimum number of locations of a class associated to the IO point
                /*rhs = new GRBLinExpr();
                rhs.addTerm(S[i] * 0.1, y[i][j]);
                model.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "c4_"+i+"_"+j);*/
            }

            // maximum number of IO points used by a class
            /*lhs = new GRBLinExpr();
            for (int j=0; j<m; j++) {
                lhs.addTerm(1, y[i][j]);
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 4, "c5_"+i);*/

            /*for (int i1=0; i1<n; i1++) {
                for (int i2=0; i2<n; i++) {
                    for (int j=0; j<m; j++) {
                        for (int k = 0; k < l; k++) {
                            obj2.addTerm(A[i1][i2] * D[j][k], y[i1][j], x[i2][j][k]);
                        }
                    }
                }
            }

            obj2.add(obj1);
            model.setObjective(obj2);*/
        }
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
        int[] sol = new int[x[0][0].length];
        Arrays.fill(sol, -1);
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                for (int k = 0; k < x[i][j].length; k++) {
                    int value = (int) Math.round(x[i][j][k].get(GRB.DoubleAttr.X));
                    if (value == 1) {
                        sol[k] = i;
                    }
                }
            }
        }
        return sol;
    }

    public double[][] getFrequencies() throws GRBException {
        double[][] sol = new double[x.length][x[0].length];
        for (int k = 0; k < x[0][0].length; k++) {
            for (int j = 0; j < x[0].length; j++) {
                for (int i = 0; i < x.length; i++) {
                    sol[i][j] += x[i][j][k].get(GRB.DoubleAttr.X) / S[i];
                }
            }
        }
        return sol;
    }

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
