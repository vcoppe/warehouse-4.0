package brain;

import gurobi.GRBException;

public class SLAP extends ClusterLocationAssignment {

    @Override
    public int[] matchClustersToLocations(int nClusters, int nIOPoints, int nLocations, int[] locationCapacity, int[] clusterSpace, double[] clusterThroughput, double[][] clusterAffinity, double[][] locationIOPointDistance) {
        try {
            double[][] freq = new double[nClusters][nIOPoints];
            for (int i = 0; i < nClusters; i++) {
                for (int j = 0; j < nIOPoints; j++) {
                    freq[i][j] = 1.0 / nIOPoints;
                }
            }
            SLAPMIP model = new SLAPMIP(nClusters, nIOPoints, nLocations, locationCapacity, clusterSpace, clusterThroughput, freq, locationIOPointDistance);
            model.solve();
            return model.getSolution();
        } catch (GRBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
