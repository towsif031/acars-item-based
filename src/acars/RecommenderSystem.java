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
    int mxmid = 3953;
    int mxuid = 6041;

    List < List < Integer >> userCluster = new ArrayList < List < Integer >> (mxuid);
    List < List < Integer >> userClusterTest = new ArrayList < List < Integer >> (mxuid);
    List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);

    List < List < Integer >> itemCluster = new ArrayList < List < Integer >> (mxuid);
    List < List < Integer >> itemClusterTest = new ArrayList < List < Integer >> (mxuid);

    Map < String, Integer > m = new TreeMap < String, Integer > ();

    boolean[][] flag = new boolean[mxmid + 1][mxmid];

    /* catagorize information start*/
    double[][] rat = new double[mxuid][mxmid];
    double[][] Rat = new double[mxuid][mxmid];
    double[][] predRat = new double[mxuid][mxmid];

    double[][] diff = new double[mxuid][mxuid];
    double[][] finalMatrix = new double[mxuid][mxuid];

    double[] itemSum = new double[mxmid];
    double[] itemAvg = new double[mxmid];
    boolean[] itemFlag = new boolean[mxmid];
    int totalRat = 0;
    boolean litmus = false;
    String inputPathPrefix, outputPathPrefix, prefix;
    ArrayList < Integer > clusterCentroids;
    Double[] curr = new Double[mxmid];

    Integer[] indexes;

    RecommenderSystem(String prefix, String inPrefix, String outPrefix) {
        this.prefix = prefix;
        this.inputPathPrefix = inPrefix;
        this.outputPathPrefix = outPrefix;
        init();
    }

    void init() {
        for (int i = 0; i < mxmid; i++) {
            itemCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            userCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            userClusterTest.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxmid; i++) {
            itemClusterTest.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
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
        for (int i = 1; i < mxmid; i++) {
            int j = i - 1;
            sz = itemCluster.get(j).size();
            if (sz != 0) {
                itemAvg[j] = (itemSum[j] / sz);
            } else {
                itemAvg[j] = 0;
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
            for (int p = 0; p < arrayListofClusters.get(centroid).size(); p++) {
                int currentUser = arrayListofClusters.get(centroid).get(p);
                for (int q = 0; q < arrayListofClusters.get(centroid).size(); q++) {
                    int currentNextUser = arrayListofClusters.get(centroid).get(q);
                    finalMatrix[currentUser][currentNextUser] += 1;
                    //finalMatrix[currentUser][centroid] += 1;
                }
                //System.out.print(arrayListofClusters.get(centroid).get(p)+", ");
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

    // Distance Calculator
    void calculateDistance() {
        for (int u = 2; u < mxuid; u++) { // as there is no uid 1. so we start from uid 2 for simplicity
            for (int v = 2; v < mxuid; v++) {
                //if (userCluster.get(u).size() != 0 && userCluster.get(v).size() != 0) {
                List < Integer > userList = userCluster.get(u);
                int itemSize = userList.size();
                int commonCounter = 0;
                for (int movieIndex = 0; movieIndex < itemSize; movieIndex++) {
                    int movieId = userList.get(movieIndex);
                    if (rat[v][movieId] != 0) {
                        commonCounter++;
                        //  System.out.println("movieId: "+ movieId); // common movie they both watched
                        diff[u][v] += Math.abs(normalize(rat[u][movieId]) - normalize(rat[v][movieId]));
                    }
                }
                if (commonCounter != 0) {
                    diff[u][v] = diff[u][v] / commonCounter;
                } else {
                    diff[u][v] = 1;
                }

                System.out.println("Diff of " + u + " & " + v + " = " + diff[u][v]);
                //}
            }
        }
    }

    // //// Finding out missing uids
    // void missingUid() {
    //     for (int u = 1; u < mxuid; u++) {
    //         if (userCluster.get(u).size() == 0) {
    //             System.out.println(u);
    //         }
    //     }
    // }

    // Display distance between 2 user objects
    void distanceCalculator(int u, int v) {
        System.out.println("Distance between " + u + " and " + v + " is " + diff[u][v]);
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
            //int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i)); // displays centroid
            for (int p = 0; p < arrayListofClusters.get(i).size(); p++) {
                System.out.print(arrayListofClusters.get(i).get(p) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }

    // Display number of objects in clusters
    void displayTotalNumOfObjectsInClusters() {
        System.out.println("Number of items in each clusters:");
        int totalObjectInClusters = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            //int currentCentroid = clusterCentroids.get(i);
            totalObjectInClusters += arrayListofClusters.get(i).size();
            System.out.println((i + 1) + " , " + arrayListofClusters.get(i).size());
        }
        System.out.println("Total number of Objects in all clusters: ");
        System.out.println(totalObjectInClusters);
    }

    // Calculate total cost of clusters
    int calculateTotalCostofAllClusters() {
        int totalCostofCurrentCluster = 0;
        int totalCostofAllClusters = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int currentCentroid = clusterCentroids.get(i);
            for (int p = 0; p < arrayListofClusters.get(i).size(); p++) {
                int currentItem = arrayListofClusters.get(i).get(p);
                totalCostofCurrentCluster += diff[currentCentroid][currentItem];
            }
            totalCostofAllClusters += totalCostofCurrentCluster;
        }
        return totalCostofAllClusters;
    }


    // ============================================================ //
    // K-Means Clustering
    // ============================================================ //
    void K_MeansClustering() {
        clusterCentroids = new ArrayList < Integer > ();
        // Choosing 1 centroid from every 100 objects orderly.
        clusterCentroids = randomInRange(clusterCentroids);


        // Display randomly choosen centroids (K = 61)
        displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 2; i < mxuid; i++) { // i = current item
            // Check if item itself is centroid
            // Because sometimes 2 users' diff may be 0.0
            boolean isCentroid = false;
            for (int j = 0; j < clusterCentroids.size(); j++) {
                int centroid = clusterCentroids.get(j);
                if (i == centroid) { // If item itself is centroid
                    arrayListofClusters.get(j).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                }
            }

            double tempMax = 1000000;
            int tempCentroid = 0;
            for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                int currentCentroid = clusterCentroids.get(k);
                if (isCentroid == false && diff[i][currentCentroid] < tempMax) {
                    tempMax = diff[i][currentCentroid]; // tempMax will contain the closest centroid distance from a object
                    tempCentroid = currentCentroid; // The closest centroid
                }
            }

            int centroidPosition = 0; // centroid position in arraylist
            for (int m = 0; m < clusterCentroids.size(); m++) { // Here, clusterCentroids.size() = 61
                int matchCentroid = clusterCentroids.get(m);
                if (matchCentroid == tempCentroid) {
                    centroidPosition = m;
                    break;
                }
            }

            if (tempCentroid != 0) // keeps away all centroids from getting added again
                arrayListofClusters.get(centroidPosition).add(i);
        }

        // Display initial clusters
        //System.out.println("initial clusters:");
        //displayClusters();

        // Display total number of objects in all clusters
        displayTotalNumOfObjectsInClusters();

        //        distanceCalculator(5426, 1894);
        //        distanceCalculator(5837, 385);
        //        distanceCalculator(5837, 2312);
        //        distanceCalculator(5792, 4263);
        //        distanceCalculator(5792, 4990);
        //        distanceCalculator(5792, 4642);

        // Iterations of finding new centroids and populating
        for (int iterator = 0; iterator < 5; iterator++) {
            List < List < Integer >> arrayListofClustersTemp = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofClustersTemp.add(new ArrayList < Integer > ());
            }
            ArrayList < Integer > newClusterCentroids = new ArrayList < Integer > ();

            // Find new centroid from the cluster
            for (int i = 0; i < clusterCentroids.size(); i++) {
                int currentCentroid = clusterCentroids.get(i);
                double diffSum = 1000000;
                int newCentroid = 0;
                for (int j = 0; j < arrayListofClusters.get(i).size(); j++) { // arrayListofClusters.get(currentCentroid).size() = cluster size of current centroid
                    int currentItem = arrayListofClusters.get(i).get(j); // current item of the cluster
                    double diffSumTemp = 0;
                    for (int k = 0; k < arrayListofClusters.get(i).size(); k++) {
                        int tempItem = arrayListofClusters.get(i).get(k); // next item of the cluster
                        diffSumTemp += diff[currentItem][tempItem];
                    }

                    if (diffSumTemp < diffSum) {
                        diffSum = diffSumTemp; // store smallest diffSumTemp
                        newCentroid = currentItem; // store the item as new centroid
                    }
                }
                newClusterCentroids.add(newCentroid);
            }

            //            // Display new cluster centroids
            //            System.out.println("NEW Centroids of clusters:");
            //            for (int i = 0; i < newClusterCentroids.size(); i++) {
            //                System.out.println((i + 1) + " | " + newClusterCentroids.get(i));
            //            }

            // Convengence condition for ending iteration
            boolean isNewCentroids = false;
            for (int curr = 0; curr < newClusterCentroids.size(); curr++) {
                if (clusterCentroids.get(curr) != newClusterCentroids.get(curr)) {
                    isNewCentroids = true;
                }
            }
            if (isNewCentroids == false)
                break;

            // Updating clusterCentroids arraylist with new newClusterCentroids
            clusterCentroids = newClusterCentroids;

            // For new centroids: Again populating each cluster with closest objects to its centroid
            for (int i = 2; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < clusterCentroids.size(); j++) {
                    int centroid = clusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofClustersTemp.get(j).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                    }
                }

                double tempMax = 1000000;
                int tempCentroid = 0;
                for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                    int currentCentroid = clusterCentroids.get(k);
                    if (isNewCentroid == false && diff[i][currentCentroid] < tempMax) {
                        tempMax = diff[i][currentCentroid]; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                int centroidPosition = 0; // centroid position in arraylist
                for (int m = 0; m < clusterCentroids.size(); m++) { // Here, clusterCentroids.size() = 61
                    int matchCentroid = clusterCentroids.get(m);
                    if (matchCentroid == tempCentroid) {
                        centroidPosition = m;
                        break;
                    }
                }

                if (tempCentroid != 0)
                    arrayListofClustersTemp.get(centroidPosition).add(i);
            }

            arrayListofClusters = arrayListofClustersTemp;
            // System.out.println("Current Iteration Number : "+iterator);
        }

        System.out.println("\n=========================================================================");
        System.out.println("For New Clusters:");
        System.out.println("=========================================================================\n");

        displayClusterCentroids();

        // Display new clusters
        displayClusters();
        // System.out.println(cc);

        // Display total number of objects in all new clusters
        displayTotalNumOfObjectsInClusters();

        // Filling the MATRIX after K-Means
        //fillMatrix();
    }


    // ============================================================ //
    // K-Medoids Clustering
    // ============================================================ //
    void K_MedoidsClustering() {
        clusterCentroids = new ArrayList < Integer > (); // Arraylist of initial centroids

        // Find 100 random centroids (K) within dataset
        clusterCentroids = uniqueRandomInRange(clusterCentroids);


        // Display initial K-Medoids centroids
        displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 2; i < mxuid; i++) { // i = current item
            // Check if item itself is centroid
            // Because sometimes 2 users' diff may be 0.0
            boolean isCentroid = false;
            for (int j = 0; j < clusterCentroids.size(); j++) {
                int centroid = clusterCentroids.get(j);
                if (i == centroid) { // If item itself is centroid
                    arrayListofClusters.get(j).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                }
            }

            double tempMax = 1000000;
            int tempCentroid = 0;
            for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 100
                int currentCentroid = clusterCentroids.get(k);
                if (isCentroid == false && diff[i][currentCentroid] < tempMax) {
                    tempMax = diff[i][currentCentroid]; // tempMax will contain the closest centroid distance from a object
                    tempCentroid = currentCentroid; // The closest centroid
                }
            }

            int centroidPosition = 0; // centroid position in arraylist
            for (int m = 0; m < clusterCentroids.size(); m++) { // Here, clusterCentroids.size() = 100
                int matchCentroid = clusterCentroids.get(m);
                if (matchCentroid == tempCentroid) {
                    centroidPosition = m;
                    break;
                }
            }

            if (tempCentroid != 0) // keeps away all centroids from getting added again
                arrayListofClusters.get(centroidPosition).add(i);
        }

        // Display initial clusters
        displayClusters();

        // Display total number of objects in all clusters
        displayTotalNumOfObjectsInClusters();

        // Calculate total cost of initial clusters
        int totalCostofInitClusters = calculateTotalCostofAllClusters();

        System.out.println("Total Cost of Initial Clusters: " + totalCostofInitClusters);

        int oldCost = totalCostofInitClusters; // Saving initial total cost

        //================
        // Iterations for Finding best Medoids
        //================
        ArrayList < Integer > newClusterCentroids = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofNewClusters = new ArrayList < List < Integer >> (mxuid);

        for (int i = 0; i < mxuid; i++) {
            arrayListofNewClusters.add(new ArrayList < Integer > ());
        }

        arrayListofNewClusters = arrayListofClusters;
        newClusterCentroids = clusterCentroids;

        for (int iterator = 0; iterator < 10; iterator++) {
            // Randomly select a centroid to remove from clusterCentroids array
            Random randRem = new Random();
            int randomSelectedCentroidIndex = randRem.nextInt(clusterCentroids.size());
            int randomSelectedCentroid = clusterCentroids.get(randomSelectedCentroidIndex);
            System.out.println("Delete centroid: " + randomSelectedCentroid);

            //clusterCentroids = ArrayUtils.removeElement(clusterCentroids, randomSelectedCentroidIndex);

            // New clusterCentroids after removing randomly choosen centroid
            ArrayList < Integer > tempClusterCentroids = new ArrayList < Integer > ();
            for (int i = 0; i < clusterCentroids.size(); i++) { // Here, clusterCentroids.size() = 100
                int currentCentroid = clusterCentroids.get(i);
                if (currentCentroid != randomSelectedCentroid) {
                    tempClusterCentroids.add(currentCentroid);
                }
            }

            // Randomly selected a new unique centroid
            Random randSel = new Random();
            int newRandomCentroid = 0;
            boolean isUnique = false;
            while (!isUnique) {
                newRandomCentroid = randSel.nextInt(6040) + 1;
                if (!clusterCentroids.contains(newRandomCentroid)) {
                    isUnique = true;
                }
            }

            // // Randomly selected a new unique centroid
            // Random randSel = new Random();
            // int newRandomCentroid = 0;
            // for (int i = 1; i < mxuid; i++) {
            //     newRandomCentroid = randSel.nextInt(6040) + 1;
            //     for (int j = 0; j < clusterCentroids.size(); j++) {
            //         int centroid = clusterCentroids.get(j);
            //         if (newRandomCentroid != centroid) {
            //             break;
            //         }
            //     }
            // }

            System.out.println("New centroid: " + newRandomCentroid);

            // Add new centroid to clusterCentroids
            tempClusterCentroids.add(newRandomCentroid);

            //            // Display centroids
            //            System.out.println("tempCentroids of clusters:");
            //            for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //                System.out.println((i + 1) + " , " + tempClusterCentroids.get(i));
            //            }

            List < List < Integer >> arrayListofClustersTemp = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofClustersTemp.add(new ArrayList < Integer > ());
            }
            //  Populate each cluster with closest objects to its centroid
            for (int i = 2; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < tempClusterCentroids.size(); j++) {
                    int centroid = tempClusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofClustersTemp.get(j).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                    }
                }

                double tempMax = 1000000;
                int tempCentroid = 0;
                for (int k = 0; k < tempClusterCentroids.size(); k++) { // Here, tempClusterCentroids.size() = 100
                    int currentCentroid = tempClusterCentroids.get(k);
                    if (isNewCentroid == false && diff[i][currentCentroid] < tempMax) {
                        tempMax = diff[i][currentCentroid]; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                int centroidPosition = 0; // centroid position in arraylist
                for (int m = 0; m < tempClusterCentroids.size(); m++) { // Here, tempClusterCentroids.size() = 100
                    int matchCentroid = tempClusterCentroids.get(m);
                    if (matchCentroid == tempCentroid) {
                        centroidPosition = m;
                        break;
                    }
                }

                if (tempCentroid != 0) // keeps away all centroids from getting added again
                    arrayListofClustersTemp.get(centroidPosition).add(i);
            }

            //            // Display objects of temp clusters
            //            System.out.println("tempClusters after swapping a centroid first time:");
            //            for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //                System.out.println((i + 1) + " | centroid: " + tempClusterCentroids.get(i)); // displays centroid
            //                for (int p = 0; p < arrayListofClustersTemp.get(i).size(); p++) {
            //                    System.out.print(arrayListofClustersTemp.get(i).get(p) + ", ");
            //                }
            //                System.out.println("\n total objects: " + arrayListofClustersTemp.get(i).size()); // displays total objects
            //                System.out.println();
            //                System.out.println("================================");
            //            }

            // Calculate total cost of tempClusters
            int totalCostofCurrentCluster = 0;
            int totalCostofAllTempClusters = 0;
            for (int i = 0; i < tempClusterCentroids.size(); i++) {
                int currentCentroid = tempClusterCentroids.get(i);
                for (int p = 0; p < arrayListofClustersTemp.get(i).size(); p++) {
                    int currentItem = arrayListofClustersTemp.get(i).get(p);
                    totalCostofCurrentCluster += diff[currentCentroid][currentItem];
                }
                totalCostofAllTempClusters += totalCostofCurrentCluster;
            }

            // total cost of with new centroid
            System.out.println("Total Cost of Temp Clusters: " + totalCostofAllTempClusters + " for iteration no: " + iterator);

            int newCost = totalCostofAllTempClusters;

            int s = newCost - oldCost;
            if (s < 0) {
                newClusterCentroids = tempClusterCentroids;
                arrayListofNewClusters = arrayListofClustersTemp;
                oldCost = newCost;
            }

            //            // [TEST] Convergence Checking with ArrayList //// Not sure!
            //            if (arrayListofClusters.size() == arrayListofNewClusters.size() && arrayListofClusters.containsAll(arrayListofNewClusters) == arrayListofNewClusters.containsAll(arrayListofClusters)) {
            //                clusterCentroids = newClusterCentroids;
            //                break;
            //            }

            // Convengence condition for ending iteration
            boolean isNewCentroids = false;
            for (int curr = 0; curr < newClusterCentroids.size(); curr++) {
                if (clusterCentroids.get(curr) != newClusterCentroids.get(curr)) {
                    isNewCentroids = true;
                }
            }
            if (isNewCentroids == false)
                break;
        }

        displayClusterCentroids();
        displayClusters();
        System.out.println("Cost after iteration: " + oldCost);
        // Subtract new centroidCost from old centroidCost. If positive then old was good.

        // Filling the MATRIX after K-Medoids
        //fillMatrix();
    }






    // ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //
    void DBSCANClusteringO() {
        double eps = 0.05; // minimum epsilon
        int minPts = 10; // minimum number of points
        boolean[] flagForVisited = new boolean[mxuid]; // Mark all object as unvisited
        boolean[] isInCluster = new boolean[mxuid];
        boolean[] isNoise = new boolean[mxuid];

        Random rand = new Random();

        ArrayList < Integer > neighborObjects = new ArrayList < Integer > (); // candidate set N
        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int a = 0; a < mxuid; a++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        int clusterPosition = 0;

        for (int i = 2; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true; // Mark i as visited

                for (int j = 2; j < mxuid; j++) {
                    if (diff[i][j] <= eps) {
                        neighborObjects.add(j);
                    }
                }

                if (neighborObjects.size() >= minPts) {
                    coreObjects.add(i); // i is a core object
                    arrayListofClusters.get(clusterPosition).add(i);

                    isInCluster[i] = true;
                    for (int k = 0; k < neighborObjects.size(); k++) {
                        int p = neighborObjects.get(k);
                        if (flagForVisited[p] == false) {
                            flagForVisited[p] = true;
                            //arrayListofClusters.get(i).add(p);
                            ArrayList < Integer > neighborObjectsOfp = new ArrayList < Integer > ();
                            for (int l = 2; l < mxuid; l++) {
                                if (diff[p][l] <= eps) {
                                    neighborObjectsOfp.add(l);
                                }
                            }

                            // add neighborhood points of p to neighborObjects
                            if (neighborObjectsOfp.size() >= minPts) {
                                for (int m = 0; m < neighborObjectsOfp.size(); m++) {
                                    int n = neighborObjectsOfp.get(m);
                                    neighborObjects.add(n); ////// may add duplicates
                                }
                            }
                        }

                        if (isInCluster[p] == false) {
                            isInCluster[p] = true;
                            arrayListofClusters.get(clusterPosition).add(p);
                        }
                    }

                    clusterPosition++;
                } else {
                    isNoise[i] = true;
                }
            }
        }

        // Display clusters
        System.out.println("Clusters:");
        for (int i = 0; i < coreObjects.size(); i++) {
            System.out.println((i + 1) + " | Core object : " + coreObjects.get(i));
            for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                System.out.print(arrayListofClusters.get(i).get(j) + ", ");
            }
            System.out.println("\n total objects in the cluster: " + arrayListofClusters.get(i).size());
            System.out.println();
            System.out.println("================================");
        }

        // Display total objects in clusters
        int totalObjectInClusters = 0;
        for (int i = 0; i < coreObjects.size(); i++) {
            totalObjectInClusters += arrayListofClusters.get(i).size();
            System.out.println((i + 1) + " , " + arrayListofClusters.get(i).size());
        }
        System.out.println("Total number of Objects in all clusters: ");
        System.out.println(totalObjectInClusters);
    }






    /// ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //
    void DBSCANClustering() {
        double eps = 0.001; // minimum epsilon
        int minPts = 10; // minimum number of points
        boolean[] flagForVisited = new boolean[mxuid]; // Mark all object as unvisited
        boolean[] isInCluster = new boolean[mxuid];
        boolean[] isNoise = new boolean[mxuid];

        ArrayList < Integer > neighborObjects = new ArrayList < Integer > (); // candidate set N
        ArrayList < Integer > neighborObjectsOfp = new ArrayList < Integer > ();
        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        //int clusterPosition = 0;

        for (int i = 2; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true; // Mark i as visited

                for (int j = 2; j < mxuid; j++) {
                    if (diff[i][j] <= eps) {
                        neighborObjects.add(j); // N
                    }
                }

                if (neighborObjects.size() >= minPts) {
                    coreObjects.add(i); // i is a core object
                    isInCluster[i] = true;
                    arrayListofClusters.get(i).add(i);

                    for (int k = 0; k < neighborObjects.size(); k++) {
                        int p = neighborObjects.get(k);

                        if (isInCluster[p] == false) {
                            isInCluster[p] = true;
                            arrayListofClusters.get(i).add(p);
                        }

                        if (flagForVisited[p] == false) {
                            flagForVisited[p] = true;
                            //arrayListofClusters.get(i).add(p);
                            for (int l = 2; l < mxuid; l++) {
                                if (diff[p][l] <= eps) {
                                    neighborObjectsOfp.add(l);
                                }
                            }

                            // add neighborhood points of p to neighborObjects
                            if (neighborObjectsOfp.size() >= minPts) {
                                for (int m = 0; m < neighborObjectsOfp.size(); m++) {
                                    int n = neighborObjectsOfp.get(m);
                                    //neighborObjects.add(n); ////// may add duplicates

                                    if (isInCluster[n] == false) {
                                        isInCluster[n] = true;
                                        neighborObjects.add(n);
                                        arrayListofClusters.get(i).add(n);
                                    }
                                }
                            } else {
                                isNoise[p] = true;
                            }

                            // empty neighborObjectsOfp arraylist
                            neighborObjectsOfp.clear();
                        }
                    }

                    //clusterPosition++;
                } else {
                    isNoise[i] = true;
                }

                // empty neighborObjects arraylist
                neighborObjects.clear();
            }
        }

        // Display clusters
        //System.out.println("Clusters after DBSCAN with coreObjects: ");
        for (int i = 0; i < coreObjects.size(); i++) {
            System.out.println((i + 1) + " | Core object : " + coreObjects.get(i));
            // for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
            //     System.out.print(arrayListofClusters.get(i).get(j) + ", ");
            // }
            // System.out.println("\n total objects in the cluster: " + arrayListofClusters.get(i).size());
            // System.out.println();
            // System.out.println("================================");
        }

        // Display clusters
        System.out.println("Clusters after DBSCAN:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }


    // ============================================================ //
    // Mean Shift Clustering
    // ============================================================ //
    void MeanShiftClustering() {
        double radius = 0.05; // radius
        boolean[] flagForVisited = new boolean[mxuid]; // Mark all object as unvisited



        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }
        ArrayList < Integer > tempCluster = new ArrayList < Integer > ();
        ArrayList < Integer > newTempCluster = new ArrayList < Integer > ();

        int newCentroid = 0;
        int oldCentroid = 0;

        for (int i = 2; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true;

                coreObjects.add(i);

                int coreObjectPosition = 0; // coreObject position in arraylist
                for (int q = 0; q < coreObjects.size(); q++) {
                    int matchCoreObject = coreObjects.get(q);
                    if (matchCoreObject == i) {
                        coreObjectPosition = q;
                        break;
                    }
                }

                //add i to final cluster
                arrayListofClusters.get(coreObjectPosition).add(i);

                // initial tempCluster
                for (int j = 2; j < mxuid; j++) {
                    if (diff[i][j] <= radius) {
                        tempCluster.add(j);
                    }
                }

                do {
                    oldCentroid = newCentroid;
                    //  find mean object (newCentroid)
                    double diffSum = 1000000;

                    for (int k = 0; k < tempCluster.size(); k++) {
                        int currentItem = tempCluster.get(k); // current item of the cluster
                        double diffSumTemp = 0;
                        for (int l = 0; l < tempCluster.size(); l++) {
                            int tempItem = tempCluster.get(l); // next item of the cluster
                            diffSumTemp += diff[currentItem][tempItem];
                        }

                        if (diffSumTemp < diffSum) {
                            diffSum = diffSumTemp; // store smallest diffSumTemp
                            newCentroid = currentItem; // store the item as new centroid
                        }
                    }

                    // if new mean object (centroid) was not visitied before, then add to cluster
                    if (flagForVisited[newCentroid] == false) {
                        flagForVisited[newCentroid] = true;

                        //add to final cluster
                        arrayListofClusters.get(coreObjectPosition).add(newCentroid);

                        ////// for debugging purpose
                        System.out.println("newCentroid / new mean: " + newCentroid);

                        //form cluster
                        for (int m = 2; m < mxuid; m++) {
                            if (diff[newCentroid][m] <= radius) {
                                newTempCluster.add(m);
                            }
                        }

                        tempCluster = newTempCluster;

                        // empty newTempCluster
                        newTempCluster.clear();
                    } else { // if newCentroid is a previously visited centroid

                        // searching the coreObject position of the oldCentroid
                        int coreObjectPositionOfOldCentroid = 0; // cluster position in arraylist

                        loopForOldCentroid:
                            for (int n = 0; n < coreObjects.size(); n++) {
                                for (int p = 0; p < arrayListofClusters.get(n).size(); p++) {
                                    int matchCentroid = arrayListofClusters.get(n).get(p);
                                    if (matchCentroid == oldCentroid) {
                                        coreObjectPositionOfOldCentroid = n;
                                        break loopForOldCentroid;
                                    }
                                }
                            }

                        // deleting coreObject of the previously visited centroid from coreObject arraylist
                        coreObjects.remove(coreObjectPositionOfOldCentroid);

                        // searching the coreObject position of the newCentroid (previously visited centroid)
                        int coreObjectPositionOfVisitedCentroid = 0;

                        loopForNewCentroid:
                            for (int n = 0; n < coreObjects.size(); n++) {
                                for (int p = 0; p < arrayListofClusters.get(n).size(); p++) {
                                    int matchCentroid = arrayListofClusters.get(n).get(p);
                                    if (matchCentroid == newCentroid) {
                                        coreObjectPositionOfVisitedCentroid = n;
                                        break loopForNewCentroid;
                                    }
                                }
                            }

                        // adding all objects of the old cluster to the appropriate cluster
                        for (int r = 0; r < arrayListofClusters.get(coreObjectPositionOfOldCentroid).size(); r++) {
                            int s = arrayListofClusters.get(coreObjectPositionOfOldCentroid).get(r);
                            arrayListofClusters.get(coreObjectPositionOfVisitedCentroid).add(s);

                        }

                        // deleting the cluster of oldCentroid (old cluster)
                        arrayListofClusters.remove(coreObjectPositionOfOldCentroid);
                    }
                } while (newCentroid != oldCentroid);

                // empty tempCluster
                tempCluster.clear();
            }
        }

        System.out.println("Clusters after MeanShift:");
        for (int i = 0; i < coreObjects.size(); i++) {
            System.out.println((i + 1) + " | Core point : " + coreObjects.get(i));
            for (int j = 0; j < arrayListofClusters.get(i).size(); j++) {
                System.out.print(arrayListofClusters.get(i).get(j) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }


    // ============================================================ //
    // Divisive Clustering
    // ============================================================ //
    void DivisiveClustering() {

        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        ArrayList < Integer > tempClusterX = new ArrayList < Integer > ();
        ArrayList < Integer > tempClusterY = new ArrayList < Integer > ();

        // finding 2 most furthest users in the cluster
        double maxDistance = 0;
        double distance = 0;
        int x = 0;
        int y = 0;
        for (int i = 2; i < mxuid; i++) {
            for (int j = i + 1; j < mxuid; j++) {
                distance = diff[i][j];
                if (distance > maxDistance) {
                    maxDistance = distance;
                    x = i;
                    y = j;
                }
            }
        }

        ////// for debugging purpose
        System.out.println("2 most furthest users are: " + x + " and " + y);

        for (int i = 2; i < mxuid; i++) {
            if (diff[i][x] <= diff[i][y]) {
                arrayListofTempClusters.get(x).add(i);
            } else {
                arrayListofTempClusters.get(y).add(i);
            }
        }

        ////// for debugging purpose
        // Display tempClusters
        System.out.println("Clusters after first div:");
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    System.out.print(arrayListofTempClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofTempClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
            }
        }

        //==============
        // iterations
        //==============
        //  number of clusters will be max iterations + 1
        for (int iterator = 1; iterator < 60; iterator++) { // we will get 61 clusters
            maxDistance = 0;
            int clusterToDiv = 0; // the cluster where we found the 2 most furthest users. So we can devide that

            for (int i = 0; i < arrayListofTempClusters.size(); i++) { // finding in all clusters
                // finding 2 most furthest users in current cluster
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    int m = arrayListofTempClusters.get(i).get(j);
                    for (int k = j + 1; k < arrayListofTempClusters.get(i).size(); k++) {
                        int n = arrayListofTempClusters.get(i).get(k);
                        distance = diff[m][n];
                        if (distance > maxDistance) {
                            maxDistance = distance;
                            x = m;
                            y = n;
                            clusterToDiv = i;
                        }
                    }
                }
            }

            ////// for debugging purpose
            System.out.println("2 most furthest users are: " + x + " and " + y);

            for (int i = 0; i < arrayListofTempClusters.get(clusterToDiv).size(); i++) {
                int o = arrayListofTempClusters.get(clusterToDiv).get(i);
                if (diff[o][x] <= diff[o][y]) {
                    tempClusterX.add(o);
                } else {
                    tempClusterY.add(o);
                }
            }

            ////// for debugging purpose
            System.out.println("tempClusterX, tempClusterY completed.");

            // empty the arraylist where x and y was.
            arrayListofTempClusters.get(clusterToDiv).clear();

            ////// for debugging purpose
            System.out.println("empty parent for avoiding duplicate");

            // adding to own clusters
            arrayListofTempClusters.get(x).addAll(tempClusterX);
            // empty tempClusterX
            tempClusterX.clear();
            // adding to own clusters
            arrayListofTempClusters.get(y).addAll(tempClusterY);
            // empty tempClusterY
            tempClusterY.clear();

            ////// for debugging purpose
            System.out.println("empty tempClusterX, tempClusterY");

            ////// for debugging purpose
            // Display tempClusters after iteration
            System.out.println("Clusters after iteration " + iterator + ":");
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        System.out.print(arrayListofTempClusters.get(i).get(j) + ", ");
                    }
                    System.out.println("\n total objects: " + arrayListofTempClusters.get(i).size()); // displays total objects
                    System.out.println();
                    System.out.println("================================");
                }
            }
        }

        // Add all clusters to arrayListofClusters()
        int j = 0;
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                arrayListofClusters.get(j).addAll(arrayListofTempClusters.get(i));
                j++;
                // empty the arraylist where x and y was.
                arrayListofTempClusters.get(i).clear();
            }
        }

        // Display clusters
        System.out.println("Clusters after Divisive:");
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
            }
        }
    }


    // ============================================================ //
    // Agglomerative Clustering
    // ============================================================ //
    //---------------------------//
    // Single-linkage clustering //
    //---------------------------//
    void SingleLinkageClustering() {
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        // Initially creating separate clusters for each user
        for (int i = 2; i < mxuid; i++) {
            arrayListofTempClusters.get(i).add(i);
        }

        int iterator = 10000;
        while (iterator > 61) { // to get 61 clusters
            double minDistance = 10000;
            double distance = 0;
            int x = 0;
            int y = 0;
            int clusterPositionX = 0;
            int clusterPositionY = 0;

            // finding 2 most nearest users of 2 different clusters
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                    int m = arrayListofTempClusters.get(i).get(j);
                    for (int p = i + 1; p < arrayListofTempClusters.size(); p++) {
                        for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
                            int n = arrayListofTempClusters.get(p).get(q);
                            distance = diff[m][n];
                            if (distance < minDistance) {
                                minDistance = distance;
                                x = m;
                                y = n;
                                clusterPositionX = i;
                                clusterPositionY = p;
                            }
                        }
                    }
                }
            }

            //// for debugging purpose
            System.out.println("x: " + x + "& Y: " + y);

            // merging clusterPositionY in clusterPositionX
            arrayListofTempClusters.get(clusterPositionX).addAll(arrayListofTempClusters.get(clusterPositionY));

            // empty the cluster at clusterPositionY
            arrayListofTempClusters.get(clusterPositionY).clear();

            // to get desired number of clusters
            int clusterCounter = 0;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    clusterCounter++; // determines number of clusters
                }
            }
            //System.out.println(clusterCounter);
            iterator = clusterCounter;
            System.out.println("iterator: " + iterator);
        }

        // Add all clusters to arrayListofClusters()
        int j = 0;
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                arrayListofClusters.get(j).addAll(arrayListofTempClusters.get(i));
                j++;
                // empty the arraylist where x and y was.
                arrayListofTempClusters.get(i).clear();
            }
        }

        // Display clusters
        System.out.println("Clusters after Single-Linkage clustering:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }


    //-----------------------------//
    // Complete-linkage clustering //
    //-----------------------------//
    void CompleteLinkageClustering() {
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofTempClusters.add(new ArrayList < Integer > ());
        }

        // Initially creating separate clusters for each user
        for (int i = 2; i < mxuid; i++) {
            arrayListofTempClusters.get(i).add(i);
        }

        //double maxDistance = 0;
        double distance = 0;
        double minOfMax = 10000;
        // int tempX = 0;
        // int tempY = 0;
        // int x = 0;
        // int y = 0;
        int tempClusterPositionX = 0;
        int tempClusterPositionY = 0;
        int clusterPositionX = 0;
        int clusterPositionY = 0;

        int iterator = 10000;
        while (iterator > 61) { // to get 61 clusters
            // finding 2 most nearest users of 2 different clusters
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                for (int p = i + 1; p < arrayListofTempClusters.size(); p++) { /////
                    double maxDistance = 0;
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        int m = arrayListofTempClusters.get(i).get(j);
                        //for (int p = i + 1; p < arrayListofTempClusters.size(); p++) {
                        for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
                            int n = arrayListofTempClusters.get(p).get(q);
                            distance = diff[m][n];
                            if (distance > maxDistance) {
                                maxDistance = distance;
                                // tempX = m;
                                // tempY = n;
                                tempClusterPositionX = i;
                                tempClusterPositionY = p;
                            }
                        }
                    }

                    if (maxDistance < minOfMax) {
                        minOfMax = maxDistance;
                        // x = tempX;
                        // y = tempY;
                        clusterPositionX = tempClusterPositionX;
                        clusterPositionY = tempClusterPositionY;
                    }
                }
            }

            // merging clusterPositionY in clusterPositionX
            arrayListofTempClusters.get(clusterPositionX).addAll(arrayListofTempClusters.get(clusterPositionY));

            // empty the cluster at clusterPositionY
            arrayListofTempClusters.get(clusterPositionY).clear();

            // to get desired number of clusters
            int clusterCounter = 0;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    clusterCounter++; // determines number of clusters
                }
            }
            //System.out.println(clusterCounter);
            iterator = clusterCounter;
            System.out.println("iterator: " + iterator);
            //iterator--;
        }

        // Add all clusters to arrayListofClusters()
        int j = 0;
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                arrayListofClusters.get(j).addAll(arrayListofTempClusters.get(i));
                j++;
                // empty the arraylist where x and y was.
                arrayListofTempClusters.get(i).clear();
            }
        }

        // Display clusters
        System.out.println("Clusters after Complete-Linkage clustering:");
        int totalClusters = 0;
        for (int i = 0; i < arrayListofClusters.size(); i++) {
            if (arrayListofClusters.get(i).size() > 0) {
                for (j = 0; j < arrayListofClusters.get(i).size(); j++) {
                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
                }
                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
                System.out.println();
                System.out.println("================================");
                totalClusters++;
            }
        }
        System.out.println("\n total clusters: " + totalClusters);
    }
}