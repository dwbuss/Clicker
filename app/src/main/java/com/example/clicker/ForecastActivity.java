package com.example.clicker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ForecastActivity extends AppCompatActivity implements View.OnClickListener {
    private Calendar cal;
    private Button btnDatePicker;
    private Button btnTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        Button homeBtn = (Button) findViewById(R.id.home);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        cal = GregorianCalendar.getInstance();

        btnDatePicker = (Button) findViewById(R.id.btn_date);
        btnTimePicker = (Button) findViewById(R.id.btn_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        setDate();
    }

    private void setDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        ((TextView) findViewById(R.id.in_date)).setText(simpleDateFormat.format(cal.getTime()));
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm a");
        ((TextView) findViewById(R.id.in_time)).setText(simpleTimeFormat.format(cal.getTime()));
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
        weather.populate(loc.getLatitude(), loc.getLongitude(), cal.getTime(), getApplicationContext(), new VolleyCallBack() {
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
            }

            @Override
            public void onFailure() {

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

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            cal.set(year, monthOfYear, dayOfMonth);
                            setDate();
                        }
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        if (v == btnTimePicker) {
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            cal.set(Calendar.MINUTE, minute);
                            setDate();
                        }
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }
    }
}
