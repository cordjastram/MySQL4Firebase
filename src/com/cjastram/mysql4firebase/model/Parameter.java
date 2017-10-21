package com.cjastram.mysql4firebase.model;

public class Parameter {

    public String value;

    public int position;

    public boolean isIn;

    public boolean isOut;

    public int type;

    private Parameter(int position, int type, String value, boolean isIn, boolean isOut) {
        this.position = position;
        this.type = type;
        this.value = value;
        this.isIn = isIn;
        this.isOut = isOut;
    }

    public static Parameter inParameter(int position, int type, String value) {
        return new Parameter(position, type, value, true, false);
    }

    public static Parameter outParameter(int position, int type) {
        return new Parameter(position, type, null, false, true);
    }

    public static Parameter inoutParameter(int position, int type, String value) {
        return new Parameter(position, type, value, true, true);
    }

    // needed by Firebase
    protected Parameter() {

    }

    @Override
    public String toString() {
        return "Parameter{" +
                "position=" + position +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", isIn=" + isIn +
                ", isOut=" + isOut +
                '}';
    }
}