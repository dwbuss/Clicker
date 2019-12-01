package com.example.clicker;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.example.clicker.objectbo.Point;
import com.example.clicker.objectbo.PointListAdapter;
import com.example.clicker.objectbo.Point_;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    private static final int pic_id = 123;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private List<Point> pointList = new ArrayList<>();
    private PointListAdapter pointListAdapter;
    private Map<String, Float> colors;
    private boolean follow = false;

    private String mCurrentPhotoPath;
    private boolean northUp = false;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyReceiver solunarReciever;

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
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), ALL_PERMISSIONS_RESULT);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = ((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                3,
                locationListenerGPS);

        solunarReciever = new MyReceiver(getLocation());
        registerReceiver(solunarReciever, new IntentFilter(Intent.ACTION_TIME_TICK));
        getLocation();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(solunarReciever);
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
        Solunar solunar = new Solunar();
        solunar.populate(getLocation(), GregorianCalendar.getInstance());
        TextView majorText = ((TextView) findViewById(R.id.majorLbl));
        TextView minorText = ((TextView) findViewById(R.id.minorLbl));
        majorText.setText(solunar.major);
        minorText.setText(solunar.minor);
        if (solunar.isMajor)
            flash(majorText);

        if (solunar.isMinor)
            flash(minorText);

        ((ImageButton) findViewById(R.id.forecastButton)).setImageResource(solunar.moonPhaseIcon);
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
            addCrowLayer();
        }
    }

    private void flash(TextView textObj) {
        ObjectAnimator animator = ObjectAnimator.ofInt(textObj, "backgroundColor", Color.TRANSPARENT, Color.BLUE);
        animator.setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(Animation.INFINITE);
        animator.start();
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
                Intent forecast = new Intent(this, ForecastActivity.class);
                forecast.putExtra("LOCATION", getLocation());
                startActivity(forecast);
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

    private boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int result3 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);
        int result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return (result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
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
        final Point point = new Point(0, username, contactType, loc.getLongitude(), loc.getLatitude());
        final Weather weather = new Weather();
        weather.populate(loc, point.getTimeStamp(), getApplicationContext(), new VolleyCallBack() {
            @Override
            public void onSuccess() {
                point.setAirTemp(weather.temperature);
                point.setDewPoint(weather.dewPoint);
                point.setWindSpeed(weather.windSpeed);
                point.setHumidity(weather.humidity);
                point.setPressure(weather.pressure);
                point.setCloudCover(weather.cloudCover);
                point.setWindDir(weather.windDir);
                pointListAdapter.addOrUpdatePoint(point);
                pointListAdapter.updatePoints();

                showDialogUpdate(point, addPointMarker(point));

                refreshCounts();
            }
        });

    }

    private Marker addPointMarker(Point point) {
        if (mMap != null) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.getLat(), point.getLon()))
                    .title(new SimpleDateFormat("MM-dd-yyyy h:mm a").format(point.getTimeStamp()))
                    .draggable(true)
                    .icon(getMarker(point.getContactType())));
            m.setTag(point);
            return m;
        }
        return null;
    }

    private BitmapDescriptor getMarker(String contactType) {
        if (contactType.equals("CATCH"))
            return BitmapDescriptorFactory.fromResource(R.drawable.gm_catch);
        else if (contactType.equals("CONTACT"))
            return BitmapDescriptorFactory.fromResource(R.drawable.gm_contact);
        return BitmapDescriptorFactory.fromResource(R.drawable.gm_follow);
    }

    private Location getLocation() {
        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        LatLng crow = new LatLng(49.217314, -93.863248);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crow, (float) 16.0));
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMapLongClickListener(onMyMapLongClickListener);
        mMap.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
        mMap.setOnInfoWindowLongClickListener(onInfoWindowLongClickListener);
        addCrowLayer();
        BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
        Box<Point> pointBox = boxStore.boxFor(Point.class);
        List<Point> points = pointBox.getAll();
        for (Point p : points) {
            addPointMarker(p);
        }
    }

    private void addCrowLayer() {
        File sdcard = new File("/mnt/sdcard/");
        File file = new File(sdcard, "Crow.mbtiles");

        if (!file.exists())
            Toast.makeText(this, "File not Found" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        TileProvider tileProvider = new ExpandedMBTilesTileProvider(file, 256, 256);
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
    }

    private GoogleMap.OnInfoWindowLongClickListener onInfoWindowLongClickListener = new GoogleMap.OnInfoWindowLongClickListener() {
        @Override
        public void onInfoWindowLongClick(final Marker marker) {
            final Point point = (Point) marker.getTag();
            showDialogUpdate(point, marker);
        }
    };

    private void showDialogUpdate(final Point point, final Marker marker) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.update_dialog);
        // dialog.setTitle("Update");

        ((EditText) dialog.findViewById(R.id.name)).setText(point.getName());
        ((EditText) dialog.findViewById(R.id.contactType)).setText(point.getContactType());
        ((EditText) dialog.findViewById(R.id.timeStamp)).setText(point.getTimeStamp().toString());
        ((EditText) dialog.findViewById(R.id.lat)).setText(Double.toString(point.getLat()));
        ((EditText) dialog.findViewById(R.id.lon)).setText(Double.toString(point.getLon()));
        ((EditText) dialog.findViewById(R.id.bait)).setText(point.getBait());
        ((EditText) dialog.findViewById(R.id.fishSize)).setText(point.getFishSize());
        ((EditText) dialog.findViewById(R.id.airtemp)).setText(point.getAirTemp());
        ((EditText) dialog.findViewById(R.id.watertemp)).setText(point.getWaterTemp());
        ((EditText) dialog.findViewById(R.id.windSpeed)).setText(point.getWindSpeed());
        ((EditText) dialog.findViewById(R.id.windDir)).setText(point.getWindDir());
        ((EditText) dialog.findViewById(R.id.cloudCover)).setText(point.getCloudCover());
        ((EditText) dialog.findViewById(R.id.dewPoint)).setText(point.getDewPoint());
        ((EditText) dialog.findViewById(R.id.pressure)).setText(point.getPressure());
        ((EditText) dialog.findViewById(R.id.humidity)).setText(point.getHumidity());
        ((EditText) dialog.findViewById(R.id.notes)).setText(point.getNotes());

        int width = (int) (this.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (this.getResources().getDisplayMetrics().heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogDelete = new AlertDialog.Builder(MainActivity.this);
                dialogDelete.setTitle("Warning!!");
                dialogDelete.setMessage("Are you sure to delete this point?");
                dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                            Box<Point> pointBox = boxStore.boxFor(Point.class);
                            pointBox.remove(point);
                            marker.remove();
                            refreshCounts();
                            Toast.makeText(MainActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("error", e.getMessage());
                        }
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    }
                });
                dialogDelete.show();
            }
        });
        Button btnSave = dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    point.setName(((EditText) dialog.findViewById(R.id.name)).getText().toString().trim());
                    point.setLon(Double.parseDouble(((EditText) dialog.findViewById(R.id.lon)).getText().toString().trim()));
                    point.setLat(Double.parseDouble(((EditText) dialog.findViewById(R.id.lat)).getText().toString().trim()));
                    // point.setTimeStamp(Date.parse((EditText) dialog.findViewById(R.id.timeStamp)).getText().toString().trim());
                    point.setContactType(((EditText) dialog.findViewById(R.id.contactType)).getText().toString().trim());
                    point.setBait(((EditText) dialog.findViewById(R.id.bait)).getText().toString().trim());
                    point.setFishSize(((EditText) dialog.findViewById(R.id.fishSize)).getText().toString().trim());
                    point.setAirTemp(((EditText) dialog.findViewById(R.id.airtemp)).getText().toString().trim());
                    point.setWaterTemp(((EditText) dialog.findViewById(R.id.watertemp)).getText().toString().trim());
                    point.setWindSpeed(((EditText) dialog.findViewById(R.id.windSpeed)).getText().toString().trim());
                    point.setWindDir(((EditText) dialog.findViewById(R.id.windDir)).getText().toString().trim());
                    point.setCloudCover(((EditText) dialog.findViewById(R.id.cloudCover)).getText().toString().trim());
                    point.setDewPoint(((EditText) dialog.findViewById(R.id.dewPoint)).getText().toString().trim());
                    point.setPressure(((EditText) dialog.findViewById(R.id.pressure)).getText().toString().trim());
                    point.setHumidity(((EditText) dialog.findViewById(R.id.humidity)).getText().toString().trim());
                    point.setNotes(((EditText) dialog.findViewById(R.id.notes)).getText().toString().trim());
                    BoxStore boxStore = ((ObjectBoxApp) getApplicationContext()).getBoxStore();
                    Box<Point> pointBox = boxStore.boxFor(Point.class);
                    pointBox.put(point);
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Save Successful", Toast.LENGTH_SHORT).show();
                } catch (Exception error) {
                    Log.e("Update error", error.getMessage());
                }
            }
        });
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
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

    private GoogleMap.OnMapLongClickListener onMyMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
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

    public void openSettings(View view) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    public void forcast(View view) {
        Intent forecast = new Intent(this, ForecastActivity.class);
        forecast.putExtra("LOCATION", getLocation());
        startActivity(forecast);
    }

    public void switchLayer(View view) {
        final CharSequence[] items = {"Satellite", "Hybrid", "Normal", "None"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle("Choose layer");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                if (i == 1) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                if (i == 2) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                if (i == 3) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                }
            }
        });
        dialog.show();
    }

    public void openCamera(View view) {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            //Start the camera application
            startActivityForResult(camera_intent, pic_id);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            // ivCameraPreview.setImageBitmap(bitmap);
        }
    }
}
