package brain;

import util.Vector3D;
import warehouse.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class ClusterLocationAssignment {

    public static ArrayList<Rule> getRules(int[] productCluster, int[] locationCluster, Vector3D[] locations) {
        int nProducts = productCluster.length, nLocations = locations.length;

        HashMap<Integer, HashSet<Integer>> clusterProducts = new HashMap<>();
        for (int i = 0; i < nProducts; i++) {
            if (!clusterProducts.containsKey(productCluster[i])) {
                clusterProducts.put(productCluster[i], new HashSet<>());
            }
            clusterProducts.get(productCluster[i]).add(i);
        }

        HashMap<Integer, ArrayList<Vector3D>> clusterLocations = new HashMap<>();
        for (int i = 0; i < nLocations; i++) {
            if (!clusterLocations.containsKey(locationCluster[i])) {
                clusterLocations.put(locationCluster[i], new ArrayList<>());
            }
            clusterLocations.get(locationCluster[i]).add(locations[i]);
        }

        ArrayList<Rule> rules = new ArrayList<>();
        for (int i : clusterLocations.keySet()) {
            if (i == -1) { // no cluster
                rules.add(new Rule(Integer.MAX_VALUE, true, pallet -> true, clusterLocations.get(i)));
            } else {
                rules.add(new Rule(1, true, pallet -> pallet.getProduct() == i /*clusterProducts.get(i).contains(pallet.getProduct())*/, clusterLocations.get(i)));
            }
        }

        return rules;
    }

    public abstract int[] matchClustersToLocations(int nClusters, int nIOPoints, int nLocations, int[] locationCapacity, int[] clusterSpace, double[] clusterThroughput, double[][] clusterAffinity, double[][] locationIOPointDistance);

}
