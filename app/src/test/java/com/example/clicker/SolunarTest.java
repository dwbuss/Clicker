package com.example.clicker;

import android.location.Location;

import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

public class SolunarTest {
    @Test
    public void testNotification() {

        Solunar solunar = new Solunar();
        Location loc = new Location(""){
            @Override
            public double getLatitude() {
                return 49.217314;
            }

            @Override
            public double getLongitude() {
                return -93.863248;
            }
        };
        solunar.populate(loc, GregorianCalendar.getInstance());
        //Minor 1: 7:51 AM - 8:51 AM Minor 2: 4:24 PM - 5:24 PM
        assertEquals("Minor 7:51 AM - 8:51 AM is starting - good luck!", solunar.getEventNotification("7:51 AM"));
        assertEquals("Minor 7:51 AM - 8:51 AM has ended - time for a nap!", solunar.getEventNotification("8:51 AM"));
        assertEquals("Minor 4:24 PM - 5:24 PM is starting - good luck!", solunar.getEventNotification("4:24 PM"));
        assertEquals("Minor 4:24 PM - 5:24 PM has ended - time for a nap!", solunar.getEventNotification("5:24 PM"));
        //Checking if 3:10 PM is in Major 1: 11:14 PM - 1:14 AM Major 2: 11:39 AM - 1:39 PM
        assertEquals("Major 11:14 PM - 1:14 AM is starting - good luck!", solunar.getEventNotification("11:14 PM"));
        assertEquals("Major 11:14 PM - 1:14 AM has ended - time for a nap!", solunar.getEventNotification("1:14 AM"));
        assertEquals("Major 11:39 AM - 1:39 PM is starting - good luck!", solunar.getEventNotification("11:39 AM"));
        assertEquals("Major 11:39 AM - 1:39 PM has ended - time for a nap!", solunar.getEventNotification("1:39 PM"));
    }
}