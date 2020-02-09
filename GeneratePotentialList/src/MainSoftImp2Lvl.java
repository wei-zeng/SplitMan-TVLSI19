import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.J48;
import weka.classifiers.meta.ClassificationViaRegression;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.Randomize;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class MainSoftImp2Lvl {

    public static void main(String[] args) {
        long    startTime = System.nanoTime();
        System.out.println("===========  Processing benchmark " + args[0] + " layer " + args[1] + "  =============");
        int     benchmarkIndex = Integer.parseInt(args[0]);
        int     splitLevel = Integer.parseInt(args[1]);
        String  pathPrefix = args[2];
        boolean boundingBox = args[3].equals("1");
        boolean lvl1Classification = args[4].equals("1");
        boolean lvl2Classification = args[5].equals("1");
		double  threshold = Double.parseDouble(args[6]);

        // get the correct training instances from different arff files
	    

        //System.out.println("Shuffle training instance.");
        // shuffle the training instances
        //trainingIns = shuffleInstances(trainingIns);

        // build the randomforest classifier
//        AbstractClassifier classifier = new RandomForest();
//        AbstractClassifier classifier = new J48();
//        AbstractClassifier classifier = new ClassificationViaRegression();
        //AbstractClassifier classifier = new Bagging();
		if (lvl1Classification) {
            runClassification_Lvl1(benchmarkIndex, splitLevel, pathPrefix, startTime, boundingBox, threshold);
        }
		if (lvl2Classification) {
			Instances trainingIns = getTrainingInstances_Lvl2(benchmarkIndex, splitLevel, pathPrefix, boundingBox);

			double[] vpinHammingDis = trainingIns.attributeToDoubleArray(4); // index 4 is vpinHammingDistance
			double[] vpinXDis = trainingIns.attributeToDoubleArray(0); // index 0 is vpinXDistance
			double[] vpinYDis = trainingIns.attributeToDoubleArray(1); // index 1 is vpinYDistance
        
			double   maxHammingDis = Double.MIN_VALUE;
			double   maxXDis = Double.MAX_VALUE;
			double   maxYDis = Double.MAX_VALUE;
			for (int j = 0; j < vpinHammingDis.length; j++) {
				if (maxHammingDis < vpinHammingDis[j])
					maxHammingDis = vpinHammingDis[j];
				if (maxXDis < vpinXDis[j])
					maxXDis = vpinXDis[j];
				if (maxYDis < vpinYDis[j])
					maxYDis = vpinYDis[j];
			}
			System.out.println("Max Vpin hamming distance in other benchmarks = " + maxHammingDis);
			System.out.println("Max Vpin X distance in other benchmarks = " + maxXDis);
			System.out.println("Max Vpin Y distance in other benchmarks = " + maxYDis);
			System.out.println("Perform training.");
			AbstractClassifier classifier = new Bagging();
			try {				
				classifier.buildClassifier(trainingIns);
			}
			catch (Exception e){
				System.out.println("Classifier cannot be built.");
				e.printStackTrace();
				System.exit(3);
			}
			System.out.println("Finish training. Start classification.");
			runLvl2Classification(classifier, benchmarkIndex, splitLevel, pathPrefix, startTime, boundingBox, threshold);
		}
    }

    private static Instances getTrainingInstances_Lvl2(int excludedIndex, int splitLevel, String pathPrefix,
                                                  boolean boundingBox){
        // Read in a arbitrary dataset file to get the enumeration of dataset's attributes
        // Use the attributes to create an empty "instances" object and add instance from desired data files
        String arffFilePath = null;
        if (boundingBox)
            pathPrefix += "BB_";
        pathPrefix += "Lvl2";

        arffFilePath = pathPrefix + "TrainArffs/";

        Instances tempIns;
        try {
			int[] benchmarkIndices = {1, 5, 10, 12, 18};
			int testIndex = 1;
			if (excludedIndex == 1)
				testIndex = 5;
			System.out.println(1);
            tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + testIndex + "_" + splitLevel + "_for_" + excludedIndex + ".arff")));
            System.out.println(2);
			ArrayList<Attribute> attributeList = new ArrayList<>();
            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            Instances trainingIns = new Instances("vpin-connections", attributeList, 0);
            for (int i = 0; i < benchmarkIndices.length; i++) {
				System.out.println(i);
                if (benchmarkIndices[i] == excludedIndex)
                    continue;
                tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + benchmarkIndices[i] + "_" + splitLevel + "_for_" + excludedIndex + ".arff")));
                for (int j = 0; j < tempIns.numInstances(); j++) {
                    trainingIns.add(tempIns.instance(j));
                }
            }
            trainingIns.setClassIndex(trainingIns.numAttributes() - 1);
            System.out.println("Training instances number = " + trainingIns.size());
            return trainingIns;
        }
        catch (FileNotFoundException e){
            System.err.println("Cannot find arff file.");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Something wrong with initializing instances from arff file!");
            System.exit(2);
        }
        return null;
    }

	private static Instances getTrainingInstances_Lvl1(int excludedIndex, int splitLevel, String pathPrefix,
                                                  boolean boundingBox){
        // Read in a arbitrary dataset file to get the enumeration of dataset's attributes
        // Use the attributes to create an empty "instances" object and add instance from desired data files
        String arffFilePath = null;
        if (boundingBox)
            pathPrefix += "BB_";

        arffFilePath = pathPrefix + "TrainArffs/";

        Instances tempIns;
        try {
			int testIndex = 1;
			if (excludedIndex == 1)
				testIndex = 5;
            tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + testIndex + "_" + splitLevel + ".arff")));
            ArrayList<Attribute> attributeList = new ArrayList<>();
            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            Instances trainingIns = new Instances("vpin-connections", attributeList, 0);
            int[] benchmarkIndices = {1, 5, 10, 12, 18};
            for (int i = 0; i < benchmarkIndices.length; i++) {
                if (benchmarkIndices[i] == excludedIndex)
                    continue;
                tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + benchmarkIndices[i] + "_" + splitLevel + ".arff")));
                for (int j = 0; j < tempIns.numInstances(); j++) {
                    trainingIns.add(tempIns.instance(j));
                }
            }
            trainingIns.setClassIndex(trainingIns.numAttributes() - 1);
            System.out.println("Training instances number = " + trainingIns.size());
            return trainingIns;
        }
        catch (FileNotFoundException e){
            System.err.println("Cannot find arff file.");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Something wrong with initializing instances from arff file.");
            System.exit(2);
        }
        return null;
    }
	
    private static Instances shuffleInstances(Instances instances){
        String[] options = new String[2];
        options[0] = "-S";
        options[1] = "" + ThreadLocalRandom.current().nextInt();
        Randomize randomize = new Randomize();
        try {
            randomize.setOptions(options);
        }
        catch (Exception e){
            System.out.println("Unsupported options, using the default setting.");
        }
        try {
            randomize.setInputFormat(instances);
        }
        catch (Exception e){
            System.out.println("Input instances format cannot be processed.");
        }
        try {
            Instances shuffledIns = Filter.useFilter(instances, randomize);
            return shuffledIns;
        }
        catch (Exception e){
            System.out.println("Filter cannot be used successfully.");
        }
        System.out.println("Instances are not shuffled.");
        return instances;
    }

	public static int mapProbToBin(double prob) {
		if (prob >= 0.0 && prob <= 1.0)
            return ((int) (prob * 10000 + 0.5));
        else {
            System.out.println("Invalid probability!");
            return -1;
        }
    }
	
	private static void runLvl2Classification(AbstractClassifier classifier,
                                              int benchmarkIndex,
                                              int splitLevel,
                                              String pathPrefix,
                                              long startTime,
                                              boolean boundingBox,
											  double threshold) {

        String vpinDataPath = pathPrefix + "VpinData/";
        String potentialListPath, metaFilesPath, arffFilesPath;
        if (boundingBox)
            pathPrefix += "BB_";
        String lvl1PotentialListPath = pathPrefix + "PotentialListFiles/";
        pathPrefix += "Lvl2";

        potentialListPath = pathPrefix + "PotentialListFiles/";
        metaFilesPath = pathPrefix + "MetaFiles/";
        arffFilesPath = pathPrefix + "TrainArffs/";

        String dataFileName = vpinDataPath + "superblue" + benchmarkIndex + "_" + splitLevel + ".csv";
        String lvl1PotentialListFilaName = lvl1PotentialListPath + "superblue" + benchmarkIndex + "_" + splitLevel + ".potentialList";
        String potentialListFileName = potentialListPath + "superblue"+ benchmarkIndex + "_" + splitLevel + ".potentialList";
        String histFileName = metaFilesPath + "superblue"+ benchmarkIndex + "_" + splitLevel + "_hist.csv";
        String metaFileName = metaFilesPath + "superblue"+ benchmarkIndex + "_" + splitLevel + ".metaInfo";
        try {
            HashMap<Integer, DataEntry> dataEntries = buildMapFromCsv(dataFileName);
            Instances classifyIns = getDataSet(arffFilesPath, benchmarkIndex, splitLevel);
            // create a bufferedwriter to write the potential list file
            BufferedWriter listWriter = new BufferedWriter(new FileWriter(potentialListFileName));
            BufferedWriter histWriter = new BufferedWriter(new FileWriter(histFileName));

            // generate new instances and perform classification
            long cAsC = 0; // connected predicted as connected
            long cAsD = 0; // connected predicted as disconnected
            long dAsC = 0; // disconnected predicted as connected
            long dAsD = 0; // disconnected predicted as disconnected
            long alreadyMissing = 0;
            long totalValidVpinNum = dataEntries.size();

			long[] binsPos = new long[10001];
			long[] binsNeg = new long[10001];
			double[] acc = new double[10001];
			double[] sizeLoC = new double[10001];

            Scanner lvl1LocFile = null;
            long counter = 0;
            int totalNum = dataEntries.size();
            int portionNum = totalNum / 10; // this number is used only for estimating the program progress,
                                            // roughly equals to 1/10 of the number of vpins in the benchmark
            try {
                lvl1LocFile = new Scanner(new FileReader(lvl1PotentialListFilaName));
            } catch (FileNotFoundException e) {
                System.err.println("Cannot find the potential list file." + lvl1PotentialListFilaName);
            }

            while (lvl1LocFile.hasNextLine()) {
                if (counter % portionNum == 0) {
                    System.out.println("Processed " + counter + " / " + totalNum);
                }
                DataEntry curVpin = dataEntries.get(Integer.parseInt(lvl1LocFile.nextLine().substring(1)));
                if (curVpin == null)
                    continue;
                DataEntry matchVpin = dataEntries.get(curVpin.getMatchIDNum());

                listWriter.write("S" + curVpin.getCurrentIDNum());
                listWriter.newLine();
                boolean isMatchIncluded = false;
                while (lvl1LocFile.hasNextLine()) {
                    String tempVpinID = lvl1LocFile.nextLine();
                    if (!tempVpinID.equals("!")) {
                        DataEntry tempVpin = dataEntries.get(Integer.parseInt(tempVpinID.substring(1)));
                        if (matchVpin != null && tempVpin.getCurrentIDNum() == matchVpin.getCurrentIDNum())
                            isMatchIncluded = true;
                        if (curVpin.getCurrentIDNum() == tempVpin.getCurrentIDNum() ||
                                (curVpin.getOutCellArea() != 0 && tempVpin.getOutCellArea() != 0) ||
                                (curVpin.getInCellArea() == 0 && tempVpin.getInCellArea() == 0))
                            continue;
                        CrossInfoEntry tempCrossInfo = new CrossInfoEntry(curVpin, tempVpin);
                        Instance classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
                        classifyIn.setDataset(classifyIns);
						double[] predictedDist = classifier.distributionForInstance(classifyIn);
                        double p = predictedDist[0];
						double predictedLabel = (p >= threshold ? 0 : 1);
                        if (tempCrossInfo.isConnected() && predictedLabel == 0) {
                            cAsC++;
                            listWriter.write("S" + tempVpin.getCurrentIDNum());
                            listWriter.newLine();
							binsPos[mapProbToBin(p)]++;
                        }
                        else if (tempCrossInfo.isConnected() && predictedLabel == 1) {
                            cAsD++;
							binsPos[mapProbToBin(p)]++;
                        }
                        else if (!tempCrossInfo.isConnected() && predictedLabel == 0) {
                            dAsC++;
                            listWriter.write("S" + tempVpin.getCurrentIDNum());
                            listWriter.newLine();
							binsNeg[mapProbToBin(p)]++;
                        }
                        else if (!tempCrossInfo.isConnected() && predictedLabel == 1) {
                            dAsD++;
							binsNeg[mapProbToBin(p)]++;
                        }
                    } else {
                        listWriter.write("!");
                        listWriter.newLine();
                        if (!isMatchIncluded) {
                            alreadyMissing++;
                            cAsD++;
                        }
                        break;
                    }
                }
                counter++;
            }
            listWriter.flush();
            listWriter.close();

		    long LoC_accum = 0;
			long hit_accum = 0;
            for (int i = binsPos.length - 1; i >= 0; i--) {
				LoC_accum += (binsPos[i] + binsNeg[i]);
				hit_accum += binsPos[i];
				sizeLoC[i] = LoC_accum * 2.0 / totalValidVpinNum;
				acc[i] = hit_accum * 1.0 / (cAsC + cAsD);
				//FPR[i] = (LoC_accum - hit_accum) * 1.0 / (dAsC + dAsD);
				//if (i < binsPos.length - 1)
				//	ROC_AUC += (FPR[i] - FPR[i+1]) * (acc[i] + acc[i+1]) / 2.0;
				//else
				//	ROC_AUC += (FPR[i] - 0.0) * (acc[i] + 0.0) / 2.0;
                histWriter.write(sizeLoC[i] + "," + acc[i] + "\n");
            }
            histWriter.flush();
            histWriter.close();

            System.out.println("Done");
            System.out.println("" + cAsC + "\t" + cAsD);
            System.out.println("" + dAsC + "\t" + dAsD);
            System.out.println("Probability that the right vpin is in the list = " + 100 * (double) cAsC / (cAsC + cAsD) + "%");
            System.out.println("Average list length = " + (double) (cAsC + dAsC) * 2.0 / totalValidVpinNum);
            System.out.println("Out of bound / Already miss number = " + alreadyMissing);
            writeMetaInfo(startTime, metaFileName, cAsC, cAsD, dAsC, dAsD, alreadyMissing, totalValidVpinNum);
        }
        catch (FileNotFoundException e){
            System.err.println("Cannot find the original data file.");
            e.printStackTrace();
            System.exit(4);
        }
        catch (IOException e) {
            System.err.println("Something wrong with initializing instances from arff file.");
            System.exit(5);
        }
        catch (Exception e){
            System.err.println("Something wrong with classifying the new instances.");
            e.printStackTrace();
            System.exit(6);
        }
    }

	private static void runClassification_Lvl1(int excludedIndex,
                                         int splitLevel,
                                         String pathPrefix,
                                         long startTime,
                                         boolean boundingBox,
										 double threshold){

		double eps = 0.5;
        
        String vpinDataPath = pathPrefix + "VpinData/";
        String potentialListPath, metaFilesPath, arffFilesPath;
        if (boundingBox) {
            potentialListPath = pathPrefix + "BB_PotentialListFiles/";
            metaFilesPath = pathPrefix + "BB_MetaFiles/";
            arffFilesPath = pathPrefix + "BB_TrainArffs/";
        }
        else {
            potentialListPath = pathPrefix + "PotentialListFiles/";
            metaFilesPath = pathPrefix + "MetaFiles/";
            arffFilesPath = pathPrefix + "TrainArffs/";
        }

        String dataFileName = vpinDataPath + "superblue" + excludedIndex + "_" + splitLevel + ".csv";
        String[] tempEntry;
        try {
            // read in the entries from the original .csv file
            Scanner dataFile = new Scanner(new FileReader(dataFileName));
            dataFile.nextLine(); // Consume the first line
            ArrayList<DataEntry> dataEntries = new ArrayList<>();
            DataEntry dataEntryTemp;
            while (dataFile.hasNextLine()){
                tempEntry = dataFile.nextLine().split(",");
                dataEntryTemp = new DataEntry(tempEntry);
                if (!dataEntryTemp.isUnclearPinType() && !dataEntryTemp.isUnclearCell()) {
                    dataEntries.add(dataEntryTemp);
                }
            }
            dataFile.close();

            // read in a sample arff file to extract the attribute info in order to create the testing dataset
			Instances tempIns = new Instances(new BufferedReader(new FileReader(arffFilesPath + "superblue" + excludedIndex + "_" + splitLevel + ".arff")));			
            ArrayList<Attribute> attributeList = new ArrayList<>();
			
            int[] benchmarkIndices = {1, 5, 10, 12, 18};

            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            Instances classifyIns = new Instances("vpin-connections", attributeList, 0);
			classifyIns.setClassIndex(classifyIns.numAttributes() - 1);

            
            //BufferedWriter probWriter = new BufferedWriter(new FileWriter(probFileName));
//            BufferedWriter histWriter = new BufferedWriter(new FileWriter(histFileName));
         //   BufferedWriter backWriter = new BufferedWriter(new FileWriter(backFileName));
            // generate new instances and perform classification
            Instance  classifyIn;
            double[] predictedDist;
        //    double predictedLabel;
      //      long cAsC = 0; // connected predicted as connected
      //      long cAsD = 0; // connected predicted as disconnected
      //      long dAsC = 0; // disconnected predicted as connected
        //    long dAsD = 0; // disconnected predicted as disconnected
      //      long oufOfBoundCouter = 0;
      //      long totalValidVpinNum = dataEntries.size();
			
	//		long[] binsPos = new long[1001];
	//		long[] binsNeg = new long[1001];
	//		double[] acc = new double[1001]; // Accuracy = True Positive Rate = Recall
	//		double[] sizeLoC = new double[1001]; // Mean size of LoC
	//		double[] FPR = new double[1001]; // False Positive Rate
	//		double ROC_AUC = 0;
    
// Build classifier from Level-1 training set	
			Instances trainingIns = getTrainingInstances_Lvl1(excludedIndex, splitLevel, pathPrefix, boundingBox);
			double[] vpinHammingDis = trainingIns.attributeToDoubleArray(4); // index 4 is vpinHammingDistance
			double[] vpinXDis = trainingIns.attributeToDoubleArray(0); // index 0 is vpinXDistance
			double[] vpinYDis = trainingIns.attributeToDoubleArray(1); // index 1 is vpinYDistance
        
			double   maxHammingDis = Double.MIN_VALUE;
			double   maxXDis = Double.MAX_VALUE;
			double   maxYDis = Double.MAX_VALUE;
			for (int j = 0; j < vpinHammingDis.length; j++) {
				if (maxHammingDis < vpinHammingDis[j])
					maxHammingDis = vpinHammingDis[j];
				if (maxXDis < vpinXDis[j])
					maxXDis = vpinXDis[j];
				if (maxYDis < vpinYDis[j])
					maxYDis = vpinYDis[j];
			}
			System.out.println("Max Vpin hamming distance in other benchmarks = " + maxHammingDis);
			System.out.println("Max Vpin X distance in other benchmarks = " + maxXDis);
			System.out.println("Max Vpin Y distance in other benchmarks = " + maxYDis);
			System.out.println("Perform training.");
			AbstractClassifier classifier = new Bagging();
			try {
//           randomForest.buildClassifier(trainingIns);
//           j48.buildClassifier(trainingIns);
//           cvr.buildClassifier(trainingIns);
				classifier.buildClassifier(trainingIns);
			}
			catch (Exception e){
				System.out.println("Classifier cannot be built.");
				e.printStackTrace();
				System.exit(3);
			}
			
// run backfit testing 
			for (int k = 0; k < benchmarkIndices.length; k++) {
                if (benchmarkIndices[k] == excludedIndex)
                    continue;
				// create a bufferedwriter to write the potential list file
				String potentialListFileName = potentialListPath + "superblue" + benchmarkIndices[k] + "_" + splitLevel + "_for_" + excludedIndex + ".potentialList";
				String metaFileName = metaFilesPath + "superblue"+ benchmarkIndices[k] + "_" + splitLevel + "_for_" + excludedIndex + ".metaInfo";
        
				BufferedWriter listWriter = new BufferedWriter(new FileWriter(potentialListFileName));
				BufferedWriter metaWriter = new BufferedWriter(new FileWriter(metaFileName));
				
				dataFileName = vpinDataPath + "superblue" + benchmarkIndices[k] + "_" + splitLevel + ".csv";
				dataFile = new Scanner(new FileReader(dataFileName));
				dataFile.nextLine(); // Consume the first line
				dataEntries = new ArrayList<>();

				while (dataFile.hasNextLine()){
					tempEntry = dataFile.nextLine().split(",");
					dataEntryTemp = new DataEntry(tempEntry);
					if (!dataEntryTemp.isUnclearPinType() && !dataEntryTemp.isUnclearCell()) {
						dataEntries.add(dataEntryTemp);
					}
				}
				dataFile.close();
				 ArrayList<ArrayList<Integer>> skippedID = new ArrayList<ArrayList<Integer>>(); // keep track of skipped Vpin ID in LoC due to j < i
				// double[] p0 = new double[dataEntries.size()]; // keep track of Prob of positive samples.				
				// boolean[] hasNegativeSamples = new boolean[dataEntries.size()]; // keep track of whether there is negative samples in LoC.
				 double[] maxProb = new double[dataEntries.size()]; // keep track of max Prob of negative samples, in case there is no negative samples in LoC.
				 int[] maxProbID = new int[dataEntries.size()];
								
				for (int i = 0; i < dataEntries.size(); i++) {
					// Create empty skipped ID lists, initialize maxProb, maxProbID, neg sample flag.
					skippedID.add(new ArrayList<Integer>());
					// maxProb[i] = -1.0;
					// maxProbID[i] = -1;
					//p0[i] = -1.0;
					//hasNegativeSamples[i] = false;
				    // Find Matching Vpin and calc p0.
				/*	for (int j = 0; j < dataEntries.size(); j++) {
						if (j == i ||
								(dataEntries.get(i).getOutCellArea() != 0 && dataEntries.get(j).getOutCellArea() != 0) ||
								(dataEntries.get(i).getInCellArea() == 0 && dataEntries.get(j).getInCellArea() == 0)) {
									continue; // bypass invalid pairs
						}
						if (dataEntries.get(i).getMatchIDNum() == dataEntries.get(j).getCurrentIDNum()) {
							// d0 = Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) + Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY());
						//	CrossInfoEntry tempCrossInfo = new CrossInfoEntry(dataEntries.get(i), dataEntries.get(j));
						//	classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
						//	classifyIn.setDataset(classifyIns);
						//	predictedDist = classifier.distributionForInstance(classifyIn);
						//	p0[i] = predictedDist[0];
						}
					}*/
				}
				
				for (int i = 0; i < dataEntries.size(); i++) {
					if (i % 1000 == 0)
						System.out.println("Processed " + i + " / " + dataEntries.size());
	//                if (dataEntries.get(i).getCellArea() == 1)
	//                    continue;
	//                totalValidVpinNum++;
			//        probWriter.write("-1," + i);
			//		probWriter.newLine();
		            listWriter.write("S" + dataEntries.get(i).getCurrentIDNum());
		            listWriter.newLine();



			//		if (p0[i] == -1.0) {
			//			System.out.println("No match for " + i);
			//			continue; // No match, go to next i.
			//		}
					
					// Add skippedID to LoC
					for (int j: skippedID.get(i)) {
						listWriter.write("S" + dataEntries.get(j).getCurrentIDNum());
                        listWriter.newLine();
				//		if (!hasNegativeSamples[i] && dataEntries.get(i).getMatchIDNum() != dataEntries.get(j).getCurrentIDNum()) {
				//			hasNegativeSamples[i] = true;
				//		}
					}
						
					// Count other Vpins around Vpin i
					for (int j = 0; j < dataEntries.size(); j++) {
						if (j <= i ||
								(dataEntries.get(i).getOutCellArea() != 0 && dataEntries.get(j).getOutCellArea() != 0) ||
								(dataEntries.get(i).getInCellArea() == 0 && dataEntries.get(j).getInCellArea() == 0)) {
									continue;
						}
						double d = Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) + Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY());
						if (boundingBox){
							// if the vpin pair is far from each other and they are actually connected, increment the counter
							// if the vpin pair is far from each other and they are not connected, do nothing
							if (d > maxHammingDis + eps ||
								Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) > maxXDis + eps ||
								Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY()) > maxYDis + eps) {
								double p = 0.0;
				//				if (p >= p0[i]) { // very unlikely to happen, but possible in extreme case
									// add j to potentialList and skippedID
				//					listWriter.write("S" + dataEntries.get(j).getCurrentIDNum());
				//					listWriter.newLine();
				//					skippedID.get(j).add(i);
				//				}
								continue;
							}
						}
						CrossInfoEntry tempCrossInfo = new CrossInfoEntry(dataEntries.get(i), dataEntries.get(j));
						classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
						classifyIn.setDataset(classifyIns);
						predictedDist = classifier.distributionForInstance(classifyIn);
			//			predictedLabel = predictedDist[0] >= threshold ? 0 : 1;
						double p = predictedDist[0];
						if (dataEntries.get(i).getMatchIDNum() != dataEntries.get(j).getCurrentIDNum()) {
							if (p > maxProb[i]) {
								maxProb[i] = p;
								maxProbID[i] = j;
							}
							if (p > maxProb[j]) {
								maxProb[j] = p;
								maxProbID[j] = i;
							}
						}
						if (p >= threshold) {
							// add j to potentialList and skippedID
							listWriter.write("S" + dataEntries.get(j).getCurrentIDNum());
							listWriter.newLine();
							skippedID.get(j).add(i);
							// if j is a negative sample, set flag
						//	if (!hasNegativeSamples[i] && dataEntries.get(i).getMatchIDNum() != dataEntries.get(j).getCurrentIDNum()) {
						//		hasNegativeSamples[i] = true;
						//	}
						}
					}
					if (maxProb[i] > 0.0)	{
						// Add maxProbID to list
						listWriter.write("S" + dataEntries.get(maxProbID[i]).getCurrentIDNum());
						listWriter.newLine();
					}
					listWriter.write("!");
                    listWriter.newLine();
				}
				listWriter.flush();
				listWriter.close();
				writeMetaInfo(startTime, metaFileName, 0, 0, 0, 0, 0, dataEntries.size());
			}			
            System.out.println("Done");
		}
        catch (FileNotFoundException e){
            System.err.println("Cannot find the original data file.");
            e.printStackTrace();
            System.exit(4);
        }
        catch (IOException e) {
            System.err.println("Something wrong with initializing instances from arff file.");
            System.exit(5);
        }
        catch (Exception e){
            System.err.println("Something wrong with classifying the new instances.");
            e.printStackTrace();
            System.exit(6);
        }
    }
	
    private static void writeMetaInfo(long startTime, String metaFileName, long cAsC, long cAsD, long dAsC, long dAsD, long oufOfBoundCouter, long totalValidVpinNum) {
        try {
            BufferedWriter metaWriter = new BufferedWriter(new FileWriter(metaFileName));
            long elapsedTime = System.nanoTime() - startTime;
            long hour = TimeUnit.NANOSECONDS.toHours(elapsedTime);
            long minute = TimeUnit.NANOSECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(hour);
            long second = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);
            metaWriter.write("====== Confusion Matrix ======");
            metaWriter.newLine();
            metaWriter.write("Connected\tDisconnected\t<--\tclassified as");
            metaWriter.newLine();
            metaWriter.write(cAsC + "\t\t" + cAsD + "\t\t\tConnected");
            metaWriter.newLine();
            metaWriter.write(dAsC + "\t\t" + dAsD + "\t\tDisconnected");
            metaWriter.newLine();
            metaWriter.write("Probability that the right vpin is in the list = " + 100 * (double) cAsC / (cAsC + cAsD) + "%");
            metaWriter.newLine();
            metaWriter.write("Average list length = " + (double) (cAsC + dAsC) / totalValidVpinNum);
            metaWriter.newLine();
            metaWriter.write("Out of bound / Already miss number = " + oufOfBoundCouter);
            metaWriter.newLine();
            metaWriter.write("Total runtime: " + hour + " hours " + minute + " minutes " + second + " second.");
            metaWriter.newLine();
            metaWriter.flush();
            metaWriter.close();
        } catch (IOException e) {
            System.err.println("Something wrong when writing to meta file: " + metaFileName);
            e.printStackTrace();
            System.exit(7);
        }
    }

    private static ArrayList<DataEntry> buildListFromCsv(String fileName) {
        ArrayList<DataEntry> dataEntries = new ArrayList<>();
        try {
            // read in the entries from the original .csv file
            Scanner dataFile = new Scanner(new FileReader(fileName));
            dataFile.nextLine(); // Consume the first line
            DataEntry dataEntryTemp;
            String[] tempEntry;
            while (dataFile.hasNextLine()) {
                tempEntry = dataFile.nextLine().split(",");
                dataEntryTemp = new DataEntry(tempEntry);
                if (!dataEntryTemp.isUnclearPinType() && !dataEntryTemp.isUnclearCell()) {
                    dataEntries.add(dataEntryTemp);
                }
            }
            dataFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("Something wrong when reading the original csv file into list.");
            System.exit(8);
        }
        return dataEntries;
    }

    private static HashMap<Integer, DataEntry> buildMapFromCsv(String fileName) {
        HashMap<Integer, DataEntry> dataEntries = new HashMap<>();
        try {
            // read in the entries from the original .csv file
            Scanner dataFile = new Scanner(new FileReader(fileName));
            dataFile.nextLine(); // Consume the first line
            DataEntry dataEntryTemp;
            String[] tempEntry;
            while (dataFile.hasNextLine()) {
                tempEntry = dataFile.nextLine().split(",");
                dataEntryTemp = new DataEntry(tempEntry);
                if (!dataEntryTemp.isUnclearPinType() && !dataEntryTemp.isUnclearCell()) {
                    dataEntries.put(Integer.parseInt(tempEntry[0].substring(1)), dataEntryTemp);
                }
            }
            dataFile.close();
        } catch (FileNotFoundException e) {
            System.err.println("Something wrong when reading the original csv file into list.");
            System.exit(8);
        }
        return dataEntries;
    }

    private static Instances getDataSet(String arffFilesPath, int benchmarkIndex, int splitLevel) {
        Instances classifyIns = null;
		int testIndex = 1;
		if (benchmarkIndex == 1)
			testIndex = 5;
        try {
            // read in a sample arff file to extract the attribute info in order to create the testing dataset
            Instances tempIns = new Instances(new BufferedReader(new FileReader(arffFilesPath + "superblue" + testIndex + "_" + splitLevel + "_for_" + benchmarkIndex + ".arff")));
            ArrayList<Attribute> attributeList = new ArrayList<>();
            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            classifyIns = new Instances("vpin-connections", attributeList, 0);
            classifyIns.setClassIndex(classifyIns.numAttributes() - 1);
        } catch (IOException e) {
            System.err.println("Something wrong when read the sample arff file for build dataset.");
            System.exit(9);
        }
        return classifyIns;
    }

}
