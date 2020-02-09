import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class MainData2Lvl {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("===========  Processing benchmark " + args[0] + " layer " + args[1] + "  =============");
        String   benchmarkID = args[0];
        String   splitLevel = args[1];
        double   scalingFactor = Double.parseDouble(args[2]);
        String   pathPrefix = args[3];
        boolean  boundingBox = args[4].equals("1");
        boolean  genTrain  = args[5].equals("1");
        boolean  genLvl2Train = args[6].equals("1");
        int      targetBenchmark = Integer.parseInt(args[7]);
        String   vpinDataPath = pathPrefix + "VpinData/";
        String   dataFileName = vpinDataPath + "superblue" + benchmarkID + "_" + splitLevel + ".csv";
        String   trainArffPath = null, testArffPath = null, locFilePath = null, lvl2TrainFilePath = null;
        if (boundingBox){
            pathPrefix = pathPrefix + "BB_";
        }
        trainArffPath = pathPrefix + "TrainArffs/";
        testArffPath = pathPrefix + "TestArffs/";
        locFilePath = pathPrefix + "PotentialListFiles/";
        lvl2TrainFilePath = pathPrefix + "Lvl2TrainArffs/";
        String   trainFileName = trainArffPath + "superblue" + benchmarkID + "_" + splitLevel + ".arff";
        String   testFileName = testArffPath + "superblue" + benchmarkID + "_" + splitLevel + ".arff";
        String   locFileName = locFilePath + "superblue" + benchmarkID + "_" + splitLevel + "_for_" + targetBenchmark + ".potentialList";
        String   lvl2TrainFileName = lvl2TrainFilePath + "superblue" + benchmarkID + "_" + splitLevel + "_for_" + targetBenchmark + ".arff";

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

        float maxVpinHammingDis = 0;
        if (genTrain)
            maxVpinHammingDis = generateTrainingData(dataEntries, vpinIDNums, trainFileName, scalingFactor, boundingBox);
//        if (genTestSet)
//            generateTestingData(dataEntries, vpinIDNums, maxVpinHammingDis, testFileName, boundingBox);
        if (genLvl2Train) {
            generateLvl2TrainingData(dataEntries, lvl2TrainFileName, locFileName, vpinIDNums.size());
        }
    }

    private static void generateLvl2TrainingData (DataEntry[] dataEntries,
                                                  String trainFileName,
                                                  String locFileName,
                                                  int totalNum) {
        int portionNum = totalNum / 10;
        LinkedList<CrossInfoEntry> crossInfoEntries = new LinkedList<>();
        Random rdgen = new Random();

        Scanner locFile = null;
        try {
            locFile = new Scanner(new FileReader(locFileName));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the potential list file." + locFileName);
        }
        ArrayList<DataEntry> loc = new ArrayList<>();
        while (locFile.hasNextLine()) {
            if (crossInfoEntries.size() % portionNum == 0) {
                System.out.println("Processed " + crossInfoEntries.size() / 2 + " / " + totalNum);
            }
            int curVpinID = Integer.parseInt(locFile.nextLine().substring(1));
            DataEntry curVpin = dataEntries[curVpinID];
            int matchVpinID = curVpin.getMatchIDNum();
            DataEntry matchVpin = dataEntries[matchVpinID];
			
            if (matchVpin == null) {
                while(locFile.hasNextLine() && !locFile.nextLine().equals("!"));
            } else {
                loc.clear();
                while (locFile.hasNextLine()) {
                    String tempVpinID = locFile.nextLine();
                    if(!tempVpinID.equals("!")) {
                        loc.add(dataEntries[Integer.parseInt(tempVpinID.substring(1))]);
                    } else if (!loc.isEmpty()){
                        int index = rdgen.nextInt(loc.size());
                        while (loc.get(index).getCurrentIDNum() == matchVpinID && loc.size() > 1)
                            index = rdgen.nextInt(loc.size());
                        if (loc.get(index).getCurrentIDNum() != matchVpinID) {
							crossInfoEntries.add(new CrossInfoEntry(curVpin, matchVpin));
                            crossInfoEntries.add(new CrossInfoEntry(curVpin, loc.get(index)));
						}
                        break;
                    }
					else break;
                }
            }
        }
        // write crossinfos to training file
        try {
            writeArffFile(crossInfoEntries, trainFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot write train crossinfos.");
        }
    }

    private static float generateTrainingData (DataEntry[] dataEntries,
                                             ArrayList<Integer> vpinIDNums,
                                             String trainFileName,
                                             double scalingFactor,
                                             boolean boundingBox) {
        LinkedList<CrossInfoEntry> crossInfoEntries = new LinkedList<>();
        Random rdgen = new Random();
        DataEntry dataEntryTemp;
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
            for (CrossInfoEntry c : crossInfoEntries){
                if (c.getHammingVpin() <= scalingFactor * maxHammingDis)
                    crossInfoEntriesTemp.add(c);
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
            System.out.println("Number of connected instances in training set = " + crossInfoEntries.size());

            int pInsNum = crossInfoEntries.size();
            int firstIndex;
            int secondIndex;
            for (int i = 0; i < pInsNum; i++) {
                firstIndex = rdgen.nextInt(vpinIDNums.size());
                secondIndex = rdgen.nextInt(vpinIDNums.size());
                while (firstIndex == secondIndex ||
                        dataEntries[vpinIDNums.get(firstIndex)].getMatchIDNum() == vpinIDNums.get(secondIndex)){
                    firstIndex = rdgen.nextInt(vpinIDNums.size());
                    secondIndex = rdgen.nextInt(vpinIDNums.size());
                }
                crossInfoEntries.add(new CrossInfoEntry(dataEntries[vpinIDNums.get(firstIndex)], dataEntries[vpinIDNums.get(secondIndex)]));
            }
            System.out.println("Number of instance in training set = " + crossInfoEntries.size());

            // write crossinfos to training file
            try {
                writeArffFile(crossInfoEntries, trainFileName);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot write train crossinfos.");
            }
            return 0;
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
