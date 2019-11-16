package com.example.clicker;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd");
        String url = "https://api.solunar.org/solunar/" + loc.getLatitude() + "," + loc.getLongitude() + "," + simpleDateFormat.format(cal.getTime()) + "," + offset;
        RequestQueue queue = Volley.newRequestQueue(this);
        final String finalOffset = offset;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);

                            ((TextView) findViewById(R.id.lon)).setText(Double.toString(loc.getLongitude()));
                            ((TextView) findViewById(R.id.lat)).setText(Double.toString(loc.getLatitude()));
                            ((TextView) findViewById(R.id.offset)).setText(finalOffset);
                            ((TextView) findViewById(R.id.sunRise)).setText(parseTime(reader.getString("sunRise")));
                            ((TextView) findViewById(R.id.sunSet)).setText(parseTime(reader.getString("sunSet")));
                            ((TextView) findViewById(R.id.moonRise)).setText(parseTime(reader.getString("moonRise")));
                            ((TextView) findViewById(R.id.moonTransit)).setText(parseTime(reader.getString("moonTransit")));
                            ((TextView) findViewById(R.id.moonUnder)).setText(parseTime(reader.getString("moonUnder")));
                            ((TextView) findViewById(R.id.moonSet)).setText(parseTime(reader.getString("moonSet")));
                            ((TextView) findViewById(R.id.moonPhase)).setText(reader.getString("moonPhase"));
                            ((TextView) findViewById(R.id.minor)).setText(parse(reader.getString("minor1Start"),
                                    reader.getString("minor1Stop"),
                                    reader.getString("minor2Start"),
                                    reader.getString("minor2Stop")));
                            ((TextView) findViewById(R.id.major)).setText(parse(reader.getString("major1Start"),
                                    reader.getString("major1Stop"),
                                    reader.getString("major2Start"),
                                    reader.getString("major2Stop")));

                        } catch (Exception e) {
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

    private String parse(String aStart, String aStop, String bStart, String bStop) throws ParseException {
        return parseTime(aStart) + " - " + parseTime(aStop) + "    " + parseTime(bStart) + " - " + parseTime(bStop);
    }

    String parseTime(String time) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
        final Date dateObj = sdf.parse(time);
        return new SimpleDateFormat("h:mm a").format(dateObj);

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
