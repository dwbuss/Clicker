package com.example.clicker;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Weather {
    public String temperature;
    public String feelsLike;
    public String dewPoint;
    public String windSpeed;
    public String windDir;
    public String windGust;
    public String date;
    public String precipProbability;
    public String humidity;
    public String pressure;
    public String cloudCover;

    public void populate(double lat, double lon, Date cal, Context context, final VolleyCallBack callback) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon + "," + (cal.getTime()/1000);
        populate(url, context, callback);
    }

    public void populate(double lat, double lon, Context context, final VolleyCallBack callback) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + lat + "," + lon;
        populate(url, context, callback);
    }

    public void populate(String url, Context context, final VolleyCallBack callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONObject main = reader.getJSONObject("currently");
                            temperature = main.getString("temperature") + (char) 0x00B0;
                            feelsLike = main.getString("apparentTemperature") + (char) 0x00B0;
                            dewPoint = main.getString("dewPoint") + (char) 0x00B0;
                            windSpeed = main.getString("windSpeed") + " mph ";
                            windDir = getCardinalDirection(main.getDouble("windBearing"));
                            windGust = main.getString("windGust") + " mph";
                            date = new SimpleDateFormat("MM-dd-yyyy h:mm a").format(new Date(1000 * Long.parseLong(main.getString("time"))));
                            precipProbability = main.getString("precipProbability");
                            humidity = main.getString("humidity");
                            pressure = main.getString("pressure") + " mb";
                            cloudCover = main.getString("cloudCover");

                            JSONObject main1 = reader.getJSONObject("daily");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        callback.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure();
            }
        });
        queue.add(stringRequest);
    }

    String getCardinalDirection(double input) {
        String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        int index = (int) Math.floor(((input - 22.5) % 360) / 45);
        return directions[index + 1];
    }
}
