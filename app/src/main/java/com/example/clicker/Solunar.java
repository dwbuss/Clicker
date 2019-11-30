package com.example.clicker;

import android.location.Location;
import android.util.Log;

import org.shredzone.commons.suncalc.MoonIllumination;
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
    public int moonPhaseIcon;
    public String minor;
    public String major;
    public boolean isMajor;
    public boolean isMinor;

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

        int phase = getPhase(cal);
        moonPhase = getMoonPhaseText(phase);
        moonPhaseIcon = IMAGE_LOOKUP[phase];
        longitude = Double.toString(loc.getLongitude());
        latitude = Double.toString(loc.getLatitude());
        sunRise = parseTime(times.getRise());
        sunSet = parseTime(times.getSet());
        moonRise = parseTime(moon.getRise());
        moonSet = parseTime(moon.getSet());
        moonOverHead = parseTime(moonOverHeadDt);
        moonUnderFoot = parseTime(moonUnderFootDt);
        minor = addMinor(cal, moon);
        major = addMajor(cal, moonOverHeadDt, moonUnderFootDt);
    }

    String parseTime(Date time) {
        if (time != null)
            return new SimpleDateFormat("h:mm a").format(time);
        return "N/A";
    }

    private int getPhase(Calendar cal) {
        double MOON_PHASE_LENGTH = 29.530588853;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Convert the year into the format expected by the algorithm.
        double transformedYear = year - Math.floor((12 - month) / 10);

        // Convert the month into the format expected by the algorithm.
        int transformedMonth = month + 9;
        if (transformedMonth >= 12) {
            transformedMonth = transformedMonth - 12;
        }

        // Logic to compute moon phase as a fraction between 0 and 1
        double term1 = Math.floor(365.25 * (transformedYear + 4712));
        double term2 = Math.floor(30.6 * transformedMonth + 0.5);
        double term3 = Math.floor(Math.floor((transformedYear / 100) + 49) * 0.75) - 38;

        double intermediate = term1 + term2 + day + 59;
        if (intermediate > 2299160) {
            intermediate = intermediate - term3;
        }

        double normalizedPhase = (intermediate - 2451550.1) / MOON_PHASE_LENGTH;
        normalizedPhase = normalizedPhase - Math.floor(normalizedPhase);
        if (normalizedPhase < 0) {
            normalizedPhase = normalizedPhase + 1;
        }

        // Return the result as a value between 0 and MOON_PHASE_LENGTH
        double phase = normalizedPhase * MOON_PHASE_LENGTH;

        return ((int) Math.floor(phase)) % 30;
    }

    String getMoonPhaseText(int phaseValue) {
        if (phaseValue == 0) {
            return "New";
        } else if (phaseValue > 0 && phaseValue < 7) {
            return "Waxing Crescent";
        } else if (phaseValue == 7) {
            return "First Quarter";
        } else if (phaseValue > 7 && phaseValue < 15) {
            return "Waxing Gibbous";
        } else if (phaseValue == 15) {
            return "Full Moon";
        } else if (phaseValue > 15 && phaseValue < 23) {
            return "Waning Gibbous";
        } else if (phaseValue == 23) {
            return "Third Quarter";
        } else {
            return "Waning Crescent";
        }
    }

    private static final int[] IMAGE_LOOKUP = {
            R.drawable.moon0,
            R.drawable.moon1,
            R.drawable.moon2,
            R.drawable.moon3,
            R.drawable.moon4,
            R.drawable.moon5,
            R.drawable.moon6,
            R.drawable.moon7,
            R.drawable.moon8,
            R.drawable.moon9,
            R.drawable.moon10,
            R.drawable.moon11,
            R.drawable.moon12,
            R.drawable.moon13,
            R.drawable.moon14,
            R.drawable.moon15,
            R.drawable.moon16,
            R.drawable.moon17,
            R.drawable.moon18,
            R.drawable.moon19,
            R.drawable.moon20,
            R.drawable.moon21,
            R.drawable.moon22,
            R.drawable.moon23,
            R.drawable.moon24,
            R.drawable.moon25,
            R.drawable.moon26,
            R.drawable.moon27,
            R.drawable.moon28,
            R.drawable.moon29,
    };

    private String addMajor(Calendar cal, Date moonOverHead, Date moonUnderFoot) {
        long curTime = cal.getTime().getTime();
        long overHead = moonOverHead == null ? 0 : moonOverHead.getTime();
        long underFoot = moonUnderFoot == null ? 0 : moonUnderFoot.getTime();
        if (moonOverHead != null && moonUnderFoot != null && overHead > underFoot) {
            if (((underFoot - 3600000) > curTime && (underFoot + 3600000) < curTime) ||
                    ((overHead - 3600000) > curTime && (overHead + 3600000) < curTime))
                isMajor = true;

            return parseTime(new Date(underFoot - 3600000)) + " - " +
                    parseTime(new Date(underFoot + 3600000)) + "    " +
                    parseTime(new Date(overHead - 3600000)) + " - " +
                    parseTime(new Date(overHead + 3600000));
        } else {
            String range = "";
            if (moonOverHead != null) {
                range = parseTime(new Date(overHead - 3600000)) + " - " +
                        parseTime(new Date(overHead + 3600000)) + "    ";
                if ((overHead - 3600000) > curTime && (overHead + 3600000) < curTime)
                    isMajor = true;
            } else
                range = "N/A ";
            if (moonUnderFoot != null) {
                range += parseTime(new Date(underFoot - 3600000)) + " - " +
                        parseTime(new Date(underFoot + 3600000));
                if ((underFoot - 3600000) > curTime && (underFoot + 3600000) < curTime)
                    isMajor = true;
            } else
                range += "N/A";
            return range;
        }
    }

    private String addMinor(Calendar cal, MoonTimes moon) {
        long curTime = cal.getTime().getTime();
        long moonSet = (moon.getSet() == null) ? 0 : moon.getSet().getTime();
        long moonRise = (moon.getRise() == null) ? 0 : moon.getRise().getTime();
        if (moon.getSet() != null && moon.getRise() != null && moonSet < moonRise) {
            if (((moonRise - 1800000) > curTime && (moonRise + 1800000) < curTime) ||
                    ((moonSet - 1800000) > curTime && (moonSet + 1800000) < curTime))
                isMinor = true;
            return parseTime(new Date(moonSet - 1800000)) + " - " +
                    parseTime(new Date(moonSet + 1800000)) + "    " +
                    parseTime(new Date(moonRise - 1800000)) + " - " +
                    parseTime(new Date(moonRise + 1800000));
        } else {
            String range = "";
            if (moon.getRise() != null) {
                range = parseTime(new Date(moonRise - 1800000)) + " - " +
                        parseTime(new Date(moonRise + 1800000)) + "    ";
                if ((moonRise - 1800000) > curTime && (moonRise + 1800000) < curTime)
                    isMinor = true;
            } else
                range = "N/A ";
            if (moon.getSet() != null) {
                range += parseTime(new Date(moonSet - 1800000)) + " - " +
                        parseTime(new Date(moonSet + 1800000));
                if ((moonSet - 1800000) > curTime && (moonSet + 1800000) < curTime)
                    isMinor = true;
            } else
                range += "N/A";
            return range;
        }
    }
}
