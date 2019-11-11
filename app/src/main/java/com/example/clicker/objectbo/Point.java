package com.example.clicker.objectbo;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Point {
    @Id
    private long id;
    private String name;
    private double lon;
    private double lat;

    public Point() {
    }

    public Point(long id, String name, double lon, double lat) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

}
