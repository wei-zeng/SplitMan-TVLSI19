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


public class MainCVPA {

    public static void main(String[] args) {
        long    startTime = System.nanoTime();
        System.out.println("===========  Processing benchmark " + args[0] + " layer " + args[1] + "  =============");
        int     benchmarkIndex = Integer.parseInt(args[0]);
        int     splitLevel = Integer.parseInt(args[1]);
        String  pathPrefix = args[2];
        boolean boundingBox = args[3].equals("1");
		double  testingRatio = Double.parseDouble(args[4]);

        // get the correct training instances from different arff files
	    Instances trainingIns = getTrainingInstances(benchmarkIndex, splitLevel, pathPrefix, boundingBox);

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
		
		// shuffle the training instances
        // trainingIns = shuffleInstances(trainingIns);
		// build the randomforest classifier
//        RandomForest randomForest = new RandomForest();
		
//        J48 j48 = new J48();
//        ClassificationViaRegression cvr = new ClassificationViaRegression();
        AbstractClassifier classifier = new Bagging();
        try {
//            randomForest.buildClassifier(trainingIns);
//            j48.buildClassifier(trainingIns);
//            cvr.buildClassifier(trainingIns);
            classifier.buildClassifier(trainingIns);
        }
        catch (Exception e){
            System.out.println("Classifier cannot be built.");
            e.printStackTrace();
            System.exit(3);
        }
        System.out.println("Finish training. Start classification.");

//        runClassification(randomForest, benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, maxXDis, maxYDis, startTime, boundingBox, threshold);
//        runClassification(j48, benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, startTime, boundingBox);
//        runClassification(cvr, benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, startTime, boundingBox);
        runClassification(classifier, benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, maxXDis, maxYDis, startTime, boundingBox, 0.5);
      //  runBackFit(benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, maxXDis, maxYDis, startTime, boundingBox);
	//	runCV_PA(benchmarkIndex, splitLevel, pathPrefix, maxHammingDis, maxXDis, maxYDis, startTime, boundingBox);
    }

    public static Instances getTrainingInstances(int excludedIndex, int splitLevel, String pathPrefix, boolean boundingBox){
        // Read in an arbitrary dataset file to get the enumeration of dataset's attributes
        // Use the attributes to create an empty "instances" object and add instance from desired data files
        String arffFilePath = null;
        if (boundingBox)
            arffFilePath = pathPrefix + "BB_TrainArffs/";
        else
            arffFilePath = pathPrefix + "TrainArffs/";

        Instances tempIns;
        try {
			int testIndex = 1;
			if (excludedIndex == 1)
				testIndex = 5;
            tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + testIndex + "_" + splitLevel + "_for_PA.arff")));
            ArrayList<Attribute> attributeList = new ArrayList<>();
            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            Instances trainingIns = new Instances("vpin-connections", attributeList, 0);
			int[] benchmarkIndices = {1, 5, 10, 12, 18};
            for (int i = 0; i < benchmarkIndices.length; i++) {
                if (benchmarkIndices[i] == excludedIndex)
                    continue;
                tempIns = new Instances(new BufferedReader(new FileReader(arffFilePath + "superblue" + benchmarkIndices[i] + "_" + splitLevel + "_for_PA.arff")));
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

    public static Instances shuffleInstances(Instances instances){
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
    
    public static void runClassification(AbstractClassifier classifier,
                                         int excludedIndex,
                                         int splitLevel,
                                         String pathPrefix,
                                         double maxHammingDis,
										 double maxXDis,
										 double maxYDis,
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
		
        try {
			int[] benchmarkIndices = {1, 5, 10, 12, 18};
            for (int k = 0; k < benchmarkIndices.length; k++) {
				int benchmarkIndex = benchmarkIndices[k]; 
                if (benchmarkIndex == excludedIndex)
                    continue;
            
		String dataFileName = vpinDataPath + "superblue" + benchmarkIndex + "_" + splitLevel + ".csv";
		String trainIDFileName = arffFilesPath + "superblue"+ benchmarkIndex + "_" +  + splitLevel + "_trainID.csv";
	//    String potentialListFileName = potentialListPath + "superblue"+ benchmarkIndex + "_" + "_for_" + excludedIndex + splitLevel + ".potentialList";
        String metaFileName = metaFilesPath + "superblue"+ benchmarkIndex + "_" + splitLevel + "_for_" + excludedIndex + ".metaInfo";
        String histFileName = metaFilesPath + "superblue"+ benchmarkIndex + "_" + splitLevel + "_for_" + excludedIndex + "_hist.csv";
		String paFileName = metaFilesPath + "superblue"+ benchmarkIndex + "_" +  + splitLevel + "_for_" + excludedIndex +"_pa.csv";
        
		String[] tempEntry;
        			
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

			// read trainID file
			HashSet<Integer> isTraining = new HashSet<>();
            dataFile = new Scanner(new FileReader(trainIDFileName));
            while (dataFile.hasNextLine()){
                isTraining.add(Integer.parseInt(dataFile.nextLine()));
            }
            dataFile.close();

			
            // read in a sample arff file to extract the attribute info in order to create the testing dataset
			int testIndex = 1;
			if (benchmarkIndex == 1)
				testIndex = 5;
            Instances tempIns = new Instances(new BufferedReader(new FileReader(arffFilesPath + "superblue" + testIndex + "_" + splitLevel + "_for_PA.arff")));
            ArrayList<Attribute> attributeList = new ArrayList<>();
            for (int i = 0; i < tempIns.numAttributes(); i++) {
                attributeList.add(tempIns.attribute(i));
            }
            Instances classifyIns = new Instances("vpin-connections", attributeList, 0);
			classifyIns.setClassIndex(classifyIns.numAttributes() - 1);

            // create a bufferedwriter to write the potential list file
            // BufferedWriter listWriter = new BufferedWriter(new FileWriter(potentialListFileName));
            BufferedWriter metaWriter = new BufferedWriter(new FileWriter(metaFileName));
            BufferedWriter paWriter = new BufferedWriter(new FileWriter(paFileName));
			//BufferedWriter probWriter = new BufferedWriter(new FileWriter(probFileName));
            BufferedWriter histWriter = new BufferedWriter(new FileWriter(histFileName));
            // generate new instances and perform classification
            Instance  classifyIn;
            double[] predictedDist;
            
            long totalValidVpinNum = dataEntries.size();
			long[] binsPos = new long[10001];
			long[] binsNeg = new long[10001];
			// PA-related
			double[] p0 = new double[dataEntries.size()]; // keep track of Prob of positive samples.
			double[] d0 = new double[dataEntries.size()]; // keep track of Distance of positive samples.
			int[] n0 = new int[dataEntries.size()]; // keep track of n0.
			int[] n235 = new int[dataEntries.size()]; // keep track of n2 n3 n5 w/ prob > critical Prob.			
			int[] n8 = new int[dataEntries.size()]; // keep track of n8.
			boolean[] dontCare = new boolean[dataEntries.size()]; // keep track of whether n4+n6+n7 > 0.
			double[] criticalProb = new double[dataEntries.size()]; // keep track of maxProb in n1.
			int dontCareCnt = 0;

//            ArrayList<ArrayList<Integer>> skippedID = new ArrayList<ArrayList<Integer>>(); // keep track of skipped Vpin ID in LoC due to j < i
			
			// Pass 1: get p0 and d0.
			System.out.println("Pass 1: Get p0 and d0 from ground truth");
			for (int i = 0; i < dataEntries.size(); i++) {
				// Create empty skipped ID lists, initialize maxProb, maxProbID, neg sample flag.
				//skippedID.add(new ArrayList<Integer>());
				if (isTraining.contains(dataEntries.get(i).getCurrentIDNum()))
					continue;
				p0[i] = -1.0;
				d0[i] = -1.0;
				
			    // Find Matching Vpin and calc p0 and d0.
				for (int j = 0; j < dataEntries.size(); j++) {
					if (j == i ||
							(dataEntries.get(i).getOutCellArea() != 0 && dataEntries.get(j).getOutCellArea() != 0) ||
							(dataEntries.get(i).getInCellArea() == 0 && dataEntries.get(j).getInCellArea() == 0)) {
								continue; // bypass invalid pairs
					}
					if (dataEntries.get(i).getMatchIDNum() == dataEntries.get(j).getCurrentIDNum()) {
						d0[i] = Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) + Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY());
						CrossInfoEntry tempCrossInfo = new CrossInfoEntry(dataEntries.get(i), dataEntries.get(j));
						classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
						classifyIn.setDataset(classifyIns);
						predictedDist = classifier.distributionForInstance(classifyIn);
						p0[i] = predictedDist[0];
					}
				}
			}

			// Pass 2: run d < d0[i] to get critical Prob for PA
			System.out.println("Pass 2: Get critical Prob for PA");
			for (int i = 0; i < dataEntries.size(); i++) {
                if (i % 1000 == 0)
                    System.out.println("Processed " + i + " / " + dataEntries.size());
				if (isTraining.contains(dataEntries.get(i).getCurrentIDNum()))
					continue;
				double d;
				double p;
				for (int j = 0; j < dataEntries.size(); j++) {
                    if (j == i)
                        continue;
					d = Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) + Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY());
					if (d >= d0[i])
						continue;
                    if (boundingBox && (d > maxHammingDis + eps ||
						    Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) > maxXDis + eps ||
							Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY()) > maxYDis + eps) ||
							(dataEntries.get(i).getOutCellArea() != 0 && dataEntries.get(j).getOutCellArea() != 0) ||
                            (dataEntries.get(i).getInCellArea() == 0 && dataEntries.get(j).getInCellArea() == 0)) {
						p = 0.0;
						if (p0[i] > -1.0 && d < d0[i] && p < p0[i] && criticalProb[i] < p)
							criticalProb[i] = p;
						continue;
                        
                    }
                    CrossInfoEntry tempCrossInfo = new CrossInfoEntry(dataEntries.get(i), dataEntries.get(j));
                    classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
                    classifyIn.setDataset(classifyIns);
                    predictedDist = classifier.distributionForInstance(classifyIn);
					p = predictedDist[0];
					if (p0[i] > -1.0 && d < d0[i] && p < p0[i] && criticalProb[i] < p)
						criticalProb[i] = p;
                }
            }
			
			// Pass 3: Write LoC and calc PA
			System.out.println("Pass 3: Write LoC and calc PA");
            for (int i = 0; i < dataEntries.size(); i++) {
                if (i % 1000 == 0)
                    System.out.println("Processed " + i + " / " + dataEntries.size());
				if (isTraining.contains(dataEntries.get(i).getCurrentIDNum()))
					continue;
				
//                totalValidVpinNum++;
        //        probWriter.write("-1," + i);
		//		probWriter.newLine();
        //        listWriter.write("S" + dataEntries.get(i).getCurrentIDNum());
        //        listWriter.newLine();
				
				// Add skippedID to LoC
		//		for (int j: skippedID.get(i)) {
		//			listWriter.write("S" + dataEntries.get(j).getCurrentIDNum());
        //            listWriter.newLine();
		//		}
				
				// Count other Vpins around Vpin i
				double d;
				double p;
                for (int j = 0; j < dataEntries.size(); j++) {
                    if (j == i)
                        continue;
					d = Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) + Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY());
                    if (boundingBox && (d > maxHammingDis + eps ||
						    Math.abs(dataEntries.get(i).getVpinX() - dataEntries.get(j).getVpinX()) > maxXDis + eps ||
							Math.abs(dataEntries.get(i).getVpinY() - dataEntries.get(j).getVpinY()) > maxYDis + eps) ||
							(dataEntries.get(i).getOutCellArea() != 0 && dataEntries.get(j).getOutCellArea() != 0) ||
                            (dataEntries.get(i).getInCellArea() == 0 && dataEntries.get(j).getInCellArea() == 0)) {
						p = 0.0;
						if (p0[i] > -1.0) {
							if (p >= p0[i] && d < d0[i] || p > p0[i] && d == d0[i]) {
								dontCare[i] = true;
								dontCareCnt++;
								break; // go to next i
							}
							else if (p == p0[i] && d == d0[i]) n0[i]++;
							else if (p <= p0[i] && p > criticalProb[i] && d >= d0[i]) n235[i]++;
							else if (p > p0[i] && d > d0[i]) n8[i]++;
						}
                        continue;
                    }

                    CrossInfoEntry tempCrossInfo = new CrossInfoEntry(dataEntries.get(i), dataEntries.get(j));
                    classifyIn = new DenseInstance(1.0, tempCrossInfo.toDoubleArray());
                    classifyIn.setDataset(classifyIns);
                    predictedDist = classifier.distributionForInstance(classifyIn);
					p = predictedDist[0];
                    if (dataEntries.get(i).getMatchIDNum() == dataEntries.get(j).getCurrentIDNum()) {
						binsPos[mapProbToBin(p)]++;
					} else {
						binsNeg[mapProbToBin(p)]++;
					}
					if (p0[i] > -1.0) {
						if (p >= p0[i] && d < d0[i] || p > p0[i] && d == d0[i]) {
							dontCare[i] = true;
							dontCareCnt++;
							break; // go to next i
						}
						else if (p == p0[i] && d == d0[i]) n0[i]++;
						else if (p <= p0[i] && p > criticalProb[i] && d >= d0[i]) n235[i]++;
						else if (p > p0[i] && d > d0[i]) n8[i]++;
					}
                }
            }
           
			for (int i = 0; i < dataEntries.size(); i++) {
				if (!dontCare[i] && !(isTraining.contains(dataEntries.get(i).getCurrentIDNum())))
					paWriter.write(n8[i] + "," + n0[i] + "," + n235[i] + "," + (criticalProb[i] > 0 ? 1 : 0) + "," + dataEntries.size() + "\n"); 
			}
            System.out.println("Done. Don't cares cnt = " + dontCareCnt + " HashSet size = " + isTraining.size());

            long elapsedTime = System.nanoTime() - startTime;
            long hour = TimeUnit.NANOSECONDS.toHours(elapsedTime);
            long minute = TimeUnit.NANOSECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(hour);
            long second = TimeUnit.NANOSECONDS.toSeconds(elapsedTime) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);
            metaWriter.write("Total runtime: " + hour + " hours " + minute + " minutes " + second + " second.");
            metaWriter.newLine();
            metaWriter.flush();
            metaWriter.close();
			for (int i = 0; i < binsPos.length; i++) {
				histWriter.write(binsPos[i] + "," + binsNeg[i] + "\n");
			}
			histWriter.flush();
			histWriter.close();
			paWriter.flush();
            paWriter.close();
        }
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
}
