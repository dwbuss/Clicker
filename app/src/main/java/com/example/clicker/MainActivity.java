package com.example.clicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private RecyclerView rvPointList;
    private List<Point> pointList = new ArrayList<>();
    private PointListAdapter pointListAdapter;
    private Button fabAddPoint;

    LocationManager locationManager;
    Location loc;

    ArrayList<String> permissionsRejected = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_LONG).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions.toArray(new String[permissions.size()]),
                        ALL_PERMISSIONS_RESULT);
            } else {
                getLocation();
            }
        }
        initView();
    }

    public void initView() {
        pointListAdapter = new PointListAdapter(getApplicationContext(), pointList);
        pointListAdapter.updatePoints();
        rvPointList = (RecyclerView) findViewById(R.id.rvPointList);
        rvPointList.setHasFixedSize(true);
        LinearLayoutManager llm2 = new LinearLayoutManager(MainActivity.this);
        llm2.setOrientation(LinearLayoutManager.VERTICAL);
        rvPointList.setLayoutManager(llm2);
        rvPointList.setAdapter(pointListAdapter);
        fabAddPoint = (Button) findViewById(R.id.clickBtn);
        /*fabAddPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pointListAdapter.showAddEditDialog(PointListAdapter.MODE_ADD, 0);
            }
        });*/
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);

        ((TextView)findViewById(R.id.daily)).setText(Integer.toString(pointBox.getAll().size()));
        ((TextView)findViewById(R.id.weekly)).setText(Integer.toString(pointBox.getAll().size()));
        ((TextView)findViewById(R.id.total)).setText(Integer.toString(pointBox.getAll().size()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.clicker_menu, menu);
        return true;
    }

    public void showLocation(View view) {
        Toast.makeText(this, "Location " + loc.getLongitude() + ":" + loc.getLatitude(), Toast.LENGTH_LONG).show();
    }

    public void showSolunar(View view) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd");
        String url = "https://api.solunar.org/solunar/" + loc.getLatitude() + "," + loc.getLongitude() + "," + simpleDateFormat.format(Calendar.getInstance().getTime()) + ",-4";
        callApi(url);
    }

    public void showWeather(View view) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + loc.getLatitude() + "," + loc.getLongitude();
        callApi(url);
    }

    public void callApi(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response.substring(0, 1000), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }


    public void addCount(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", null);
        pointListAdapter.addOrUpdatePoint(new Point(0, username, loc.getLongitude(),loc.getLatitude() ));
        pointListAdapter.updatePoints();
        bump(R.id.total);
        bump(R.id.weekly);

        TextView totalView = (TextView) findViewById(R.id.daily);
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);

        totalView.setText(Integer.toString(pointBox.getAll().size()));
    }

    public void bump(int id) {
        TextView totalView = (TextView) findViewById(id);
        Integer total = Integer.parseInt(totalView.getText().toString());
        total++;
        totalView.setText(total.toString());
    }

    private void getLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissions) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    getLocation();
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
