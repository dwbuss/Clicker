package com.example.clicker;

import android.location.Location;

import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Solunar {
    public String longitude;
    public String latitude;
    public String offset;
    public String sunRise;
    public String sunSet;
    public String moonRise;
    public String moonSet;
    public String moonOverHead;
    public String moonUnderFoot;
    public String moonPhase;
    public String moonPhaseIcon;
    public String minor;
    public String major;

    public void populate(Location loc, Calendar cal) {
        int offsetInMillis = TimeZone.getDefault().getOffset(cal.getTimeInMillis());
        offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + Integer.parseInt(offset.split(":")[0]);
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(cal.getTime());
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        SunTimes times = SunTimes.compute()
                .on(startOfDay)
                .fullCycle()
                .at(loc.getLatitude(), loc.getLongitude())
                .execute();
        MoonTimes moon = MoonTimes.compute()
                .on(startOfDay)
                .fullCycle()
                .oneDay()
                .at(loc.getLatitude(), loc.getLongitude())
                .execute();
        MoonPosition moonp;
        double prev = 0;
        boolean increasing = false;
        boolean decreasing = false;
        Date moonOverHeadDt = null;
        Date moonUnderFootDt = null;
        Date afterAddingMins = startOfDay.getTime();
        for (int i = 0; i < 1440; i++) {
            long curTimeInMs = afterAddingMins.getTime();
            afterAddingMins = new Date(curTimeInMs + 60000);
            moonp = MoonPosition.compute()
                    .at(loc.getLatitude(), loc.getLongitude())
                    .on(afterAddingMins)
                    .execute();
            double alt = moonp.getAltitude();
            if (increasing && alt < prev) {
                moonOverHeadDt = afterAddingMins;
                increasing = false;
                decreasing = true;
            } else if (decreasing && alt > prev) {
                moonUnderFootDt = afterAddingMins;
                decreasing = false;
                increasing = true;
            } else if (prev != 0) {
                if (alt > prev) increasing = true;
                else decreasing = true;
            }
            prev = alt;
        }
        longitude = Double.toString(loc.getLongitude());
        latitude = Double.toString(loc.getLatitude());
        sunRise = parseTime(times.getRise());
        sunSet = parseTime(times.getSet());
        moonRise = parseTime(moon.getRise());
        moonSet = parseTime(moon.getSet());
        moonOverHead = parseTime(moonOverHeadDt);
        moonUnderFoot = parseTime(moonUnderFootDt);
        minor = addMinor(moon);
        major = addMajor(moonOverHeadDt, moonUnderFootDt);
    }

    String parseTime(Date time) {
        if (time != null)
            return new SimpleDateFormat("h:mm a").format(time);
        return "N/A";
    }

    private String addMajor(Date moonOverHead, Date moonUnderFoot) {
        if (moonOverHead != null && moonUnderFoot != null && moonOverHead.getTime() > moonUnderFoot.getTime())
            return parseTime(new Date(moonUnderFoot.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonUnderFoot.getTime() + 3600000)) + "    " +
                    parseTime(new Date(moonOverHead.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonOverHead.getTime() + 3600000));
        else {
            String range = "";

            if (moonOverHead != null)
                range = parseTime(new Date(moonOverHead.getTime() - 3600000)) + " - " +
                        parseTime(new Date(moonOverHead.getTime() + 3600000)) + "    ";
            else
                range = "N/A ";
            if (moonUnderFoot != null)
                range += parseTime(new Date(moonUnderFoot.getTime() - 3600000)) + " - " +
                        parseTime(new Date(moonUnderFoot.getTime() + 3600000));
            else
                range += "N/A";
            return range;
        }
    }

    private String addMinor(MoonTimes moon) {
        if (moon.getSet() != null && moon.getRise() != null && moon.getSet().getTime() < moon.getRise().getTime())
            return parseTime(new Date(moon.getSet().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getSet().getTime() + 1800000)) + "    " +
                    parseTime(new Date(moon.getRise().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getRise().getTime() + 1800000));
        else {
            String range = "";
            if (moon.getRise() != null)
                range = parseTime(new Date(moon.getRise().getTime() - 1800000)) + " - " +
                        parseTime(new Date(moon.getRise().getTime() + 1800000)) + "    ";
            else
                range = "N/A ";
            if (moon.getSet() != null)
                range += parseTime(new Date(moon.getSet().getTime() - 1800000)) + " - " +
                        parseTime(new Date(moon.getSet().getTime() + 1800000));
            else
                range += "N/A";
            return range;
        }
    }
}
