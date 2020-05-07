package com.example.indoorsgps;

public class UserLocationModel {

    private String usersname;
    private double latitude;
    private double longitude;
    private double altitude;
    private String buildingId;

    public UserLocationModel(String usersname, double latitude , double longitude, double altitude, String buildingId) {
        this.usersname = usersname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.buildingId = buildingId;
    }

}
