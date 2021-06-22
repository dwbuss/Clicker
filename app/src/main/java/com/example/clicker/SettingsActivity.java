package com.example.clicker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateCounts();
    }

    private void updateCounts() {
        PointListAdapter pointListAdapter;
        pointListAdapter = new PointListAdapter(this.getApplicationContext());

        ((TextView) findViewById(R.id.dailyCatch)).setText(pointListAdapter.getDailyCatch());
        ((TextView) findViewById(R.id.dailyContact)).setText(pointListAdapter.getDailyContact());
        ((TextView) findViewById(R.id.dailyFollow)).setText(pointListAdapter.getDailyFollow());

        ((TextView) findViewById(R.id.tripCatch)).setText(pointListAdapter.getTripCatch());
        ((TextView) findViewById(R.id.tripContact)).setText(pointListAdapter.getTripContact());
        ((TextView) findViewById(R.id.tripFollow)).setText(pointListAdapter.getTripFollow());

        ((TextView) findViewById(R.id.totalCatch)).setText(pointListAdapter.getTotalCatch());
        ((TextView) findViewById(R.id.totalContact)).setText(pointListAdapter.getTotalContact());
        ((TextView) findViewById(R.id.totalFollow)).setText(pointListAdapter.getTotalFollow());
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}