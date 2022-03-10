package warehouse;

import brain.ClusterLocationAssignment;
import brain.ProductClustering;
import util.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Scenario {

    // generates distribution of pallet types
    // (later) distribution evolves over time

    private static final Random random = new Random(0);
    public final int nLocations, nProducts, nIOPoints, nDocks;
    public final ArrayList<Vector3D> locations;
    public final int[] productSpace, locationCapacity;
    public final double[][] dist, freq, productAffinity;
    public final double[] productThroughput, dockThroughput, productionLineInThroughput, productionLineOutThroughput;
    private final Configuration configuration;
    private ArrayList<Rule> rules;

    public Scenario(Configuration configuration, int nProducts) {
        this.configuration = configuration;
        this.nProducts = nProducts;

        this.nDocks = this.configuration.docks.size();
        int nProductionLines = this.configuration.productionLines.size();

        this.locations = new ArrayList<>(this.configuration.stock.getStockPositions());
        Collections.sort(this.locations);
        this.nLocations = this.locations.size();
        this.locationCapacity = new int[this.nLocations];
        Arrays.fill(this.locationCapacity, 1);

        this.nIOPoints = nDocks + nProductionLines * 2; // all docks + production lines (in + out)

        this.dist = new double[this.nLocations][this.nIOPoints];
        for (int i = 0; i < this.nLocations; i++) {
            Vector3D p1 = this.locations.get(i);
            for (int j = 0; j < this.nIOPoints; j++) {
                Vector3D p2;
                if (j < nDocks) {
                    p2 = this.configuration.docks.get(j).getPosition();
                } else if (j % 2 == 0) {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getStartBuffer().get(0); // could take average of all buffer positions
                } else {
                    p2 = this.configuration.productionLines.get((j - nDocks) / 2).getEndBuffer().get(0); // could take average of all buffer positions
                }
                this.dist[i][j] = this.configuration.warehouse.getDistance(p1, p2).norm();
            }
        }

        this.productSpace = new int[this.nProducts];
        this.freq = new double[this.nProducts][this.nIOPoints];
        this.productThroughput = new double[this.nProducts];
        this.productAffinity = new double[this.nProducts][this.nProducts];

        int totalPallets = 0;
        for (int i = 0; i < this.nProducts; i++) {
            this.productSpace[i] = 1 + random.nextInt(this.nLocations - totalPallets - (this.nProducts - 1 - i));
            totalPallets += this.productSpace[i];
            this.productThroughput[i] = 10 * random.nextDouble() * this.productSpace[i];

            double totalFreq = 0;
            for (int j = 0; j < this.nIOPoints; j++) {
                this.freq[i][j] = random.nextInt(1000);
                totalFreq += this.freq[i][j];
            }
            for (int j = 0; j < this.nIOPoints; j++) {
                this.freq[i][j] /= totalFreq;
            }

            for (int j = i + 1; j < this.nProducts; j++) {
                this.productAffinity[i][j] = this.productAffinity[j][i] = random.nextDouble() * random.nextDouble();
            }
        }


        this.dockThroughput = new double[this.nProducts];
        this.productionLineInThroughput = new double[this.nProducts];
        this.productionLineOutThroughput = new double[this.nProducts];
        for (int i = 0; i < this.nProducts; i++) {
            for (int j = 0; j < this.nIOPoints; j++) {
                if (j < nDocks) {
                    this.dockThroughput[i] += this.productThroughput[i] * this.freq[i][j];
                } else if (j % 2 == 0) { // start
                    this.productionLineInThroughput[i] += this.productThroughput[i] * this.freq[i][j];
                } else { // end
                    this.productionLineOutThroughput[i] += this.productThroughput[i] * this.freq[i][j];
                }
            }
        }
    }

    public static int pickFromDistribution(double[] dist) {
        double total = 0;
        double[] normDist = new double[dist.length];
        for (int i = 0; i < dist.length; i++) {
            total += dist[i];
        }
        for (int i = 0; i < normDist.length; i++) {
            normDist[i] = dist[i] / total;
        }

        total = 0;
        double number = random.nextDouble();
        for (int i = 0; i < normDist.length; i++) {
            total += normDist[i];
            if (number <= total) {
                return i;
            }
        }

        return -1;
    }

    public void createZones() {
        int[] productCluster = configuration.productClustering.cluster(this.nProducts, this.productSpace, this.productThroughput, this.productAffinity);
        int nClusters = ProductClustering.getNumberOfClusters(this.nProducts, productCluster);
        int[] clusterSpace = ProductClustering.getClusterSpace(this.nProducts, nClusters, productCluster, this.productSpace);
        double[] clusterThroughput = ProductClustering.getClusterThroughput(this.nProducts, nClusters, productCluster, this.productThroughput);
        double[][] clusterAffinity = ProductClustering.getClusterAffinity(this.nProducts, nClusters, productCluster, this.productAffinity);
        int[] assignment = configuration.clusterLocationAssignment.matchClustersToLocations(nClusters, this.nIOPoints, this.nLocations, this.locationCapacity, clusterSpace, clusterThroughput, clusterAffinity, this.dist);
        this.rules = ClusterLocationAssignment.getRules(productCluster, assignment, this.locations.toArray(new Vector3D[0]));
        for (Rule rule : this.rules) {
            configuration.stock.filter.add(rule);
        }
    }

    public void initStock() {
        for (int i = 0; i < this.nProducts; i++) {
            int nPallets = this.productSpace[i] / 2 + random.nextInt(1 + this.productSpace[i] / 2);
            for (int j = 0; j < nPallets; j++) {
                Pallet pallet = new Pallet(i);
                for (Rule rule : this.rules) {
                    if (rule.matches(pallet)) {
                        for (Vector3D position : rule.getPositions()) {
                            if (this.configuration.stock.isFree(position)) {
                                this.configuration.stock.add(position, pallet);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

}
