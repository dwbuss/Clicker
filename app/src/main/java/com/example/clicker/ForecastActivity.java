package com.example.clicker;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
        ((ImageView) findViewById(R.id.moonView)).setImageResource(solunar.moonPhaseIcon);
    }

    public void showWeather() {
        Location loc = (Location) getIntent().getExtras().get("LOCATION");
        final Weather weather = new Weather();
        weather.populate(loc, cal.getTime(), getApplicationContext(), new VolleyCallBack() {
            @Override
            public void onSuccess() {
                ((TextView) findViewById(R.id.temperature)).setText(weather.temperature);
                ((TextView) findViewById(R.id.apparentTemperature)).setText(weather.feelsLike);
                ((TextView) findViewById(R.id.dewPoint)).setText(weather.dewPoint);
                ((TextView) findViewById(R.id.windSpeed)).setText(weather.windSpeed);
                ((TextView) findViewById(R.id.windGust)).setText(weather.windGust);
                ((TextView) findViewById(R.id.time)).setText(weather.date);
                ((TextView) findViewById(R.id.precipProbability)).setText(weather.precipProbability);
                ((TextView) findViewById(R.id.humidity)).setText(weather.humidity);
                ((TextView) findViewById(R.id.pressure)).setText(weather.pressure);
                ((TextView) findViewById(R.id.cloudCover)).setText(weather.cloudCover);
                ((TextView) findViewById(R.id.summary)).setText(weather.summary);
            }
        });
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
