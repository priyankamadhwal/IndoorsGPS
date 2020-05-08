package com.acms.iexplore;

public class UserLocationModel {

    private String name;
    private double latitude;
    private double longitude;
    private double altitude;
    private String buildingId;

    public UserLocationModel(String name, double latitude , double longitude, double altitude, String buildingId) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.buildingId = buildingId;
    }

}
