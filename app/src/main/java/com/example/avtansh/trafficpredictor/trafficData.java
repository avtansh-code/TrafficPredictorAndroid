package com.example.avtansh.trafficpredictor;

/**
 * Created by Avtansh Gupta on 22-12-2017.
 */

public class trafficData {
    String location;
    int currSpeed;
    int normSpeed;
    String date;
    int hour;
    double cong;

    trafficData(String l, int c, int n, String d, int h, double cp){
        this.location = l;
        this.currSpeed = c;
        this.normSpeed = n;
        this.date = d;
        this.hour = h;
        this.cong = cp;
    }
}
