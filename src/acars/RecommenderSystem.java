/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 * @author TOWSIF AHMED
 */
public class RecommenderSystem {
    int mxmid = 3952;
    int mxuid = 6041;

    List < List < Integer >> userCluster = new ArrayList < List < Integer >> (mxuid + 1);
    List < List < Integer >> userClusterTest = new ArrayList < List < Integer >> (mxuid + 1);
    List < List < Integer >> arrayListofCluster = new ArrayList < List < Integer >> (mxuid + 1);

    List < List < Integer >> itemCluster = new ArrayList < List < Integer >> (mxuid + 1);
    List < List < Integer >> itemClusterTest = new ArrayList < List < Integer >> (mxuid + 2);

    Map < String, Integer > m = new TreeMap < String, Integer > ();

    boolean[][] flag = new boolean[mxmid + 1][mxmid + 1];

    /* catagorize information start*/
    double[][] rat = new double[mxuid + 1][mxmid + 1];
    double[][] Rat = new double[mxuid + 1][mxmid + 1];
    double[][] predRat = new double[mxuid + 1][mxmid + 1];

    double[][] diff = new double[mxuid + 1][mxuid + 1];
    double[][] finalMatrix = new double[mxuid + 1][mxuid + 1];

    double[] itemSum = new double[mxmid + 1];
    double[] itemAvg = new double[mxmid + 1];
    boolean[] itemFlag = new boolean[mxmid + 1];
    int totalRat = 0;
    boolean litmus = false;
    String inputPathPrefix, outputPathPrefix, prefix;
    ArrayList < Integer > clusterCentroids;
    Double[] curr = new Double[mxmid + 1];

    Integer[] indexes;

    RecommenderSystem(String prefix, String inPrefix, String outPrefix) {
        this.prefix = prefix;
        this.inputPathPrefix = inPrefix;
        this.outputPathPrefix = outPrefix;
        init();
    }

    void init() {
        for (int i = 0; i <= mxmid; i++) {
            itemCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i <= mxuid; i++) {
            userCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i <= mxuid; i++) {
            userClusterTest.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i <= mxmid; i++) {
            itemClusterTest.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            arrayListofCluster.add(new ArrayList < Integer > ());
        }
    }

    // Reading the train.csv file
    public void takeTrainData() throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(inputPathPrefix + "train.csv"));
        String text;
        String[] cut;
        int uid = 0, mid = 0, r = 0, t = 0;
        while ((text = in .readLine()) != null) {

            cut = text.split(",");
            // System.out.println(text);
            uid = Integer.parseInt(cut[0]);
            mid = Integer.parseInt(cut[1]);
            r = Integer.parseInt(cut[2]);
            t = Integer.parseInt(cut[3]);
            // System.out.println(uid+" + "+ mid +" + "+ r +" + "+t);
            rat[uid][mid] = r;

            itemCluster.get(mid).add(uid);
            userCluster.get(uid).add(mid);
            // itemCluster.get(mid).add(uid);
            itemSum[mid] += r;
        }

        /*Calculate the average of all user*/
        int sz = 0;
        for (int i = 1; i <= mxmid; i++) {
            sz = itemCluster.get(i).size();
            if (sz != 0) {
                itemAvg[i] = (itemSum[i] / sz);
            } else {
                itemAvg[i] = 0;
            }
            // System.out.println(usrAvg[i]);
        }

        // Reading the test.csv file
        in = new BufferedReader(new FileReader(inputPathPrefix + "test.csv"));

        while ((text = in .readLine()) != null) {
            cut = text.split(",");

            uid = Integer.parseInt(cut[0]);
            mid = Integer.parseInt(cut[1]);
            r = Integer.parseInt(cut[2]);
            t = Integer.parseInt(cut[3]);
            // System.out.println(uid+" + "+ mid +" + "+ r +" + "+t);
            Rat[uid][mid] = r;
            itemClusterTest.get(mid).add(uid);
            totalRat++;
        }
    }

    // The MATRIX to Fill
    void fillMatrix() {
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            for (int p = 0; p < arrayListofCluster.get(centroid).size(); p++) {
                int currentUser = arrayListofCluster.get(centroid).get(p);
                for (int q = 0; q < arrayListofCluster.get(centroid).size(); q++) {
                    int currentNextUser = arrayListofCluster.get(centroid).get(q);
                    finalMatrix[currentUser][currentNextUser] += 1;
                    //finalMatrix[currentUser][centroid] += 1;
                }
                //System.out.print(arrayListofCluster.get(centroid).get(p)+", ");
            }
            System.out.println();
        }
    }

    // Normalizing values
    double normalize(double val) {
        double new_min = 0.2;
        double new_max = 1;
        double minVal = 1;
        double maxVal = 5;
        double newVal = 0;

        newVal = ((val - minVal) * (new_max - new_min)) / (maxVal - minVal) + new_min;

        return newVal;
    }

    void calculateDistance() {
        for (int u = 1; u < mxuid; u++) {
            for (int v = 1; v < mxuid; v++) {
                List < Integer > userList = userCluster.get(v);
                int itemSize = userList.size();
                int commonCounter = 0;
                for (int movieIndex = 0; movieIndex < itemSize; movieIndex++) {
                    int movieId = userList.get(movieIndex);
                    if (rat[v][movieId] != 0) {
                        commonCounter++;
                        diff[u][v] += Math.abs(normalize(rat[u][movieId]) - normalize(rat[v][movieId]));
                    }
                }
                if (commonCounter != 0) {
                    diff[u][v] /= commonCounter;
                } else {
                    diff[u][v] = 1;
                }
            }
            //for (int v = 1; v < mxuid; v++){
            //    System.out.println(diff[u][v]);
            //}
        }
    }

    // Choose K unique centroids randomly from the dataset. Here K = 100
    ArrayList < Integer > uniqueRandomInRange(ArrayList < Integer > clusterCentroids) {
        int numofCluster = 100;
        Random rand = new Random();
        while (numofCluster > 0) {
            int n = rand.nextInt(6040) + 1;
            if (clusterCentroids.size() == 0) {
                clusterCentroids.add(n);
                numofCluster--;
            } else {
                boolean flag = false;
                for (int j = 0; j < clusterCentroids.size(); j++) {
                    if (clusterCentroids.get(j) == n) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    clusterCentroids.add(n);
                    numofCluster--;
                }
            }
        }
        return clusterCentroids;
    }

    // Choose random 1 centroid from every 100 objects orderly. 6040 users So, K = 61
    ArrayList < Integer > randomInRange(ArrayList < Integer > clusterCentroids) {
        Random r = new Random();
        for (int userId = 1; userId + 100 < mxuid; userId = userId + 100) {
            int Low = userId;
            int High = userId + 100;
            int Result = r.nextInt(High - Low) + Low;
            clusterCentroids.add(Result);
        }
        // As, 6001-6040 there is only 40 objects
        int Low = 6001;
        int High = 6040;
        int Result = r.nextInt(High - Low) + Low;
        clusterCentroids.add(Result);

        return clusterCentroids;
    }

    // Display cluster centroids
    void displayClusterCentroids() {
        System.out.println("Centroids of clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            System.out.println((i + 1) + " | " + clusterCentroids.get(i));
        }
    }

    // Display Clusters
    void displayClusters() {
        System.out.println("Clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i));    // displays centroid
            for (int p = 0; p < arrayListofCluster.get(centroid).size(); p++) {
                System.out.print(arrayListofCluster.get(centroid).get(p) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofCluster.get(centroid).size());   // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }

    // Display number of objects in clusters
    void displayTotalNumOfObjectsInClusters() {
        System.out.println("Number of items in each clusters:");
        int totalObjectInClusters = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int currentCentroid = clusterCentroids.get(i);
            totalObjectInClusters += arrayListofCluster.get(currentCentroid).size();
            System.out.println((i + 1) + " , " + arrayListofCluster.get(currentCentroid).size());
        }
        System.out.println("Total number of Objects in all clusters: ");
        System.out.println(totalObjectInClusters);
    }


    // ============================================================ //
    // K-Means Clustering
    // ============================================================ //
    void K_MeansClustering() {
        clusterCentroids = new ArrayList < Integer > ();
        // Choosing 1 centroid from every 100 objects orderly.
        clusterCentroids = randomInRange(clusterCentroids);
        calculateDistance();

        // Display randomly choosen centroids (K = 61)
        displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 0; i < mxuid; i++) { // i = current item
            // // Check if item itself is centroid
            // boolean isCentroid = false;
            // for (int j = 0; j < clusterCentroids.size(); j++) {
            //     int centroid = clusterCentroids.get(j);
            //     if (i == centroid) { // If item itself is centroid
            //         arrayListofCluster.get(centroid).add(i); // Add centroid to its own cluster
            //         isCentroid = true;
            //     }
            // }
            double tempMax = 1000000;
            int tempCentroid = 0;
            for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                int currentCentroid = clusterCentroids.get(k);
                // if (isCentroid == false && diff[i][currentCentroid] < tempMax) {
                if (diff[i][currentCentroid] < tempMax) {
                    tempMax = diff[i][currentCentroid]; // tempMax will contain the closest centroid distance from a object
                    tempCentroid = currentCentroid; // The closest centroid
                }
            }
            if (tempCentroid != 0)
                arrayListofCluster.get(tempCentroid).add(i);
        }

        // Display initial clusters
        System.out.println("initial clusters:");
        displayClusters();

        // Display total number of objects in all clusters
        displayTotalNumOfObjectsInClusters();
    }


    // ============================================================ //
    // K-Medoids Clustering
    // ============================================================ //

    // ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //

    // ============================================================ //
    // Mean Shift Clustering
    // ============================================================ //
}