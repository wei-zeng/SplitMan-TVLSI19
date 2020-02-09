import java.util.Arrays;

public class DataEntry {

    private int currentIDNum;
    private int matchIDNum;
    private float vpinX;
    private float vpinY;
    private float pinX;
    private float pinY;
    private float wireLength;
    private float inCellArea;
    private float outCellArea;
    private int numPin;
    private double routingCongestion;
    private double placeCongestion;
    private String pinType;
    private String rawData;
    private boolean processed;
	private boolean training;
    private boolean unclearPinType = false;
    private boolean unclearCell = false;

    public DataEntry (String[] dataEntry){
        int idx = -1;
        if (dataEntry[0].charAt(0) == 'S') {
            this.currentIDNum = Integer.parseInt(dataEntry[0].substring(1));
        } else {
            if (dataEntry[0].charAt(0) == 'P') {
                this.currentIDNum = -1000000;
            } else {
                this.currentIDNum = -2000000;
            }
            idx = dataEntry[0].indexOf("_");
            assert idx >= 0;
            this.currentIDNum -= Integer.parseInt(dataEntry[0].substring(1,idx)) * 1000;
            this.currentIDNum -= Integer.parseInt(dataEntry[0].substring(idx+1));
        }
        this.vpinX = Float.parseFloat(dataEntry[1]);
        this.vpinY = Float.parseFloat(dataEntry[2]);
        this.wireLength = Float.parseFloat(dataEntry[3]);
        this.pinType = dataEntry[4];
        if (this.pinType.equals("N")) this.unclearPinType = true;
        this.numPin = Integer.parseInt(dataEntry[5]);
        this.inCellArea = Float.parseFloat(dataEntry[6]);
        this.outCellArea = Float.parseFloat(dataEntry[7]);
        if ((this.inCellArea == 1 && this.outCellArea == 0) || (this.inCellArea == 0 && this.outCellArea == 1)) this.unclearCell = true;
        this.pinX = Float.parseFloat(dataEntry[8]);
        this.pinY = Float.parseFloat(dataEntry[9]);
        this.routingCongestion = Double.parseDouble(dataEntry[10]);
        this.placeCongestion = Double.parseDouble(dataEntry[11]);
        this.matchIDNum = Integer.parseInt(dataEntry[12].substring(1));
        this.rawData = Arrays.toString(dataEntry);
        this.processed = false;
    }

    public int getCurrentIDNum() {
        return currentIDNum;
    }

    public int getMatchIDNum() {
        return matchIDNum;
    }

    public float getVpinX() {
        return vpinX;
    }

    public float getVpinY() {
        return vpinY;
    }

    public float getPinX() {
        return pinX;
    }

    public float getPinY() {
        return pinY;
    }

    public float getWireLength() {
        return wireLength;
    }

    public float getInCellArea() {
        return inCellArea;
    }

    public float getOutCellArea() { return outCellArea; }

    public int getNumPin() { return numPin; }

    public double getRoutingCongestion() {
        return routingCongestion;
    }

    public double getPlaceCongestion() {
        return placeCongestion;
    }

    public String getPinType() {
        return pinType;
    }

    public boolean isUnclearPinType() { return unclearPinType; }

    public boolean isUnclearCell() { return unclearCell; }

    @Override
    public String toString() {
        return rawData + processed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isTraining() {
        return training;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }
}
