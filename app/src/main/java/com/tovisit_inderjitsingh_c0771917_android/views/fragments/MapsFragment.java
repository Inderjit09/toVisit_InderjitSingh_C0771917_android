package com.tovisit_inderjitsingh_c0771917_android.views.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.tovisit_inderjitsingh_c0771917_android.R;
import com.tovisit_inderjitsingh_c0771917_android.database.AppDatabase;
import com.tovisit_inderjitsingh_c0771917_android.database.AppExecutors;
import com.tovisit_inderjitsingh_c0771917_android.models.DreamPlacesModel;
import com.tovisit_inderjitsingh_c0771917_android.models.RefreshDataBroadcastReceiver;
import com.tovisit_inderjitsingh_c0771917_android.networking.volley.GetByVolley;
import com.tovisit_inderjitsingh_c0771917_android.networking.volley.VolleySingleton;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsFragment extends Fragment implements View.OnClickListener,
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener {
    private View rootView;
    private FusedLocationProviderClient fusedLocationProviderClient = null;
    private GoogleMap googleMap = null;
    private LocationRequest locationRequest = null;
    private LocationCallback locationCallback = null;
    private LocationSettingsRequest.Builder builder;
    private int PERMISSION_REQUEST_CODE = 323;
    private double mLatitude, mLongitude, destLatitude, destLongitude;
    private static int ALL_PERMISSIONS_REQUEST_CODE = 221;
    private int AUTOCOMPLETE_REQUEST_CODE = 132;
    private ImageView imgMapType;
    private ProgressBar progressBar;
    private TextView tvSearch;
    private AppDatabase database;
    private String destinationAddress = "";
    private int mapType = 0;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    private MarkerOptions markerOptions = null;
    private Marker currentMarker = null, destinationMarker = null;
    private boolean isMapLongPressed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        database = AppDatabase.getInstance(getContext());
        sharedpreferences = getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        subscribeRefreshDataReceiver();
        clickViews(rootView);
        initializeLocation();
        getLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        currentLocationWithMap();
        return rootView;
    }

    private void clickViews(View rootView) {
        imgMapType = rootView.findViewById(R.id.map_type);
        progressBar = rootView.findViewById(R.id.progress_bar);
        tvSearch = rootView.findViewById(R.id.tv_search);
        imgMapType.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_search: {
                String googleApiKey = getString(R.string.google_api_key);

                if (TextUtils.isEmpty(googleApiKey)) {
                    Toast.makeText(getActivity(), "Google api key required", Toast.LENGTH_SHORT).show();
                } else {
                    if (!Places.isInitialized()) {
                        Places.initialize(getContext(), getString(R.string.google_api_key));
                    }
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

                    Intent intent = new Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(getContext());
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                }
                break;
            }
            case R.id.map_type: {
                showMapTypeDialog();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentLocationWithMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (googleMap.isIndoorEnabled()) {
            googleMap.setIndoorEnabled(false);
        }
        if (checkPermissions())
            googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapLongClickListener(this);
    }

    private void showMapTypeDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_map_type);
        dialog.setCancelable(false);

        final RadioGroup rbMapType = dialog.findViewById(R.id.rb_map_type);
        final RadioButton rbDefault = dialog.findViewById(R.id.rb_default);
        final RadioButton rbSatellite = dialog.findViewById(R.id.rb_sattelite);
        final RadioButton rbTerrain = dialog.findViewById(R.id.rb_terrain);
        TextView tvSelect = dialog.findViewById(R.id.tv_select);
        TextView tvCancel = dialog.findViewById(R.id.tv_cancel);

        if (mapType == 0)
            rbMapType.check(R.id.rb_default);
        else if (mapType == 1)
            rbMapType.check(R.id.rb_sattelite);
        else if (mapType == 2)
            rbMapType.check(R.id.rb_terrain);


        tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbDefault.isChecked()) {
                    mapType = 0;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (rbSatellite.isChecked()) {
                    mapType = 1;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (rbTerrain.isChecked()) {
                    mapType = 2;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
                dialog.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            googleMap.clear();
            currentLocationWithMap();

            Place place = Autocomplete.getPlaceFromIntent(data);
            tvSearch.setText(place.getAddress());

            destLatitude = place.getLatLng().latitude;
            destLongitude = place.getLatLng().longitude;
            destinationAddress = place.getAddress();

            Location destLocation = new Location(LocationManager.GPS_PROVIDER);
            destLocation.setLatitude(destLatitude);
            destLocation.setLongitude(destLongitude);
            callGetDirection(destLocation);
        }
    }

    private void initializeLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
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

                        progressBar.setVisibility(View.GONE);

                        mLatitude = location.getLatitude();
                        mLongitude = location.getLongitude();
                        LatLng latLng = new LatLng(mLatitude, mLongitude);
                        setCurrentMarker(latLng);
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
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        currentMarker = googleMap.addMarker(markerOptions);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        googleMap.clear();
        currentLocationWithMap();
        tvSearch.setText("");
        isMapLongPressed = true;
        destinationAddress = "Destination";

        destLatitude = point.latitude;
        destLongitude = point.longitude;
        Location destLocation = new Location(LocationManager.GPS_PROVIDER);
        destLocation.setLatitude(point.latitude);
        destLocation.setLongitude(point.longitude);
        callGetDirection(destLocation);
    }

    public void setDestinationMarker(LatLng latLng, String address, String distance, String duration) {
        this.destLatitude = latLng.latitude;
        this.destLongitude = latLng.longitude;
        this.destinationAddress = address;

        if (isMapLongPressed) {
            alertToAddDreamPlace(latLng.latitude, latLng.longitude, address);
            isMapLongPressed = false;
        }

        if (destinationMarker != null)
            destinationMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        destinationMarker = googleMap.addMarker(markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .position(latLng).title(destinationAddress).snippet("Distance : " + distance + ", " +
                        "Duration : " + duration));
    }

    private void alertToAddDreamPlace(final Double lat, final Double lng, final String finalAddress) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.add_dream_place)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveDataIntoDatabase(lat, lng, finalAddress);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void saveDataIntoDatabase(Double latitude, Double longitude, String address) {
        DreamPlacesModel place = null;
        if (TextUtils.isEmpty(address)) {
            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = df.format(c);
            place = new DreamPlacesModel(latitude, longitude, currentDate, false);
        } else {
            place = new DreamPlacesModel(latitude, longitude, address, false);
        }
        final DreamPlacesModel finalPlace = place;
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.dreamPlaces().insertLocation(finalPlace);
            }
        });

        Intent placesData = new Intent("refreshData");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(placesData);
    }

    private void subscribeRefreshDataReceiver() {
        RefreshDataBroadcastReceiver refreshDataBroadcastReceiver = new RefreshDataBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshDataBroadcastReceiver,
                new IntentFilter("refreshData"));
    }


    private void callGetDirection(final Location location) {
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                drawPolylineGetDirections(latLng), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setCurrentMarker(new LatLng(mLatitude, mLongitude));
                GetByVolley.getHomeDirection(response, googleMap, location,
                        MapsFragment.this);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
    }

    private String drawPolylineGetDirections(LatLng location) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=" + mLatitude + "," + mLongitude);
        googleDirectionUrl.append(("&destination=" + location.latitude + "," + location.longitude));
        googleDirectionUrl.append("&key=" + getString(R.string.google_api_key));
        return googleDirectionUrl.toString();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (googleMap != null) {
            googleMap.clear();
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
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getActivity()).
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
                            resolvable.startResolutionForResult(getActivity(), PERMISSION_REQUEST_CODE);
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
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ALL_PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
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
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    currentLocationWithMap();
                }
            }
        }
    }
}
