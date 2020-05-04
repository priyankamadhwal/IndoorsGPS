package com.example.indoorsgps;

public class UserLocationModel {

    private double latitude;
    private double longitude;
    private double altitude;
    private String buildingId;

    public UserLocationModel(double latitude , double longitude, double altitude, String buildingId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.buildingId = buildingId;
    }

}
