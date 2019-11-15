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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ForecastActivity extends AppCompatActivity {
    private Button homeBtn;
    private Location loc;
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
        loc = (Location) getIntent().getExtras().get("LOCATION");
        cal = Calendar.getInstance();
        setDate();
    }

    private void setDate() {
        String pattern = "MM-dd-yyyy h:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        ((TextView) findViewById(R.id.dateView)).setText(simpleDateFormat.format(cal.getTime()));
        showSolunar();
        showWeather();
    }


    public void showSolunar() {
        /*
        {"sunRise":"8:21","sunTransit":"13:50","sunSet":"19:20","moonRise":"14:44","moonTransit":"21:23","moonUnder":"9:53","moonSet":"5:02","moonPhase":"Waxing Gibbous","moonIllumination":0.805872890974292,"sunRiseDec":8.35,"sunTransitDec":13.833333333333334,"sunSetDec":19.333333333333332,"moonRiseDec":14.733333333333333,"moonSetDec":5.033333333333333,"moonTransitDec":21.383333333333333,"moonUnderDec":9.883333333333333,"minor1StartDec":14.233333333333333,"minor1Start":"14:14","minor1StopDec":15.233333333333333,"minor1Stop":"15:14","minor2StartDec":4.533333333333333,"minor2Start":"04:32","minor2StopDec":5.533333333333333,"minor2Stop":"05:32","major1StartDec":20.383333333333333,"major1Start":"20:23","major1StopDec":22.383333333333333,"major1Stop":"22:23","major2StartDec":8.883333333333333,"major2Start":"08:53","major2StopDec":10.883333333333333,"major2Stop":"10:53","dayRating":0,"hourlyRating":{"0":0,"1":0,"2":0,"3":0,"4":0,"5":20,"6":20,"7":0,"8":40,"9":40,"10":20,"11":20,"12":0,"13":0,"14":20,"15":20,"16":0,"17":0,"18":0,"19":20,"20":40,"21":20,"22":20,"23":20}}
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd");
        String url = "https://api.solunar.org/solunar/" + loc.getLatitude() + "," + loc.getLongitude() + "," + simpleDateFormat.format(cal.getTime()) + ",-4";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);

                            ((TextView) findViewById(R.id.sunRise)).setText(reader.getString("sunRise"));
                            ((TextView) findViewById(R.id.sunSet)).setText(reader.getString("sunSet"));
                            ((TextView) findViewById(R.id.moonRise)).setText(reader.getString("moonRise"));
                            ((TextView) findViewById(R.id.moonTransit)).setText(reader.getString("moonTransit"));
                            ((TextView) findViewById(R.id.moonUnder)).setText(reader.getString("moonUnder"));
                            ((TextView) findViewById(R.id.moonSet)).setText(reader.getString("moonSet"));
                            ((TextView) findViewById(R.id.moonPhase)).setText(reader.getString("moonPhase"));
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

    public void showWeather() {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + loc.getLatitude() + "," + loc.getLongitude();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main  = reader.getJSONObject("currently");
                            ((TextView) findViewById(R.id.temperature)).setText(main.getString("temperature"));
                            ((TextView) findViewById(R.id.dewPoint)).setText(main.getString("dewPoint"));
                            ((TextView) findViewById(R.id.windSpeed)).setText(main.getString("windSpeed"));
                            ((TextView) findViewById(R.id.pressure)).setText(main.getString("pressure"));

                            JSONObject main1  = reader.getJSONObject("daily");
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
