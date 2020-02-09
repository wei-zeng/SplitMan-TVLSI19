public class CrossInfoEntry implements Comparable<CrossInfoEntry>{

    private int sourceIDNumA;
    private int sourceIDNumB;
    private float diffVpinX;
    private float diffVpinY;
    private float diffPinX;
    private float diffPinY;
    private float hammingVpin;
    private float hammingPin;
//    private String pinTypes;
    private float totalWireLength;
    private float cellAreaTotal;
    private float cellAreaDiff; // this is the difference between total out cell area - total in cell area
    private double routingCongestion;
    private double placeCongestion;
    private boolean connected;

    public CrossInfoEntry(DataEntry dataEntryA, DataEntry dataEntryB) {
        this.sourceIDNumA = dataEntryA.getCurrentIDNum();
        this.sourceIDNumB = dataEntryB.getCurrentIDNum();
        this.diffVpinX = Math.abs(dataEntryA.getVpinX() - dataEntryB.getVpinX());
        this.diffVpinY = Math.abs(dataEntryA.getVpinY() - dataEntryB.getVpinY());
        this.diffPinX = Math.abs(dataEntryA.getPinX() - dataEntryB.getPinX());
        this.diffPinY = Math.abs(dataEntryA.getPinY() - dataEntryB.getPinY());
        this.hammingVpin = this.diffVpinX + this.diffVpinY;
        this.hammingPin = this.diffPinX + this.diffPinY;

//        if ((dataEntryA.getPinType() + dataEntryB.getPinType()).equals("IO") ||
//                (dataEntryA.getPinType() + dataEntryB.getPinType()).equals("OI")) {
//            this.pinTypes = "IO";
//        } else if ((dataEntryA.getPinType() + dataEntryB.getPinType()).equals("IIO") ||
//                (dataEntryA.getPinType() + dataEntryB.getPinType()).equals("IOI")) {
//            this.pinTypes = "IIO";
//        } else if (dataEntryA.getPinType().equals("?") || dataEntryB.getPinType().equals("?")){
//            this.pinTypes = "?";
//        } else {
//            this.pinTypes = dataEntryA.getPinType() + dataEntryB.getPinType();
//        }

        this.totalWireLength = dataEntryA.getWireLength() + dataEntryB.getWireLength();
        this.cellAreaTotal = dataEntryA.getInCellArea() + dataEntryA.getOutCellArea() + dataEntryB.getInCellArea() + dataEntryB.getOutCellArea();
        this.cellAreaDiff = dataEntryA.getOutCellArea() + dataEntryB.getOutCellArea() - dataEntryA.getInCellArea() - dataEntryB.getInCellArea();
        if (dataEntryA.getMatchIDNum() == dataEntryB.getCurrentIDNum() && dataEntryA.getCurrentIDNum() == dataEntryB.getMatchIDNum()){
            this.connected = true;
        }
        else {
            this.connected = false;
        }
        this.routingCongestion = dataEntryA.getRoutingCongestion() + dataEntryB.getRoutingCongestion();
        this.placeCongestion = dataEntryA.getPlaceCongestion() + dataEntryB.getPlaceCongestion();
    }

    public int getSourceIDNumA() {
        return sourceIDNumA;
    }

    public int getSourceIDNumB() {
        return sourceIDNumB;
    }

    public float getDiffVpinX() {
        return diffVpinX;
    }

    public float getDiffVpinY() {
        return diffVpinY;
    }

    public float getDiffPinX() {
        return diffPinX;
    }

    public float getDiffPinY() {
        return diffPinY;
    }

    public float getHammingPin() {
        return hammingPin;
    }

    public float getHammingVpin() {
        return hammingVpin;
    }

    public float getCellAreaDiff() {
        return cellAreaDiff;
    }

    public float getCellAreaTotal() {
        return cellAreaTotal;
    }

    public float getTotalWireLength() {
        return totalWireLength;
    }

    public double getRoutingCongestion() {
        return routingCongestion;
    }

    public double getPlaceCongestion() {
        return placeCongestion;
    }

    //    public String getPinTypes() {
//        return pinTypes;
//    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public String toString() {
        return ("" + sourceIDNumA + ", " + sourceIDNumB + ", " + diffVpinX + ", " + diffVpinY + ", " +
        diffPinX + ", " + diffPinY + ", " + hammingVpin + ", " + hammingPin + ", " +
        totalWireLength + ", " + cellAreaDiff + ", " + cellAreaTotal + ", " + connected);
    }

    @Override
    public int compareTo(CrossInfoEntry crossInfoEntry) {
        return (int)(this.hammingVpin - crossInfoEntry.getHammingVpin());
    }
}
