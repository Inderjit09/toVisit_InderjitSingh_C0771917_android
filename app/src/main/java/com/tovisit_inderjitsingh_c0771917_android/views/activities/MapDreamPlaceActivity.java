package com.tovisit_inderjitsingh_c0771917_android.views.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.tovisit_inderjitsingh_c0771917_android.R;
import com.tovisit_inderjitsingh_c0771917_android.database.AppDatabase;
import com.tovisit_inderjitsingh_c0771917_android.models.RefreshDataBroadcastReceiver;
import com.tovisit_inderjitsingh_c0771917_android.networking.volley.GetByVolley;
import com.tovisit_inderjitsingh_c0771917_android.networking.volley.VolleySingleton;

import org.json.JSONObject;

public class MapDreamPlaceActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private GoogleMap gMap = null;
    private LocationRequest locationRequest = null;
    private LocationCallback locationCallback = null;
    private LocationSettingsRequest.Builder builder;
    private int PERMISSION_REQUEST_CODE = 323;
    private double mLatitude, mLongitude, destLatitude, destLongitude;
    private static int ALL_PERMISSIONS_REQUEST_CODE = 221;
    private AppDatabase database;
    private String destinationAddress = "";
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    private MarkerOptions markerOptions = null;
    private Marker currentMarker = null;
    private int databaseId = 0;
    private boolean isScreenStarted = true;
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_dream_place);
        database = AppDatabase.getInstance(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        subscribeRefreshDataReceiver();
        initializeLocation();
        getLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onBackPressed(); }
        });
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        String latitude = intent.getStringExtra("latitude");
        String longitude = intent.getStringExtra("longitude");
        String databaseId = intent.getStringExtra("id");
        this.databaseId = Integer.parseInt(databaseId);
        destLatitude = Double.parseDouble(latitude);
        destLongitude = Double.parseDouble(longitude);
        destinationAddress = address;

        Location destLocation = new Location(LocationManager.GPS_PROVIDER);
        destLocation.setLatitude(destLatitude);
        destLocation.setLongitude(destLongitude);

        callGetDirection(destLocation);
        isScreenStarted = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentLocationWithMap();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.setMapType(gMap.MAP_TYPE_NORMAL);
        if (gMap.isIndoorEnabled()) {
            gMap.setIndoorEnabled(false);
        }

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                 }

            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                googleMap.clear();
                destLatitude = arg0.getPosition().latitude;
                destLongitude = arg0.getPosition().longitude;

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(destLatitude);
                location.setLongitude(destLongitude);
                callGetDirection(location);
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                 }
        });
        if (checkPermissions())
            gMap.setMyLocationEnabled(true);
    }

    private void initializeLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                currentLocationWithMap();
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };
    }

    private void currentLocationWithMap() {
        if (checkPermissions()) {
            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location.getLatitude() != 0.0) {

                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                        LatLng latLng = new LatLng(mLatitude, mLongitude);
                        setCurrentMarker(latLng);
                        if (isScreenStarted) {
                            getIntentData();
                        }

                    } else {
                        getLastKnownLocation();
                    }
                }
            });
        }
    }

    private void setCurrentMarker(LatLng latLng) {
        if (currentMarker != null)
            currentMarker.remove();
        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(getString(R.string.your_location));
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15.0f)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        currentMarker = gMap.addMarker(markerOptions);
    }

    private void subscribeRefreshDataReceiver() {
        RefreshDataBroadcastReceiver refreshDataBroadcastReceiver = new RefreshDataBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshDataBroadcastReceiver,
                new IntentFilter("refreshData"));
    }

    private void callGetDirection(final Location location) {
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                drawPolylineGetDirections(latLng), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setCurrentMarker(new LatLng(mLatitude, mLongitude));
                GetByVolley.getDirection(response, gMap, location, database, MapDreamPlaceActivity.this, databaseId);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { }
        });
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private String drawPolylineGetDirections(LatLng location) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=").append(mLatitude).append(",").append(mLongitude);
        googleDirectionUrl.append("&destination=").append(location.latitude).append(",").append(location.longitude);
        googleDirectionUrl.append("&key=").append(getString(R.string.google_api_key));
        return googleDirectionUrl.toString();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (gMap != null) {
            gMap.clear();
            currentLocationWithMap();
            if (destLatitude != 0.0) {
                callGetDirection(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(@NonNull String provider) { }

    @Override
    public void onProviderDisabled(@NonNull String provider) { }

    private void getLastKnownLocation() {
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).
                checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    if (response != null) {
                        currentLocationWithMap();
                    }
                } catch (ResolvableApiException exception) {
                    if (exception.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            ResolvableApiException resolvable = exception;
                            resolvable.startResolutionForResult(MapDreamPlaceActivity.this, PERMISSION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ALL_PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ALL_PERMISSIONS_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    currentLocationWithMap();
                }
            }
        }
    }
}