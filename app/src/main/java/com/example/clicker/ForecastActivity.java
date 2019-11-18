package com.example.clicker;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ForecastActivity extends AppCompatActivity {
    private Button homeBtn;
    private Location loc;
    private Calendar cal;
    private String offset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        homeBtn = (Button) findViewById(R.id.home);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        loc = (Location) getIntent().getExtras().get("LOCATION");

        TimeZone tz = TimeZone.getDefault();
        cal = GregorianCalendar.getInstance(tz);

        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + Integer.parseInt(offset.split(":")[0]);
        setDate();
    }

    private void setDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy h:mm a");
        ((TextView) findViewById(R.id.dateView)).setText(simpleDateFormat.format(cal.getTime()));
        showSolunar();
        showWeather();
    }

    public void showSolunar() {
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
        Date moonOverHead = startOfDay.getTime();
        Date moonUnderFoot = startOfDay.getTime();
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
                moonOverHead = afterAddingMins;
                increasing = false;
                decreasing = true;
            } else if (decreasing && alt > prev) {
                moonUnderFoot = afterAddingMins;
                decreasing = false;
                increasing = true;
            } else if (prev != 0) {
                if (alt > prev) increasing = true;
                else decreasing = true;
            }
            prev = alt;
        }
        ((TextView) findViewById(R.id.lon)).setText(Double.toString(loc.getLongitude()));
        ((TextView) findViewById(R.id.lat)).setText(Double.toString(loc.getLatitude()));
        ((TextView) findViewById(R.id.offset)).setText(offset);
        ((TextView) findViewById(R.id.sunRise)).setText(parseTime(times.getRise()));
        ((TextView) findViewById(R.id.sunSet)).setText(parseTime(times.getSet()));
        ((TextView) findViewById(R.id.moonRise)).setText(parseTime(moon.getRise()));
        ((TextView) findViewById(R.id.moonSet)).setText(parseTime(moon.getSet()));
        ((TextView) findViewById(R.id.moonTransit)).setText(parseTime(moonOverHead));
        ((TextView) findViewById(R.id.moonUnder)).setText(parseTime(moonUnderFoot));

        //   ((TextView) findViewById(R.id.moonPhase)).setText(reader.getString("moonPhase"));
        ((TextView) findViewById(R.id.minor)).setText(addMinor(moon));
        ((TextView) findViewById(R.id.major)).setText(addMajor(moonOverHead, moonUnderFoot));
    }

    private String addMinor(MoonTimes moon) {
        if (moon.getSet().getTime() < moon.getRise().getTime())
            return parseTime(new Date(moon.getSet().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getSet().getTime() + 1800000)) + "    " +
                    parseTime(new Date(moon.getRise().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getRise().getTime() + 1800000));
        else
            return parseTime(new Date(moon.getRise().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getRise().getTime() + 1800000)) + "    " +
                    parseTime(new Date(moon.getSet().getTime() - 1800000)) + " - " +
                    parseTime(new Date(moon.getSet().getTime() + 1800000));
    }

    private String addMajor(Date moonOverHead, Date moonUnderFoot) {
        if (moonOverHead.getTime() > moonUnderFoot.getTime())
            return parseTime(new Date(moonUnderFoot.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonUnderFoot.getTime() + 3600000)) + "    " +
                    parseTime(new Date(moonOverHead.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonOverHead.getTime() + 3600000));
        else
            return parseTime(new Date(moonOverHead.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonOverHead.getTime() + 3600000)) + "    " +
                    parseTime(new Date(moonUnderFoot.getTime() - 3600000)) + " - " +
                    parseTime(new Date(moonUnderFoot.getTime() + 3600000));
    }

    String parseTime(Date time) {
        return new SimpleDateFormat("h:mm a").format(time);
    }

    public void showWeather() {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + loc.getLatitude() + "," + loc.getLongitude();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main = reader.getJSONObject("currently");
                            ((TextView) findViewById(R.id.temperature)).setText(main.getString("temperature"));
                            ((TextView) findViewById(R.id.dewPoint)).setText(main.getString("dewPoint"));
                            ((TextView) findViewById(R.id.windSpeed)).setText(main.getString("windSpeed"));
                            ((TextView) findViewById(R.id.pressure)).setText(main.getString("pressure"));

                            JSONObject main1 = reader.getJSONObject("daily");
                            ((TextView) findViewById(R.id.summary)).setText(main1.getString("summary"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }


    public void nextDay(View view) {
        cal.add(Calendar.DATE, 1);
        setDate();
    }

    public void prevDay(View view) {
        cal.add(Calendar.DATE, -1);
        setDate();
    }
}
