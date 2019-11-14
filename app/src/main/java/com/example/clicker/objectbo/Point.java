package com.example.clicker.objectbo;

import java.util.Calendar;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Point {
    @Id
    private long id;
    private String name;
    private double lon;
    private double lat;
    private Date timeStamp;
    private String contactType;

    public Point() {
    }

    public Point(long id, String name, String contactType, double lon, double lat) {
        this.id = id;
        this.name = name;
        this.timeStamp = Calendar.getInstance().getTime();
        this.lat = lat;
        this.lon = lon;
        this.contactType = contactType;
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

    public Date getTimeStamp() {
        return timeStamp;
    }

    public double getLat() {
        return lat;
    }

    public String getContactType(){return contactType;}

}
