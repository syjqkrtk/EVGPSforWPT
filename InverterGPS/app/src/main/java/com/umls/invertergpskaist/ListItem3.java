package com.umls.invertergpskaist;

public class ListItem3 {
    private double PadLati;
    private double PadLongi;
    private double PadLati2;
    private double PadLongi2;
    private int padnum;

    public int getPadNum() {
        return padnum;
    }

    public void setPadNum(int num) {
        this.padnum = num;
    }

    public double getPadLati() {
        return PadLati;
    }

    public double getPadLongi() {
        return PadLongi;
    }

    public double getPadLati2() {
        return PadLati2;
    }

    public double getPadLongi2() {
        return PadLongi2;
    }

    public void setPadLati(String templati) {
        this.PadLati = Double.parseDouble(templati);
    }

    public void setPadLongi(String templongi) {
        this.PadLongi = Double.parseDouble(templongi);
    }

    public void setPadLati2(String templati) {
        this.PadLati2 = Double.parseDouble(templati);
    }

    public void setPadLongi2(String templongi) {
        this.PadLongi2 = Double.parseDouble(templongi);
    }
}