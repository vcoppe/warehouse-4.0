package brain;

import agent.Mobile;
import gurobi.*;
import util.Pair;
import util.Vector3D;
import warehouse.Mission;
import warehouse.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MobileMissionAnticipationMatchingSelector implements MobileMissionSelector {

    private final Warehouse warehouse;

    private GRBEnv env;

    public MobileMissionAnticipationMatchingSelector(Warehouse warehouse) {
        this.warehouse = warehouse;
        try {
            this.env = new GRBEnv("matching.log");
            this.env.set(GRB.IntParam.OutputFlag, 0);
        } catch (GRBException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    @Override
    public ArrayList<Pair<Mobile, Mission>> matchMobileMission(double time, ArrayList<Mobile> mobiles, ArrayList<Mission> allMissions) {
        ArrayList<Mission> missions = allMissions.stream().filter(Mission::canStart).collect(Collectors.toCollection(ArrayList::new));

        if (mobiles.size() == 0 || missions.size() == 0) {
            return new ArrayList<>();
        }

        double[][] cost = new double[mobiles.size()][missions.size()];

        for (int i = 0; i < mobiles.size(); i++) {
            Mobile mobile = mobiles.get(i);
            double offset = 0; // compute time to be available for a new mission
            Vector3D position = null;
            if (mobile.isAvailable()) {
                Pair<Pair<Vector3D, Double>, Pair<Vector3D, Double>> pair = mobile.getTimedPositionsAt(time);
                position = pair.second.first;
                offset = pair.second.second - time;
            } else {
                Mission current = mobile.getMission();
                position = current.getEndPosition();
                offset = mobile.getPathEndTime() - time;
                if (!current.pickedUp()) {
                    offset += this.warehouse.getTravelTime(current.getStartPosition(), current.getEndPosition(), true);
                }
            }

            for (int j = 0; j < missions.size(); j++) {
                Mission mission = missions.get(j);
                cost[i][j] = Math.max(
                        offset + this.warehouse.getTravelTime(position, mission.getStartPosition(), false),
                        mission.getExpectedStartTime() - time
                );
            }
        }

        HashMap<Integer, Integer> missionIndex = new HashMap<>();
        for (int j = 0; j < missions.size(); j++) {
            missionIndex.put(missions.get(j).getId(), j);
        }

        GRBVar[][] x = new GRBVar[mobiles.size()][missions.size()];
        GRBVar[] y = new GRBVar[missions.size()];

        try {
            GRBModel model = new GRBModel(this.env);

            for (int j = 0; j < missions.size(); j++) {
                y[j] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + j);
                for (int i = 0; i < mobiles.size(); i++) {
                    x[i][j] = model.addVar(0, 1, cost[i][j], GRB.BINARY, "x_" + i + "_" + j);
                }
            }

            for (int j = 0; j < missions.size(); j++) {
                GRBLinExpr expr = new GRBLinExpr();
                for (int i = 0; i < mobiles.size(); i++) {
                    expr.addTerm(1, x[i][j]);
                }
                model.addConstr(y[j], GRB.EQUAL, expr, "c1_" + j);

                Mission mission = missions.get(j);
                expr = new GRBLinExpr();
                int nParents = 0;

                //System.out.println("mission " + mission.getId() + " depends on");
                for (Mission parent : mission.getPrecedingMissions()) {
                    if (missionIndex.containsKey(parent.getId())) {
                        //System.out.println(" - mission " + parent.getId());
                        int k = missionIndex.get(parent.getId());
                        expr.addTerm(1, y[k]);
                        nParents++;
                    }
                }

                GRBLinExpr expr2 = new GRBLinExpr();
                expr2.addTerm(nParents, y[j]);
                model.addConstr(expr2, GRB.LESS_EQUAL, expr, "c2_" + j);
            }

            for (int i = 0; i < mobiles.size(); i++) {
                GRBLinExpr expr = new GRBLinExpr();
                for (int j = 0; j < missions.size(); j++) {
                    expr.addTerm(1, x[i][j]);
                }
                model.addConstr(expr, GRB.LESS_EQUAL, 1, "c3_" + i);
            }

            GRBLinExpr expr = new GRBLinExpr();
            for (int i = 0; i < mobiles.size(); i++) {
                for (int j = 0; j < missions.size(); j++) {
                    expr.addTerm(1, x[i][j]);
                }
            }
            /*GRBLinExpr expr = new GRBLinExpr();
            for (int j = 0; j < missions.size(); j++) {
                expr.addTerm(1, y[j]);
            }*/
            model.addConstr(expr, GRB.GREATER_EQUAL, Math.min(mobiles.size(), missions.size()), "c4");

            model.optimize();

            if (model.get(GRB.IntAttr.Status) != GRB.OPTIMAL) {
                model.computeIIS();

                for (GRBConstr constr : model.getConstrs()) {
                    if (constr.get(GRB.IntAttr.IISConstr) == 1) {
                        System.out.println(constr.get(GRB.StringAttr.ConstrName));
                    }
                }
            }

            ArrayList<Pair<Mobile, Mission>> matching = new ArrayList<>();
            for (int i = 0; i < mobiles.size(); i++) {
                for (int j = 0; j < missions.size(); j++) {
                    if (((int) Math.round(x[i][j].get(GRB.DoubleAttr.X))) == 1) {
                        matching.add(new Pair<>(mobiles.get(i), missions.get(j)));
                        //System.out.println("matched mobile " + mobiles.get(i).getId() + " with mission " + missions.get(j).getId());
                        break;
                    }
                }
            }
            return matching;
        } catch (GRBException e) {
            e.printStackTrace();
        }

        return null;
    }

}
