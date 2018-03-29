package com.minibee.gps.minibee_gps;

/**
 * Classe Position : objet avec 3 float (latitude, longitude, altitude)
 */
public class Position {
    private float latitude;
    private float longitude;
    private float altitude;

    public Position(float latitude, float longitude, float altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

}