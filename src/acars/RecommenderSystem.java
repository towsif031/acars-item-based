/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acars;

import java.io.*;
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

    Map < String, Integer > m = new TreeMap < String, Integer > ();

    boolean[][] flag = new boolean[mxmid + 1][mxmid];

    /* catagorize information start*/
    double[][] rat = new double[mxuid][mxmid];
    double[][] Rat = new double[mxuid][mxmid];
    double[][] predRat = new double[mxuid][mxmid];

    double[][] diff = new double[mxuid][mxuid];
    double[][] matrix = new double[mxuid][mxuid];
    double[][] finalMatrix = new double[mxuid][mxuid];

    double[] userSum = new double[mxuid + 1];
    double[] userAvg = new double[mxuid + 1];

    boolean[] userFlag = new boolean[mxuid];
    int totalRat = 0;
    boolean litmus = false;
    String inputPathPrefix, outputPathPrefix, prefix;
    ArrayList < Integer > clusterCentroids;
    Double[] curr = new Double[mxmid];

    ArrayIndexComparator comparator;
    Integer[] indexes;

    RecommenderSystem(String prefix, String inPrefix, String outPrefix) {
        this.prefix = prefix;
        this.inputPathPrefix = inPrefix;
        this.outputPathPrefix = outPrefix;
        init();
    }

    void init() {

        for (int i = 0; i < mxuid; i++) {
            userCluster.add(new ArrayList < Integer > ());
        }
        for (int i = 0; i < mxuid; i++) {
            userClusterTest.add(new ArrayList < Integer > ());
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

            userCluster.get(uid).add(mid);
            // itemCluster.get(mid).add(uid);
            userSum[mid] += r;
        }
        /*Calculate the average of all user*/
        int sz = 0;
        for (int i = 2; i < mxuid; i++) {
            sz = userCluster.get(i).size();
            if (sz != 0) {
                userAvg[i] = (userSum[i] / sz);

            } else {
                userAvg[i] = 0;
            }
            // out.println(usrAvg[i]);
        }

        // Reading the test.csv file
        in = new BufferedReader(new FileReader(inputPathPrefix + "test.csv"));

        while ((text = in .readLine()) != null) {
            cut = text.split(",");

            uid = Integer.parseInt(cut[0]);
            mid = Integer.parseInt(cut[1]);
            r = Integer.parseInt(cut[2]);
            t = Integer.parseInt(cut[3]);
            //System.out.println(uid+" + "+ mid +" + "+ r +" + "+t);
            Rat[uid][mid] = r;
            userClusterTest.get(uid).add(mid);
            totalRat++;
        }
    }

    // // The MATRIX to Fill
    // void fillMatrixRandom() {
    //     Random r = new Random();
    //     for (int currentUser = 2; currentUser < mxuid; currentUser++) {
    //         for (int currentNextUser = 2; currentNextUser < mxuid; currentNextUser++) {
    //             int Low = 0;
    //             int High = 7;
    //             int result = r.nextInt(High - Low) + Low;
    //             finalMatrix[currentUser][currentNextUser] = result;
    //             //finalMatrix[currentUser][centroid] += 1;

    //            // System.out.println(finalMatrix[currentUser][currentNextUser]);
    //         }
    //         // System.out.println();
    //     }
    // }

    double normalizedRatingBestNeighbor(int u, int m, int neighbour) {
        double S = 0, T = 0;

        if (userFlag[u] == false) {
            userFlag[u] = true;
            curr = new Double[mxuid];
            curr[0] = -1.0;
            curr[1] = -1.0;
            for (int i = 2; i < mxuid; i++) {
                //curr[1] missing
                curr[i] = (-1.0) * matrix[u][i];
            }
            comparator = new ArrayIndexComparator(curr);
            indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
        }

        int closeCounter = 0;

        for (int i = 0; i < neighbour; i++) {

            if (rat[indexes[i]][m] != 0 && indexes[i] != u) {
                closeCounter++;
                //                if (itemItemScore[m][indexes[i]] != 0) {
                //                    S += itemItemScore[m][indexes[i]] * (rat[u][indexes[i]] - itemAvg[indexes[i]]);
                //                    T += itemItemScore[m][indexes[i]];
                //                } else {
                //                    S += itemItemScore[indexes[i]][m] * (rat[u][indexes[i]] - itemAvg[indexes[i]]);
                //                    T += itemItemScore[indexes[i]][m];
                //                }
                if (matrix[u][indexes[i]] != 0) {
                    S += matrix[u][indexes[i]] * (rat[indexes[i]][m] - userAvg[indexes[i]]);
                    T += matrix[u][indexes[i]];
                }
            }
            //            if (rat[u][indexes[i]] != 0 && indexes[i] != m) {
            //                closeCounter++;
            //                if (itemItemScore[m][indexes[i]] != 0) {
            //                    S += itemItemScore[m][indexes[i]] * (rat[u][indexes[i]] - itemAvg[indexes[i]]) * relevanceScore[m][indexes[i]];
            //                    T += itemItemScore[m][indexes[i]] * relevanceScore[m][indexes[i]];
            //                } else {
            //                    S += itemItemScore[indexes[i]][m] * (rat[u][indexes[i]] - itemAvg[indexes[i]]) * relevanceScore[m][indexes[i]];
            //                    T += itemItemScore[indexes[i]][m] * relevanceScore[m][indexes[i]];
            //                }
            //            }
            if (closeCounter > neighbour) {
                break;
            }
        }

        if (T == 0) {
            return -1;
        }
        double avgRat = S / T;

        if ((avgRat + userAvg[u]) < 0) {
            return 1;
        }
        if (avgRat + userAvg[u] > 5) {
            return 5;
        }
        return avgRat + userAvg[u];
    }

    void calculateAMAE() throws FileNotFoundException, IOException {
        PrintWriter out2 = new PrintWriter(new FileWriter(outputPathPrefix + "ResultData.csv"));
        PrintWriter out3 = new PrintWriter(new FileWriter(outputPathPrefix + "calculatedARHR.csv"));

        int neighbor = 640;
        int friend = 0;
        double precisionUp = 0, precisionLow = 0, precision = 0, coverage = 0, coverageUp = 0, coverageLow = 0;
        double recallUp = 0, recallLow = 0, recall = 0;
        double arhrUp = 0, arhrLow = 0, arhr = 0;
        for (friend = 120; friend <= neighbor; friend += 10) {
            userFlag = new boolean[mxuid + 1];
            // double globalErrorSum = 0;
            //System.out.println("for : "+friend );
            precisionUp = 0;
            precisionLow = 0;
            precision = 0;
            recallUp = 0;
            recallLow = 0;
            recall = 0;
            coverageUp = 0;
            coverageLow = 0;
            coverage = 0;
            arhrUp = 0;
            arhrLow = 0;
            double globalRoundingErrorSum = 0;
            for (int user = 2; user < mxuid; user++) {
                List < Integer > itemsList = userClusterTest.get(user);
                Integer oneUserItemsSize = itemsList.size();
                for (Integer index = 0; index < oneUserItemsSize; index++) {
                    int items = itemsList.get(index);
                    coverageLow++;
                    // double predictedRating = normalizedRating(users, items, friend);
                    double predictedRating = 0;

                    int predictedRounding = 0;
                    predictedRating = normalizedRatingBestNeighbor(user, items, friend);
                    // System.out.println(Rat[user][items] + " , " + predictedRating);

                    // globalRoundingErrorSum += Math.abs(Rat[user][items] - predictedRating);
                    /*
                    if (litmus == false) {
                        predictedRating = normalizedRatingBestNeighbor(user, items, friend);
                        predictedRounding = (int) predictedRating;
                        if ((predictedRating - predictedRounding) >= 0.5) {
                            predictedRounding++;
                        }
                        predictedRating = predictedRounding;
                        predRat[user][items] = predictedRating;
                    } else {
                        predictedRating = normalizedRatingBestNeighborGeneralized(user, items, friend);
//                        predictedRounding = (int) predictedRating;
//                        if ((predictedRating - predictedRounding) >= 0.5) {
//                            predictedRounding++;
//                        }
                        // predictedRating = predictedRounding;
                        predRat[user][items] = predictedRating;
                    }
                    
                     */

                    // globalErrorSum += Math.abs(Rat[users][item] - predictedRating);
                    if (predictedRating != -1) {
                        coverageUp++;

                        // TP
                        if (Rat[user][items] > 2 && predictedRating > 2) {
                            precisionUp++;
                            precisionLow++;
                            recallUp++;
                            recallLow++;
                        }
                        // FP
                        if (Rat[user][items] < 3 && predictedRating > 2) {
                            precisionLow++;
                        }
                        // FN
                        if (Rat[user][items] > 2 && predictedRating < 3) {
                            recallLow++;
                        }

                        //System.out.println(Rat[users][items] + "   " + predictedRating);
                        //out2.println(Rat[users][items] + "   " + predictedRounding);
                        globalRoundingErrorSum += Math.abs(Rat[user][items] - predictedRating);
                    }

                }
            }
            // double AMAE = globalErrorSum / totalRat;
            double AMAE2 = globalRoundingErrorSum / totalRat;
            precision = precisionUp / precisionLow;
            recall = recallUp / recallLow;
            coverage = coverageUp / coverageLow;
            double f2measures = (2 * precision * recall) / (precision + recall);
            //out.println("AMAE is without rounding  : " + AMAE);
            out2.println(friend + "," + AMAE2 + "," + precision + "," + recall + "," + f2measures + "," + coverage);
            out2.println();
            out2.println();
            System.out.println(friend + "," + AMAE2 + "," + precision + "," + recall + "," + f2measures + "," + coverage);
            //out.flush();
            //out.close();
            out2.flush();
        }

        /*
        Double[] rr;
        for (int topK = 2; topK <= 20; topK++) {

            for (int items = 1; items <= mxmid; items++) {
                List<Integer> usersList = itemClusterTest.get(items);
                Integer oneItemUsersSize = usersList.size();

                //sort start
                rr = new Double[oneItemUsersSize];
                //curr[0] = -1.0;
                for (Integer in = 0; in < oneItemUsersSize; in++) {
                    int us = usersList.get(in);

                    rr[in] = (-1.0) * Rat[us][items];

                }
                comparator = new ArrayIndexComparator(rr);
                indexes = comparator.createIndexArray();
                Arrays.sort(indexes, comparator);

                //sort end
                for (Integer index = 0, top = 0; index < oneItemUsersSize && top < topK; index++, top++) {
                    int users = usersList.get(index);

                    int ii = indexes[top];

                    if (Rat[users][ii] == 5 && predRat[users][ii] == 5) {
                        arhrUp += 1 / (double) (top + 1);
                    }
//                        else if (Rat[users][ii] == 5 && predRat[users][ii] == 4) {
//                            arhrUp += (1 / (double)(top+1));
//                        } 
//                        else if (Rat[users][ii] == 4 && predRat[users][ii] == 5) {
//                            arhrUp += (1 / (double)(top+1));
//                        }
//                        else if (Rat[users][ii] == 4 && predRat[users][ii] == 4) {
//                            arhrUp += 1 / (double)(top+1);
//                        } 
//                        else if (Rat[users][ii] == 4 && predRat[users][ii] == 4) {
//                            arhrUp += (1);
//                        } 
                    //else if (Rat[users][ii] == 4 && predRat[users][ii] == 3) {
//                            arhrUp += (1 / 3);
//                        }

                }
                arhr += arhrUp / topK;
                //out3.prinln(topK+",");
            }
            arhr = arhr / mxmid;
            System.out.println(topK + "," + arhr);
            out3.println(topK + "," + arhr);
            arhr = 0;
        }
        
         */
        out2.close();
        out3.flush();
        out3.close();

    }

    // initial matrix
    void initFillMatrix() {
        for (int i = 2; i < mxuid; i++) {
            for (int j = 2; j < mxuid; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    // The MATRIX to fill after every clustering
    void fillMatrix() throws FileNotFoundException, IOException {
        File file = new File("F:/ThesisWorks/Datasets/90_10/userMatrix.csv");
        boolean exists = file.exists();
        if (!exists) {
            System.out.println("[userMatrix.csv] File Does Not Exist!");

            // writing userMatrix.csv
            PrintWriter out1 = new PrintWriter(new FileWriter(outputPathPrefix + "userMatrix.csv"));
            for (int p = 0; p < arrayListofClusters.size(); p++) {
                for (int q = 0; q < arrayListofClusters.get(p).size(); q++) {
                    int m = arrayListofClusters.get(p).get(q);
                    for (int r = 0; r < arrayListofClusters.get(p).size(); r++) {
                        int n = arrayListofClusters.get(p).get(r);
                        matrix[m][n] += 1;

                        // save in file
                        out1.println(m + "," + n + "," + matrix[m][n]);
                        out1.flush();
                    }
                }
            }
            out1.close();
        } else {
            System.out.println("[userMatrix.csv] File Exist!");

            // reading userMatrix.csv
            BufferedReader in = new BufferedReader(new FileReader(inputPathPrefix + "userMatrix.csv"));
            String text;
            String[] cut;
            int i = 0, j = 0;
            while ((text = in .readLine()) != null) {
                cut = text.split(",");
                i = Integer.parseInt(cut[0]);
                j = Integer.parseInt(cut[1]);
                matrix[i][j] = Double.parseDouble(cut[2]);
                System.out.println("Reading...");
                System.out.println("matrix[" + i + "][" + j + "] = " + matrix[i][j]);
            }

            // updating userMatrix.csv
            PrintWriter out2 = new PrintWriter(new FileWriter(outputPathPrefix + "userMatrix.csv"));
            for (int p = 0; p < arrayListofClusters.size(); p++) {
                for (int q = 0; q < arrayListofClusters.get(p).size(); q++) {
                    int m = arrayListofClusters.get(p).get(q);
                    for (int r = 0; r < arrayListofClusters.get(p).size(); r++) {
                        int n = arrayListofClusters.get(p).get(r);
                        matrix[m][n] += 1;

                        // save update in file
                        out2.println(m + "," + n + "," + matrix[m][n]);
                        out2.flush();

                        System.out.println();
                        System.out.println("After updating:");
                        System.out.println("matrix[" + m + "][" + n + "] = " + matrix[m][n]);
                    }
                }
            }
            out2.close();
        }
    }

    // display the MATRIX
    void displayMatrix() {
        System.out.println();
        System.out.println("display matrix...");
        for (int i = 2; i < mxuid; i++) {
            for (int j = 2; j < mxuid; j++) {
                if (matrix[i][j] > 0) {
                    System.out.println("matrix[" + i + "][" + j + "] = " + matrix[i][j]);
                }
            }
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
    void calculateDistance() throws FileNotFoundException, IOException {
        File file = new File("F:/ThesisWorks/Datasets/90_10/userDiffs.csv");
        boolean exists = file.exists();
        if (exists) {
            System.out.println("[userDiffs.csv] File Exist!");

            BufferedReader in = new BufferedReader(new FileReader(inputPathPrefix + "userDiffs.csv"));
            String text;
            String[] cut;
            int u = 0, v = 0;
            while ((text = in .readLine()) != null) {
                cut = text.split(",");
                u = Integer.parseInt(cut[0]);
                v = Integer.parseInt(cut[1]);
                diff[u][v] = Double.parseDouble(cut[2]);
                System.out.println("Distance between " + u + " and " + v + " is " + diff[u][v]);
            }
        } else {
            System.out.println("[userDiffs.csv] File Does Not Exist!");

            PrintWriter out = new PrintWriter(new FileWriter(outputPathPrefix + "userDiffs.csv"));
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

                    // save in file
                    out.println(u + "," + v + "," + diff[u][v]);
                    out.flush();

                    System.out.println("Diff of " + u + " & " + v + " = " + diff[u][v]);
                }
            }
            out.close();
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

    // Choose random 1 centroid from every 100 objects orderly. 6040 users So, K = 61
    ArrayList < Integer > randomInRange(ArrayList < Integer > clusterCentroids) {
        Random r = new Random();
        int firstrandom = r.nextInt(99 - 2 + 1) + 2;    // rand.nextInt((max - min) + 1) + min;
        clusterCentroids.add(firstrandom);
        for (int userId = 100; userId + 100 < mxuid; userId = userId + 100) {
            int Low = userId;
            int High = userId + 100;
            int Result = r.nextInt(High - Low + 1) + Low;    // rand.nextInt((max - min) + 1) + min;
            clusterCentroids.add(Result);
        }
        // As, 6001-6040 there is only 40 objects
        int Low = 6001;
        int High = 6040;
        int Result = r.nextInt(High - Low + 1) + Low;
        clusterCentroids.add(Result);

        return clusterCentroids;
    }

    // Choose K unique centroids randomly from the dataset. Here K = 61
    ArrayList < Integer > uniqueRandomInRange(ArrayList < Integer > clusterCentroids) {
        int numofCluster = 61;
        Random rand = new Random();
        while (numofCluster > 0) {
            int n = rand.nextInt(6040 - 2 + 1) + 2;     // rand.nextInt((max - min) + 1) + min;
            if (clusterCentroids.size() == 0) {
                clusterCentroids.add(n);
                numofCluster--;
            } else {
                boolean alreadyIncluded = false;
                for (int j = 0; j < clusterCentroids.size(); j++) {
                    if (clusterCentroids.get(j) == n) {
                        alreadyIncluded = true;
                        break;
                    }
                }
                if (alreadyIncluded == false) {
                    clusterCentroids.add(n);
                    numofCluster--;
                }
            }
        }
        return clusterCentroids;
    }

    // Display cluster centroids
    void displayClusterCentroids() {
        System.out.println();
        System.out.println("Centroids of clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | " + centroid);
        }
    }

    // Display Clusters
    void displayClusters() {
        System.out.println();
        System.out.println("Clusters:");
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + centroid); // displays centroid
            for (int p = 0; p < arrayListofClusters.get(centroid).size(); p++) {
                System.out.print(arrayListofClusters.get(centroid).get(p) + ", ");
            }
            System.out.println("\n total objects: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
    }

    // ============================================================ //
    // K-Means Clustering
    // ============================================================ //
    void K_MeansClustering() throws FileNotFoundException, IOException {
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
                    arrayListofClusters.get(i).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                    break;
                }
            }

            if (!isCentroid) {
                double tempMax = 10000;
                int tempCentroid = 0;
                double distance = 0;

                // finding nearest centroid to i
                for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                    int currentCentroid = clusterCentroids.get(k);
                    distance = diff[i][currentCentroid];
                    if (distance < tempMax) {
                        tempMax = distance; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                arrayListofClusters.get(tempCentroid).add(i);
            }
        }

        // //// for debugging purpose
        // // Display initial clusters
        System.out.println("initial clusters:");
        //displayClusters();
        int totalObjects = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i)); // displays centroid
            for (int j = 0; j < arrayListofClusters.get(centroid).size(); j++) {
                System.out.print(arrayListofClusters.get(centroid).get(j) + ", ");
                totalObjects++;
            }
            System.out.println("\n total objects in cluster: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
        System.out.println("\n Total Objects: " + totalObjects);

        // //// for debugging purpose
        // distanceCalculator(5426, 1894);
        // distanceCalculator(5837, 385);
        // distanceCalculator(5837, 2312);
        // distanceCalculator(5792, 4263);
        // distanceCalculator(5792, 4990);
        // distanceCalculator(5792, 4642);

        // Iterations of finding new centroids and populating
        ArrayList < Integer > newClusterCentroids;
        ArrayList < Integer > oldClusterCentroids;
        int iterator = 1;

        do {
            List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofTempClusters.add(new ArrayList < Integer > ());
            }
            newClusterCentroids = new ArrayList < Integer > ();

            // Find new centroid from the cluster
            for (int i = 0; i < clusterCentroids.size(); i++) {
                int currentCentroid = clusterCentroids.get(i);
                double diffSum = 10000;
                int newCentroid = 0;
                for (int j = 0; j < arrayListofClusters.get(currentCentroid).size(); j++) { // arrayListofClusters.get(currentCentroid).size() = cluster size of current centroid
                    int currentItem = arrayListofClusters.get(currentCentroid).get(j); // current item of the cluster
                    double diffSumTemp = 0;
                    for (int k = 0; k < arrayListofClusters.get(currentCentroid).size(); k++) {
                        int nextItem = arrayListofClusters.get(currentCentroid).get(k); // next item of the cluster
                        diffSumTemp += diff[currentItem][nextItem];
                    }

                    if (diffSumTemp < diffSum) {
                        diffSum = diffSumTemp; // store smallest diffSumTemp
                        newCentroid = currentItem; // store the item as new centroid
                    }
                }
                newClusterCentroids.add(newCentroid);
            }

            // storing clusterCentroids
            oldClusterCentroids = clusterCentroids;

            // For new centroids: Again populating each cluster with closest objects to its centroid
            for (int i = 2; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < newClusterCentroids.size(); j++) {
                    int centroid = newClusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofTempClusters.get(i).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                        break;
                    }
                }

                if (!isNewCentroid) {
                    double tempMax = 10000;
                    int tempCentroid = 0;
                    double distance = 0;

                    // for newCentroids, finding nearest centroid to i
                    for (int k = 0; k < newClusterCentroids.size(); k++) { // Here, newClusterCentroids.size() = 61
                        int currentCentroid = newClusterCentroids.get(k);
                        distance = diff[i][currentCentroid];
                        if (distance < tempMax) {
                            tempMax = distance; // tempMax will contain the closest centroid distance from a object
                            tempCentroid = currentCentroid; // The closest centroid
                        }
                    }

                    arrayListofTempClusters.get(tempCentroid).add(i);
                }
            }

            // updating arrayListofClusters
            arrayListofClusters = arrayListofTempClusters;
            clusterCentroids = newClusterCentroids;
            System.out.println("Current Iteration Number : " + iterator);
            iterator++;
        } while (!newClusterCentroids.equals(oldClusterCentroids)); // k-means convergence

        System.out.println();
        System.out.println("=========================================================================");
        System.out.println("For New Clusters:");
        System.out.println("=========================================================================\n");

        displayClusterCentroids();

        System.out.println();
        System.out.println("+++++++++++++++++++++++++++++");
        System.out.println("Final Clusters after K-Means:");
        System.out.println("+++++++++++++++++++++++++++++");
        System.out.println();

        totalObjects = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            System.out.println((i + 1) + " | centroid: " + clusterCentroids.get(i)); // displays centroid
            for (int j = 0; j < arrayListofClusters.get(centroid).size(); j++) {
                System.out.print(arrayListofClusters.get(centroid).get(j) + ", ");
                totalObjects++;
            }
            System.out.println("\n total objects in cluster: " + arrayListofClusters.get(centroid).size()); // displays total objects
            System.out.println();
            System.out.println("================================");
        }
        System.out.println("\n Total Objects: " + totalObjects);

        // Filling the MATRIX after K-Means
        fillMatrix();
        displayMatrix();
    }


    // ============================================================ //
    // K-Medoids Clustering
    // ============================================================ //
    void K_MedoidsClustering() throws FileNotFoundException, IOException {
        boolean[] isUsedCentroid = new boolean[mxuid];

        for (int i = 2; i < mxuid; i++) {
            isUsedCentroid[i] = false;
        }

        clusterCentroids = new ArrayList < Integer > (); // Arraylist of initial centroids

        // Find 61 random centroids (K) within dataset
        clusterCentroids = uniqueRandomInRange(clusterCentroids);

        for (int i = 0; i < clusterCentroids.size(); i++) {
            int centroid = clusterCentroids.get(i);
            isUsedCentroid[centroid] = true; // flag for already used as centroid
        }

        // // Display initial K-Medoids centroids
        // displayClusterCentroids();

        //  Populate each cluster with closest objects to its centroid
        for (int i = 2; i < mxuid; i++) { // i = current item
            // Check if item itself is centroid
            // Because sometimes 2 users' diff may be 0.0
            boolean isCentroid = false;
            for (int j = 0; j < clusterCentroids.size(); j++) {
                int centroid = clusterCentroids.get(j);
                if (i == centroid) { // If item itself is centroid
                    arrayListofClusters.get(i).add(i); // Add centroid to its own cluster
                    isCentroid = true;
                    break;
                }
            }

            if (!isCentroid) {
                double tempMax = 10000;
                int tempCentroid = 0;
                double distance = 0;

                // finding nearest centroid to i
                for (int k = 0; k < clusterCentroids.size(); k++) { // Here, clusterCentroids.size() = 61
                    int currentCentroid = clusterCentroids.get(k);
                    distance = diff[i][currentCentroid];
                    if (distance < tempMax) {
                        tempMax = distance; // tempMax will contain the closest centroid distance from a object
                        tempCentroid = currentCentroid; // The closest centroid
                    }
                }

                arrayListofClusters.get(tempCentroid).add(i);
            }
        }

        // Display initial clusters
        displayClusters();

        // Calculate total cost of initial clusters
        double totalCostofCurrentInitCluster = 0;
        double totalCostofInitClusters = 0;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            int currentCentroid = clusterCentroids.get(i);
            for (int p = 0; p < arrayListofClusters.get(currentCentroid).size(); p++) {
                int currentItem = arrayListofClusters.get(currentCentroid).get(p);
                totalCostofCurrentInitCluster += diff[currentCentroid][currentItem];
            }
            totalCostofInitClusters += totalCostofCurrentInitCluster;
        }

        System.out.println("Total Cost of Initial Clusters: " + totalCostofInitClusters);

        double oldCost = totalCostofInitClusters; // Saving initial total cost

        //================
        // Iterations for Finding best Medoids
        //================
        int iterationNum = 1;

        boolean allUsedAsCentroid = false;
        while (!allUsedAsCentroid) {
            System.out.println("iteration: " + iterationNum);

            List < List < Integer >> arrayListofTempClusters = new ArrayList < List < Integer >> (mxuid);
            for (int i = 0; i < mxuid; i++) {
                arrayListofTempClusters.add(new ArrayList < Integer > ());
            }

            // Randomly select a centroid to remove from clusterCentroids array
            Random randRem = new Random();
            int randomSelectedCentroidIndex = randRem.nextInt(clusterCentroids.size());
            int randomSelectedCentroid = clusterCentroids.get(randomSelectedCentroidIndex);
            //////System.out.println("Delete centroid: " + randomSelectedCentroid);

            // New clusterCentroids after removing randomly choosen centroid
            ArrayList < Integer > tempClusterCentroids = new ArrayList < Integer > ();
            for (int i = 0; i < clusterCentroids.size(); i++) { // Here, clusterCentroids.size() = 61
                int currentCentroid = clusterCentroids.get(i);
                if (currentCentroid != randomSelectedCentroid) {
                    tempClusterCentroids.add(currentCentroid);
                }
            }

            // //// for debugging purpose
            // // Display tempCentroids
            // System.out.println("tempCentroids of clusters after deleting a random centroid: ");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     System.out.println((i + 1) + "| " + tempClusterCentroids.get(i));
            // }

            // Randomly selected a new unique centroid from all other users
            Random randSel = new Random();
            int newRandomCentroid = 0;

            //isUsedCentroid[1] = true; // to avoid user 1, as its not present our dataset  //

            boolean isUnique = false;
            while (!isUnique) {
                newRandomCentroid = randSel.nextInt(6040 - 2 + 1) + 2;   // rand.nextInt((max - min) + 1) + min;
                if (!isUsedCentroid[newRandomCentroid]) {
                    isUsedCentroid[newRandomCentroid] = true;
                    isUnique = true;
                }
            }

            ////// System.out.println("New centroid to be added: " + newRandomCentroid);

            // Add new centroid to tempClusterCentroids
            tempClusterCentroids.add(newRandomCentroid);

            // //// for debugging purpose
            // // Display tempCentroids
            // System.out.println("tempCentroids of clusters after adding a random user as centroid: ");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     System.out.println((i + 1) + "| " + tempClusterCentroids.get(i));
            // }

            //  Populate each cluster with closest objects to its centroid
            for (int i = 2; i < mxuid; i++) { // i = current item
                // Check if item itself is centroid
                // Because sometimes 2 users' diff may be 0.0
                boolean isNewCentroid = false;
                for (int j = 0; j < tempClusterCentroids.size(); j++) {
                    int centroid = tempClusterCentroids.get(j);
                    if (i == centroid) { // If item itself is centroid
                        arrayListofTempClusters.get(i).add(i); // Add centroid to its own cluster
                        isNewCentroid = true;
                        break;
                    }
                }

                if (!isNewCentroid) {
                    double tempMax = 10000;
                    int tempCentroid = 0;
                    double distance = 0;

                    // finding nearest centroid to i
                    for (int k = 0; k < tempClusterCentroids.size(); k++) { // Here, tempClusterCentroids.size() = 61
                        int currentCentroid = tempClusterCentroids.get(k);
                        distance = diff[i][currentCentroid];
                        if (distance < tempMax) {
                            tempMax = distance; // tempMax will contain the closest centroid distance from a object
                            tempCentroid = currentCentroid; // The closest centroid
                        }
                    }

                    arrayListofTempClusters.get(tempCentroid).add(i);
                }
            }

            // //// for debugging purpose
            // // Display objects of temp clusters
            // System.out.println("tempClusters after swapping a centroid first time:");
            // for (int i = 0; i < tempClusterCentroids.size(); i++) {
            //     int centroid = tempClusterCentroids.get(i);
            //     System.out.println((i + 1) + " | centroid: " + tempClusterCentroids.get(i)); // displays centroid
            //     for (int p = 0; p < arrayListofTempClusters.get(centroid).size(); p++) {
            //         System.out.print(arrayListofTempClusters.get(centroid).get(p) + ", ");
            //     }
            //     System.out.println("\n total objects: " + arrayListofTempClusters.get(centroid).size()); // displays total objects
            //     System.out.println();
            //     System.out.println("================================");
            // }

            // Calculate total cost of tempClusters
            double totalCostofCurrentTempCluster = 0;
            double totalCostofAllTempClusters = 0;
            for (int i = 0; i < tempClusterCentroids.size(); i++) {
                int currentCentroid = tempClusterCentroids.get(i);
                for (int p = 0; p < arrayListofTempClusters.get(currentCentroid).size(); p++) {
                    int currentItem = arrayListofTempClusters.get(currentCentroid).get(p);
                    totalCostofCurrentTempCluster += diff[currentCentroid][currentItem];
                }
                totalCostofAllTempClusters += totalCostofCurrentTempCluster;
            }

            // total cost of with new centroid
            System.out.println("Total Cost of Temp Clusters: " + totalCostofAllTempClusters + " for iteration no: " + iterationNum);

            double newCost = totalCostofAllTempClusters;

            double s = newCost - oldCost;
            if (s < 0) {
                clusterCentroids = tempClusterCentroids;
                arrayListofClusters = arrayListofTempClusters;
                oldCost = newCost;
            }

            // check if all users are used centroid
            int usedAsCentroidCount = 0;
            for (int i = 2; i < mxuid; i++) {
                if (!isUsedCentroid[i]) { // if any user not yet used as centroid
                    usedAsCentroidCount = 1;
                    break;
                }
            }

            if (usedAsCentroidCount == 0) {
                allUsedAsCentroid = true;
            }

            iterationNum++;
        }

        System.out.println("+++++++++++++++++++++++++++++++");
        System.out.println("Final Clusters after K-Medoids:");
        System.out.println("+++++++++++++++++++++++++++++++");
        displayClusterCentroids();
        displayClusters();
        System.out.println("Final Cost: " + oldCost);

        // Filling the MATRIX after K-Medoids
        fillMatrix();
        displayMatrix();
    }


    // ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //
    void DBSCANClusteringE() throws FileNotFoundException, IOException {
        double eps = 0.05; // minimum epsilon
        int minPts = 10; // minimum number of points
        boolean[] flagForVisited = new boolean[mxuid];
        boolean[] isInCluster = new boolean[mxuid];
        boolean[] isNoise = new boolean[mxuid];

        for (int i = 2; i < mxuid; i++) { // Mark all object as unvisited
            flagForVisited[i] = false;
        }

        for (int i = 2; i < mxuid; i++) { // Mark all object as is not in any cluster
            isInCluster[i] = false;
        }

        for (int i = 2; i < mxuid; i++) { // Mark all object as not noise
            isNoise[i] = false;
        }

        List < List < Integer >> neighborObjects = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            neighborObjects.add(new ArrayList < Integer > ());
        }

        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

        for (int i = 2; i < mxuid; i++) {
            if (flagForVisited[i] == false) {
                flagForVisited[i] = true; // Mark i as visited

                for (int j = 2; j < mxuid; j++) {
                    if (diff[i][j] <= eps) {
                        neighborObjects.get(i).add(j);
                    }
                }

                if (neighborObjects.get(i).size() >= minPts) {
                    coreObjects.add(i); // i is a core object
                    arrayListofClusters.get(i).add(i);

                    isInCluster[i] = true;
                    for (int k = 0; k < neighborObjects.get(i).size(); k++) {
                        int p = neighborObjects.get(i).get(k);
                        if (flagForVisited[p] == false) {
                            flagForVisited[p] = true;
                            //arrayListofClusters.get(i).add(p);
                            for (int l = 2; l < mxuid; l++) {
                                if (diff[p][l] <= eps) {
                                    neighborObjects.get(p).add(l);
                                }
                            }

                            if (neighborObjects.get(p).size() >= minPts) {
                                for (int m = 0; m < neighborObjects.get(p).size(); m++) {
                                    int n = neighborObjects.get(p).get(m);
                                    neighborObjects.get(i).add(n); // add neighborObjects of p to neighborObjects of i
                                }
                            }
                        }

                        if (isInCluster[p] == false) {
                            isInCluster[p] = true;
                            arrayListofClusters.get(i).add(p);
                        }
                    }
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
        System.out.println("Total number of Objects in all clusters: " + totalObjectInClusters);

        // Filling the MATRIX after DBSCAN
        //fillMatrix();
        //displayMatrix();
    }






    /// ============================================================ //
    // DBSCAN Clustering
    // ============================================================ //
    void DBSCANClustering() throws FileNotFoundException, IOException {
        double eps = 0.05; // minimum epsilon
        int minPts = 50; // minimum number of points
        boolean[] flagForVisited = new boolean[mxuid]; // Mark all object as unvisited
        boolean[] isInCluster = new boolean[mxuid];
        boolean[] isNoise = new boolean[mxuid];

        for (int i = 2; i < mxuid; i++) { // Mark all object as unvisited
            flagForVisited[i] = false;
        }

        for (int i = 2; i < mxuid; i++) { // Mark all object as is not in any cluster
            isInCluster[i] = false;
        }

        for (int i = 2; i < mxuid; i++) { // Mark all object as not noise
            isNoise[i] = false;
        }

        ArrayList < Integer > neighborObjects = new ArrayList < Integer > (); // candidate set N
        ArrayList < Integer > neighborObjectsOfp = new ArrayList < Integer > ();
        ArrayList < Integer > coreObjects = new ArrayList < Integer > ();
        List < List < Integer >> arrayListofClusters = new ArrayList < List < Integer >> (mxuid);
        for (int i = 0; i < mxuid; i++) {
            arrayListofClusters.add(new ArrayList < Integer > ());
        }

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

        // // Filling the MATRIX after DBSCAN
        // System.out.println("MATRIX after DBSCAN:");

        fillMatrix();
        //displayMatrix();
    }


    // ============================================================ //
    // Mean Shift Clustering
    // ============================================================ //
    void MeanShiftClustering() throws FileNotFoundException, IOException {
        double radius = 0.05; // radius
        boolean[] flagForVisited = new boolean[mxuid];
        for (int i = 2; i < mxuid; i++) { // Mark all object as unvisited
            flagForVisited[i] = false;
        }

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

        // Filling the MATRIX after MeanShift
        //fillMatrix();
        displayMatrix();
    }


    // ============================================================ //
    // Divisive Clustering
    // ============================================================ //
    void DivisiveClustering() throws FileNotFoundException, IOException {

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
        int initClusterCounter = 1;
        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
            if (arrayListofTempClusters.get(i).size() > 0) {
                System.out.println("Cluster #" + initClusterCounter);
                initClusterCounter++;
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
            System.out.println("Clusters after iteration #" + iterator + " :");
            int tempClusterCounter = 1;
            for (int i = 0; i < arrayListofTempClusters.size(); i++) {
                if (arrayListofTempClusters.get(i).size() > 0) {
                    System.out.println("Cluster #" + tempClusterCounter);
                    tempClusterCounter++;
                    for (int j = 0; j < arrayListofTempClusters.get(i).size(); j++) {
                        System.out.print(arrayListofTempClusters.get(i).get(j) + ", ");
                    }
                    System.out.println("\n total objects: " + arrayListofTempClusters.get(i).size()); // displays total objects
                    System.out.println();
                    System.out.println("================================");
                }
            }
        }

        // // Add all clusters to arrayListofClusters()
        // int j = 0;
        // for (int i = 0; i < arrayListofTempClusters.size(); i++) {
        //     if (arrayListofTempClusters.get(i).size() > 0) {
        //         arrayListofClusters.get(j).addAll(arrayListofTempClusters.get(i));
        //         j++;
        //         // empty the arraylist where x and y was.
        //         arrayListofTempClusters.get(i).clear();
        //     }
        // }

        // // Display clusters
        // System.out.println("Clusters after Divisive:");
        // int clusterCounter = 1;
        // for (int i = 0; i < arrayListofClusters.size(); i++) {
        //     if (arrayListofClusters.get(i).size() > 0) {
        //         System.out.println("Cluster #" + clusterCounter);
        //         clusterCounter++;
        //         for (j = 0; j < arrayListofClusters.get(i).size(); j++) {
        //             System.out.print(arrayListofClusters.get(i).get(j) + ", ");
        //         }
        //         System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
        //         System.out.println();
        //         System.out.println("================================");
        //     }
        // }

        //////////////
        arrayListofClusters = arrayListofTempClusters;

        // Filling the MATRIX after Divisive
        fillMatrix();
        //        for (int p = 0; p < arrayListofTempClusters.size(); p++) {
        //            for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
        //                int m = arrayListofTempClusters.get(p).get(q);
        //                for (int r = 0; r < arrayListofTempClusters.get(p).size(); r++) {
        //                    int n = arrayListofTempClusters.get(p).get(r);
        //                    matrix[m][n] += 1;
        //                }
        //            }
        //        }
        displayMatrix();
    }


    // ============================================================ //
    // Agglomerative Clustering
    // ============================================================ //
    //---------------------------//
    // Single-linkage clustering //
    //---------------------------//
    void SingleLinkageClustering() throws FileNotFoundException, IOException {
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
            System.out.println("----------------");
            System.out.println("x: " + x + " & y: " + y);

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

        //        // Add all clusters to arrayListofClusters()
        //        int j = 0;
        //        for (int i = 0; i < arrayListofTempClusters.size(); i++) {
        //            if (arrayListofTempClusters.get(i).size() > 0) {
        //                arrayListofClusters.get(j).addAll(arrayListofTempClusters.get(i));
        //                j++;
        //                // empty the arraylist where x and y was.
        //                arrayListofTempClusters.get(i).clear();
        //            }
        //        }
        //
        //        // Display clusters
        //        System.out.println("Clusters after Single-Linkage clustering:");
        //        int totalClusters = 0;
        //        for (int i = 0; i < arrayListofClusters.size(); i++) {
        //            if (arrayListofClusters.get(i).size() > 0) {
        //                for (j = 0; j < arrayListofClusters.get(i).size(); j++) {
        //                    System.out.print(arrayListofClusters.get(i).get(j) + ", ");
        //                }
        //                System.out.println("\n total objects: " + arrayListofClusters.get(i).size()); // displays total objects
        //                System.out.println();
        //                System.out.println("================================");
        //                totalClusters++;
        //            }
        //        }
        //        System.out.println("\n total clusters: " + totalClusters);


        /////////////
        arrayListofClusters = arrayListofTempClusters;

        // Filling the MATRIX after Single-Linkage
        fillMatrix();
        //        for (int p = 0; p < arrayListofTempClusters.size(); p++) {
        //            for (int q = 0; q < arrayListofTempClusters.get(p).size(); q++) {
        //                int m = arrayListofTempClusters.get(p).get(q);
        //                for (int r = 0; r < arrayListofTempClusters.get(p).size(); r++) {
        //                    int n = arrayListofTempClusters.get(p).get(r);
        //                    matrix[m][n] += 1;
        //                }
        //            }
        //        }
        displayMatrix();
    }


    //-----------------------------//
    // Complete-linkage clustering //
    //-----------------------------//
    void CompleteLinkageClustering() throws FileNotFoundException, IOException {
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

        // Filling the MATRIX after Complete-Linkage
        //fillMatrix();
        displayMatrix();
    }
}