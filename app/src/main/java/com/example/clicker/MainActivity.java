package com.example.clicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.example.clicker.objectbo.Point_;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    //  private RecyclerView rvPointList;
    private List<Point> pointList = new ArrayList<>();
    private PointListAdapter pointListAdapter;
    private Map<String, Float> colors;
    private boolean follow = false;

    private boolean northUp = false;

    ArrayList<String> permissionsRejected = new ArrayList<>();
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LocationManager locationManager;

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        colors = new HashMap<>();
        colors.put("CATCH", BitmapDescriptorFactory.HUE_RED);
        colors.put("FOLLOW", BitmapDescriptorFactory.HUE_BLUE);
        colors.put("CONTACT", BitmapDescriptorFactory.HUE_YELLOW);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]),
                    ALL_PERMISSIONS_RESULT);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        getLocation();
        initView();
    }

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED);
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (follow) {
                LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(coordinate)
                        .bearing(location.getBearing())
                        .zoom(mMap.getCameraPosition().zoom)
                        .build();
                if (northUp)
                    cameraPosition = new CameraPosition.Builder()
                            .target(coordinate)
                            .zoom(mMap.getCameraPosition().zoom)
                            .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
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
    };

    public void initView() {
        pointListAdapter = new PointListAdapter(getApplicationContext(), pointList);
        pointListAdapter.updatePoints();
        refreshCounts();
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        if (mMap != null) {
            mMap.clear();
            List<Point> points = pointBox.getAll();
            for (Point p : points) {
                addPointMarker(p);
            }
        }
    }

    public void refreshCounts() {
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        ((Button) findViewById(R.id.catchBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "CATCH").build().count()));
        ((Button) findViewById(R.id.contactBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "CONTACT").build().count()));
        ((Button) findViewById(R.id.followBtn)).setText(Long.toString(pointBox.query().equal(Point_.contactType, "FOLLOW").build().count()));
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
/*
    public void showSolunar(View view) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd");
        String url = "https://api.solunar.org/solunar/" + loc.getLatitude() + "," + loc.getLongitude() + "," + simpleDateFormat.format(Calendar.getInstance().getTime()) + ",-4";
        callApi(url);
    }

    public void showWeather(View view) {
        String url = "https://api.darksky.net/forecast/9741785dc8b4e476aa45f20076c71fd9/" + loc.getLatitude() + "," + loc.getLongitude();
        callApi(url);
    }*/

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

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    public void addContact(View view) {
        addPoint("CONTACT", getLocation());
    }

    public void addFollow(View view) {
        addPoint("FOLLOW", getLocation());
    }

    public void addCatch(View view) {
        addPoint("CATCH", getLocation());
    }

    public void addPoint(String contactType, Location loc) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("Username", null);
        Point point = new Point(0, username, contactType, loc.getLongitude(), loc.getLatitude());
        pointListAdapter.addOrUpdatePoint(point);
        pointListAdapter.updatePoints();
        addPointMarker(point);
        refreshCounts();
    }

    private void addPointMarker(Point point) {
        if (mMap != null)
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getLat(), point.getLon()))
                    .title(point.getName() + point.getContactType())
                    .icon(BitmapDescriptorFactory.defaultMarker(colors.get(point.getContactType()))));

    }

    private Location getLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    3,
                    locationListenerGPS);
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        File sdcard = new File("/mnt/sdcard/");
        File file = new File(sdcard, "Crow.mbtiles");

        if (!file.exists())
            Toast.makeText(this, "File not Found" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        LatLng crow = new LatLng(49.217314, -93.863248);
        mMap.addMarker(new MarkerOptions().position(crow).title("Marker in Crow"));

        TileProvider tileProvider = new ExpandedMBTilesTileProvider(file, 256, 256);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
       /* try {
            new KmlLayer(mMap, R.raw.points, getApplicationContext()).addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crow, (float) 12.0));
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMapLongClickListener(onMyMapLongClickListener);
        mMap.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if ((follow && northUp) || !follow) {
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                        mMyLocationButtonView.setBackgroundColor(Color.RED);
                        northUp = false;
                        follow = true;
                    } else if (follow) {
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                        mMyLocationButtonView.setBackgroundColor(Color.GREEN);
                        northUp = true;
                    }
                    return false;
                }
            };

    private GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = (new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int reason) {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                follow = false;
                northUp = true;
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                View mMyLocationButtonView = mapFragment.getView().findViewWithTag("GoogleMapMyLocationButton");
                mMyLocationButtonView.setBackgroundColor(Color.GRAY);
            }
        }
    });

    private GoogleMap.OnMapLongClickListener onMyMapLongClickListener =
            new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(final LatLng latLng) {

                    String[] contactType = {"CATCH", "CONTACT", "FOLLOW"};
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Choose an Action")
                            .setItems(contactType, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Location loc = new Location(LocationManager.GPS_PROVIDER);
                                    loc.setLongitude(latLng.longitude);
                                    loc.setLatitude(latLng.latitude);
                                    if (which == 0)
                                        addPoint("CATCH", loc);
                                    if (which == 1)
                                        addPoint("CONTACT", loc);
                                    if (which == 2)
                                        addPoint("FOLLOW", loc);

                                }
                            }).show();
                }
            };
}
