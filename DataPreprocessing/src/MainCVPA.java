import java.io.*;
import java.util.*;

public class MainCVPA {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("===========  Processing benchmark " + args[0] + " layer " + args[1] + "  =============");
        String   benchmarkID = args[0];
        String   splitLevel = args[1];
        double   scalingFactor = Double.parseDouble(args[2]);
        String   pathPrefix = args[3];
        boolean  boundingBox = args[4].equals("1");
        int      numFolds = Integer.parseInt(args[5]);
		int      foldID = Integer.parseInt(args[6]);
        String   vpinDataPath = pathPrefix + "VpinData/";
        String   dataFileName = vpinDataPath + "superblue" + benchmarkID + "_" + splitLevel + ".csv";
        String   trainArffPath = null, testArffPath = null;
        if (boundingBox){
            trainArffPath = pathPrefix + "BB_TrainArffs/";
        }
        else if (!boundingBox){
            trainArffPath = pathPrefix + "TrainArffs/";
        }
        String   trainFileName = trainArffPath + "superblue" + benchmarkID + "_" + splitLevel + "_for_PA.arff";
        String   trainIDFileName = trainArffPath + "superblue" + benchmarkID + "_" + splitLevel + "_trainID.csv";
        String[] tempEntry;
        String   tempID = "";
        Integer  maxIDNum;
        Scanner dataFile1 = new Scanner(new FileReader(dataFileName)); // dataFile1 is used to find out the largest index number in the file
        Scanner dataFile2 = new Scanner(new FileReader(dataFileName)); // dataFile2 is the one used to parse the file
        dataFile1.nextLine(); // Consume the first line
        dataFile2.nextLine(); // Consume the first line
        while (dataFile1.hasNextLine()){
            tempEntry = dataFile1.nextLine().split(",");
            tempID    = tempEntry[0];
        }
        dataFile1.close();
        maxIDNum = Integer.parseInt(tempID.substring(1));
        System.out.println("Maximum ID number = " + maxIDNum);

        DataEntry[] dataEntries = new DataEntry[maxIDNum + 1];
        ArrayList<Integer> vpinIDNums = new ArrayList<>();
        DataEntry dataEntryTemp;
        while (dataFile2.hasNextLine()){
            tempEntry = dataFile2.nextLine().split(",");
            dataEntryTemp = new DataEntry(tempEntry);
            if (!dataEntryTemp.isUnclearPinType() && !dataEntryTemp.isUnclearCell()) {
                dataEntries[dataEntryTemp.getCurrentIDNum()] = dataEntryTemp;
                vpinIDNums.add(dataEntryTemp.getCurrentIDNum());
            }
            else
                System.out.println("Invalid entry: " + dataEntryTemp);
        }
        System.out.println("Size of valid entry = " + vpinIDNums.size());
        dataFile2.close();

        float maxVpinHammingDis = generateTrainingData(dataEntries, vpinIDNums, trainFileName, trainIDFileName, scalingFactor, boundingBox, 5, 0);
		
        //if (genTestSet)
          //  generateTestingData(dataEntries, vpinIDNums, maxVpinHammingDis, testFileName, boundingBox);
    }

    private static float generateTrainingData (DataEntry[] dataEntries,
                                             ArrayList<Integer> vpinIDNums,
                                             String trainFileName,
											 String trainIDFileName,
                                             double scalingFactor,
                                             boolean boundingBox,
											 int numFolds,
											 int foldID) {
        LinkedList<CrossInfoEntry> crossInfoEntries = new LinkedList<>();
        Random rdgen = new Random();
        DataEntry dataEntryTemp;
		try {
			BufferedWriter trainIDWriter = new BufferedWriter(new FileWriter(trainIDFileName));
		
        for (Integer i: vpinIDNums) {
            if (dataEntries[i].isProcessed())
                continue;
            else {
                dataEntries[i].setProcessed(true);
                dataEntryTemp = dataEntries[dataEntries[i].getMatchIDNum()];
                if (dataEntryTemp == null)
                    continue;

                crossInfoEntries.add(new CrossInfoEntry(dataEntries[i], dataEntryTemp));
                // check some cases, which are invalid
                // 1. both vpin connect to output pins
                if (dataEntries[i].getOutCellArea() != 0 && dataEntryTemp.getOutCellArea() != 0)
                    System.out.println("both vpins connect to output pins: " + dataEntries[i]);
                // 2. both vpin don't connect to any input pins
                if (dataEntries[i].getInCellArea() == 0 && dataEntryTemp.getInCellArea() == 0)
                    System.out.println("both vpins don't connect to any input pins: " + dataEntries[i]);
                // 3. both vpin don't connect to any output pins // this case is confirmed by Jon to be valid
//                if (dataEntries[i].getOutCellArea() == 0 && dataEntryTemp.getOutCellArea() == 0)
//                    System.out.println("both vpins don't connect to any output pins: " + dataEntries[i]);

                dataEntryTemp.setProcessed(true);
            }
        }
		double eps = 1e-15;
        if (boundingBox){
			double   maxHammingDis = Double.MIN_VALUE;
			double   maxXDis = Double.MAX_VALUE;
			double   maxYDis = Double.MAX_VALUE;
			for (CrossInfoEntry c: crossInfoEntries) {
				if (maxHammingDis < c.getHammingVpin())
					maxHammingDis = c.getHammingVpin();
				if (maxXDis < c.getDiffVpinX())
					maxXDis = c.getDiffVpinX();
				if (maxYDis < c.getDiffVpinY())
					maxYDis = c.getDiffVpinY();
			}	
			System.out.println("Max hamming distance between two connected Vpins is = " + maxHammingDis);
            System.out.println("Max X distance between two connected Vpins is = " + maxXDis);
            System.out.println("Max Y distance between two connected Vpins is = " + maxYDis);
            
			System.out.println("Scaling factor = " + scalingFactor);
            LinkedList<CrossInfoEntry> crossInfoEntriesTemp = new LinkedList<>();
            CrossInfoEntry c;
			for (int i = 0; i < crossInfoEntries.size(); i++) {
				c = crossInfoEntries.get(i);
                if (i % numFolds != foldID) {
					dataEntries[c.getSourceIDNumA()].setTraining(true);
					trainIDWriter.write(c.getSourceIDNumA() + "\n");
					dataEntries[c.getSourceIDNumB()].setTraining(true);
					trainIDWriter.write(c.getSourceIDNumB() + "\n");
					if (c.getHammingVpin() <= scalingFactor * maxHammingDis)
						crossInfoEntriesTemp.add(c);
					trainIDWriter.flush();
				}
            }
            System.out.println("Max hamming distance between two connected Vpins is = " +
                                Collections.max(crossInfoEntriesTemp).getHammingVpin() + " (after bounding box).");
            System.out.println("Number of connected instances in training set = " + crossInfoEntriesTemp.size());

            int pInsNum = crossInfoEntriesTemp.size();
            int firstIndex;
            int secondIndex;
			
            for (int i = 0; i < pInsNum; i++) {
                firstIndex = rdgen.nextInt(vpinIDNums.size());
                secondIndex = rdgen.nextInt(vpinIDNums.size());
                while (firstIndex == secondIndex ||
                        dataEntries[vpinIDNums.get(firstIndex)].getMatchIDNum() == vpinIDNums.get(secondIndex) ||
						dataEntries[vpinIDNums.get(firstIndex)].isTraining() == false ||
						dataEntries[vpinIDNums.get(secondIndex)].isTraining() == false ||
                        Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinX() - dataEntries[vpinIDNums.get(secondIndex)].getVpinX()) +
                            Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinY() - dataEntries[vpinIDNums.get(secondIndex)].getVpinY()) > scalingFactor * maxHammingDis ||
						Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinX() - dataEntries[vpinIDNums.get(secondIndex)].getVpinX()) > maxXDis + eps ||
						Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinY() - dataEntries[vpinIDNums.get(secondIndex)].getVpinY()) > maxYDis + eps) {
                    firstIndex = rdgen.nextInt(vpinIDNums.size());
                    secondIndex = rdgen.nextInt(vpinIDNums.size());
                }
                crossInfoEntriesTemp.add(new CrossInfoEntry(dataEntries[vpinIDNums.get(firstIndex)], dataEntries[vpinIDNums.get(secondIndex)]));
            }
            System.out.println("Number of instance in training set = " + crossInfoEntriesTemp.size());

            // write crossinfos to training file
            try {
                writeArffFile(crossInfoEntriesTemp, trainFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot write train crossinfos.");
            }
            return Collections.max(crossInfoEntriesTemp).getHammingVpin();
        }
        else if (!boundingBox){
			LinkedList<CrossInfoEntry> crossInfoEntriesTemp = new LinkedList<>();
			CrossInfoEntry c;
			for (int i = 0; i < crossInfoEntries.size(); i++) {
				c = crossInfoEntries.get(i);
                if (i % numFolds != foldID) {
					dataEntries[c.getSourceIDNumA()].setTraining(true);
					trainIDWriter.write(c.getSourceIDNumA() + "\n");
					dataEntries[c.getSourceIDNumB()].setTraining(true);
					trainIDWriter.write(c.getSourceIDNumB() + "\n");
					crossInfoEntriesTemp.add(c);
					trainIDWriter.flush();
				}
            }
			
            int pInsNum = crossInfoEntriesTemp.size();
            int firstIndex;
            int secondIndex;
            for (int i = 0; i < pInsNum; i++) {
                firstIndex = rdgen.nextInt(vpinIDNums.size());
                secondIndex = rdgen.nextInt(vpinIDNums.size());
                while (firstIndex == secondIndex ||
                        dataEntries[vpinIDNums.get(firstIndex)].getMatchIDNum() == vpinIDNums.get(secondIndex) ||
						dataEntries[vpinIDNums.get(firstIndex)].isTraining() == false ||
						dataEntries[vpinIDNums.get(secondIndex)].isTraining() == false){
                    firstIndex = rdgen.nextInt(vpinIDNums.size());
                    secondIndex = rdgen.nextInt(vpinIDNums.size());
                }
                crossInfoEntriesTemp.add(new CrossInfoEntry(dataEntries[vpinIDNums.get(firstIndex)], dataEntries[vpinIDNums.get(secondIndex)]));
            }
            System.out.println("Number of instance in training set = " + crossInfoEntriesTemp.size());

            // write crossinfos to training file
            try {
                writeArffFile(crossInfoEntriesTemp, trainFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot write train crossinfos.");
				System.exit(1);
            }
			trainIDWriter.flush();
			trainIDWriter.close();
            return 0;
        }
		}
		catch (IOException e) {
			System.err.println("Cannot open trainID file.");
			System.exit(2);
		}
        return 0;
    }

    private static void generateTestingData(DataEntry[] dataEntries, ArrayList<Integer> vpinIDNums,
                                                                  float maxVpinHammindDis, String testFileName, boolean boundingBox){
        int estimateNumOfMatch = 150;

        LinkedList<CrossInfoEntry> crossInfoEntries = new LinkedList<>();
        Random rdgen = new Random();
        int firstIndex;
        int secondIndex;

        if (boundingBox) {
            for (int i = 0; i < estimateNumOfMatch * vpinIDNums.size(); i++) {
                firstIndex = rdgen.nextInt(vpinIDNums.size());
                secondIndex = rdgen.nextInt(vpinIDNums.size());
                while (firstIndex == secondIndex ||
                        Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinX() - dataEntries[vpinIDNums.get(secondIndex)].getVpinX()) +
                                Math.abs(dataEntries[vpinIDNums.get(firstIndex)].getVpinY() - dataEntries[vpinIDNums.get(secondIndex)].getVpinY()) > maxVpinHammindDis) {
                    firstIndex = rdgen.nextInt(vpinIDNums.size());
                    secondIndex = rdgen.nextInt(vpinIDNums.size());
                }
                crossInfoEntries.add(new CrossInfoEntry(dataEntries[vpinIDNums.get(firstIndex)], dataEntries[vpinIDNums.get(secondIndex)]));
            }
        }
        else if (!boundingBox){
            for (int i = 0; i < estimateNumOfMatch * vpinIDNums.size(); i++) {
                firstIndex = rdgen.nextInt(vpinIDNums.size());
                secondIndex = rdgen.nextInt(vpinIDNums.size());
                while (firstIndex == secondIndex) {
                    firstIndex = rdgen.nextInt(vpinIDNums.size());
                    secondIndex = rdgen.nextInt(vpinIDNums.size());
                }
                crossInfoEntries.add(new CrossInfoEntry(dataEntries[vpinIDNums.get(firstIndex)], dataEntries[vpinIDNums.get(secondIndex)]));
            }
        }

        try {
            writeArffFile(crossInfoEntries, testFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot write test crossinfos.");
        }
    }

    private static void writeArffFile(LinkedList<CrossInfoEntry> crossInfoEntries, String fileName) throws FileNotFoundException{
        PrintWriter writer = new PrintWriter(fileName);
        writer.println("@RELATION vpin-connections");
        writer.println("@ATTRIBUTE diffVpinX numeric");
        writer.println("@ATTRIBUTE diffVpinY numeric");
        writer.println("@ATTRIBUTE diffPinX numeric");
        writer.println("@ATTRIBUTE diffPinY numeric");
        writer.println("@ATTRIBUTE hammingVpin numeric");
        writer.println("@ATTRIBUTE hammingPin numeric");
        writer.println("@ATTRIBUTE totalWireLength numeric");
        writer.println("@ATTRIBUTE cellAreaTotal numeric");
        writer.println("@ATTRIBUTE cellAreaDiff numeric");
        writer.println("@ATTRIBUTE routingCongestion numeric");
        writer.println("@ATTRIBUTE placeCongestion numeric");
        writer.println("@ATTRIBUTE class {true, false}");
        writer.println("@DATA");
        for (CrossInfoEntry c : crossInfoEntries) {
            writer.println(
                    c.getDiffVpinX() + ", " +
                    c.getDiffVpinY() + ", " +
                    c.getDiffPinX() + ", " +
                    c.getDiffPinY() + ", " +
                    c.getHammingVpin() + ", " +
                    c.getHammingPin() + ", " +
                    c.getTotalWireLength() + ", " +
                    c.getCellAreaTotal() + ", " +
                    c.getCellAreaDiff() + ", " +
                    c.getRoutingCongestion() + "," +
                    c.getPlaceCongestion() + "," +
                    c.isConnected());
        }
        writer.close();
    }
}