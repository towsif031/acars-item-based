/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acars;

import java.io.IOException;

/**
 *
 * @author TOWSIF AHMED
 */
public class ACARS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String pathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        String inputPathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        String outputPathPrefix = "F:\\ThesisWorks\\Datasets\\90_10\\";
        String prefix = outputPathPrefix;
        RecommenderSystem rs = new RecommenderSystem(pathPrefix, inputPathPrefix, outputPathPrefix);
        rs.takeTrainData();
        //rs.K_MeansClustering();
        //rs.K_MedoidsClustering();
        rs.DBSCANClustering();
        //rs.MeanShiftClustering();
        //rs.calculateDistance();
    }
    
}
