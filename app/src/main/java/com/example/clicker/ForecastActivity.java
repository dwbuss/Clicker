package com.example.clicker;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private Calendar cal;

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
        cal = GregorianCalendar.getInstance();
        setDate();
    }

    private void setDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy h:mm a");
        ((TextView) findViewById(R.id.dateView)).setText(simpleDateFormat.format(cal.getTime()));
        showSolunar();
        showWeather();
    }

    public void showSolunar() {
        Solunar solunar = new Solunar();
        solunar.populate((Location) getIntent().getExtras().get("LOCATION"), cal);
        ((TextView) findViewById(R.id.lon)).setText(solunar.longitude);
        ((TextView) findViewById(R.id.lat)).setText(solunar.latitude);
        ((TextView) findViewById(R.id.offset)).setText(solunar.offset);
        ((TextView) findViewById(R.id.sunRise)).setText(solunar.sunRise);
        ((TextView) findViewById(R.id.sunSet)).setText(solunar.sunSet);
        ((TextView) findViewById(R.id.moonRise)).setText(solunar.moonRise);
        ((TextView) findViewById(R.id.moonSet)).setText(solunar.moonSet);
        ((TextView) findViewById(R.id.moonTransit)).setText(solunar.moonOverHead);
        ((TextView) findViewById(R.id.moonUnder)).setText(solunar.moonUnderFoot);
        ((TextView) findViewById(R.id.moonPhase)).setText(solunar.moonPhase);
        ((TextView) findViewById(R.id.minor)).setText(solunar.minor);
        ((TextView) findViewById(R.id.major)).setText(solunar.major);
        ((ImageView)findViewById(R.id.moonView)).setImageResource(solunar.moonPhaseIcon);
    }

    public void showWeather() {
        Location loc = (Location) getIntent().getExtras().get("LOCATION");
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + loc.getLatitude() + "," + loc.getLongitude();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main = reader.getJSONObject("currently");
                            ((TextView) findViewById(R.id.temperature)).setText(main.getString("temperature") + (char) 0x00B0);
                            ((TextView) findViewById(R.id.apparentTemperature)).setText(main.getString("apparentTemperature") + (char) 0x00B0);
                            ((TextView) findViewById(R.id.dewPoint)).setText(main.getString("dewPoint") + (char) 0x00B0);
                            ((TextView) findViewById(R.id.windSpeed)).setText(main.getString("windSpeed") + " mph " + getCardinalDirection(main.getDouble("windBearing")));
                            ((TextView) findViewById(R.id.windGust)).setText(main.getString("windGust") + " mph");
                            ((TextView) findViewById(R.id.time)).setText(new SimpleDateFormat("MM-dd-yyyy h:mm a").format(new Date(1000 * Long.parseLong(main.getString("time")))));
                            ((TextView) findViewById(R.id.summary)).setText(main.getString("summary"));
                            ((TextView) findViewById(R.id.precipProbability)).setText(main.getString("precipProbability"));
                            ((TextView) findViewById(R.id.dewPoint)).setText(main.getString("dewPoint"));
                            ((TextView) findViewById(R.id.humidity)).setText(main.getString("humidity"));
                            ((TextView) findViewById(R.id.pressure)).setText(main.getString("pressure") + " mb");
                            ((TextView) findViewById(R.id.cloudCover)).setText(main.getString("cloudCover"));

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

    String getCardinalDirection(double input) {
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        int index = (int) Math.floor(((input - 22.5) % 360) / 45);
        return directions[index + 1];
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
