package com.umls.invertergpskaist;

public class ListItem2 {
    private String on;
    private Double distance;
    private Double speed;
    private Double arrival;
    private Double arrival2;

    public String getON() {
        return on;
    }

    public void setON(int num) {
        if (num == 0) {
            this.on = "OFF";
        }
        else {
            this.on = "ON";
        }
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double data) {
        this.distance = data;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double data) {
        this.speed = data;
    }

    public Double getArrival() {
        return arrival;
    }
    public Double getArrival2() {
        return arrival2;
    }

    public void setArrival(Double data) {
        this.arrival = data;
    }
    public void setArrival2(Double data) {
        this.arrival2 = data;
    }
}