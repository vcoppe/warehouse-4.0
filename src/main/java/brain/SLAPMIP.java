package brain;

import gurobi.*;

import java.util.Arrays;

public class SLAPMIP {

    private final GRBEnv env;
    private final GRBModel model;
    private final GRBVar[][] x;

    public SLAPMIP(int n, int m, int l, int[] C, int[] S, double[] T, double[][] P, double[][] D) throws GRBException {
        env = new GRBEnv("slap.log");
        model = new GRBModel(env);

        x = new GRBVar[n][l];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < l; k++) {
                double weightedDist = 0;
                for (int j = 0; j < m; j++) {
                    weightedDist += P[i][j] * D[k][j];
                }
                x[i][k] = model.addVar(0, 1, T[i] / S[i] * C[k] * weightedDist, GRB.BINARY, "x_" + i + "_" + k);
            }
        }

        GRBLinExpr lhs;
        for (int i = 0; i < n; i++) {
            lhs = new GRBLinExpr();
            for (int k=0; k<l; k++) {
                lhs.addTerm(C[k], x[i][k]);
            }
            model.addConstr(lhs, GRB.GREATER_EQUAL, S[i], "c1_"+i);
        }

        for (int k=0; k<l; k++) {
            lhs = new GRBLinExpr();
            for (int i=0; i<n; i++) {
                lhs.addTerm(1, x[i][k]);
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "c2_"+k);
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

    public int[] getSolution() throws GRBException {
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

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
