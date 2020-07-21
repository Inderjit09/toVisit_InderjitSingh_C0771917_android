package com.tovisit_inderjitsingh_c0771917_android.networking.volley;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.tovisit_inderjitsingh_c0771917_android.database.AppDatabase;
import com.tovisit_inderjitsingh_c0771917_android.database.AppExecutors;
import com.tovisit_inderjitsingh_c0771917_android.models.DreamPlacesModel;
import com.tovisit_inderjitsingh_c0771917_android.views.activities.MapDreamPlaceActivity;
import com.tovisit_inderjitsingh_c0771917_android.views.fragments.MapsFragment;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GetByVolley {
    private static Marker marker;

    public static void getDirection(JSONObject jsonObject, GoogleMap googleMap, Location location,
                                    AppDatabase database, MapDreamPlaceActivity mapDreamPlaceActivity, int databaseId) {
        ArrayList<String> distances = null;
        VolleyParser directionParser = new VolleyParser();
        distances = directionParser.parseDistance(jsonObject);

        try {
            String distance = distances.get(0);
            String duration = distances.get(1);
            String address = distances.get(2);

            String[] directionsList;
            directionsList = directionParser.parseDirections(jsonObject);
            displayDirection(directionsList, distance, duration, googleMap, location, database, mapDreamPlaceActivity, databaseId, address);
        } catch (Exception e) {
            Toast.makeText(mapDreamPlaceActivity, "no location found.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void displayDirection(String[] directionsList, String distance, String duration, GoogleMap googleMap,
                                         Location location, AppDatabase database, MapDreamPlaceActivity
                                                 mapDreamPlaceActivity, int databaseId, String address) {
        if (marker != null)
            marker.remove();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(address)
                .snippet("Distance : " + distance + " , " + "Duration : " + duration);
        marker = googleMap.addMarker(options);
        marker.setIcon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        marker.setDraggable(true);

        for (int i = 0; i < directionsList.length; i++) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .addAll(PolyUtil.decode(directionsList[i]));
            googleMap.addPolyline(polylineOptions);
        }
        saveDataIntoDatabase(location.getLatitude(), location.getLongitude(), address, database, mapDreamPlaceActivity, databaseId);
    }

    public static void saveDataIntoDatabase(Double latitude, Double longitude, String address, final AppDatabase database, MapDreamPlaceActivity mapDreamPlaceActivity, int databaseId) {

        DreamPlacesModel place = null;

        if (TextUtils.isEmpty(address)) {
            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            address = df.format(c);
        }
        place = new DreamPlacesModel(latitude, longitude, address, false, databaseId);
        final DreamPlacesModel finalPlace = place;
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                database.dreamPlaces().updateLocation(finalPlace);
            }
        });
        Toast.makeText(mapDreamPlaceActivity, "Place updated", Toast.LENGTH_SHORT).show();

        Intent placesData = new Intent("refreshData");
        LocalBroadcastManager.getInstance(mapDreamPlaceActivity).sendBroadcast(placesData);
    }

    public static void getHomeDirection(JSONObject jsonObject, GoogleMap googleMap, Location location,
                                        MapsFragment mapsFragment) {
        ArrayList<String> distances = null;
        VolleyParser directionParser = new VolleyParser();
        distances = directionParser.parseDistance(jsonObject);

        try {
            String distance = distances.get(0);
            String duration = distances.get(1);
            String address = distances.get(2);

            String[] directionsList;
            directionsList = directionParser.parseDirections(jsonObject);
            displayHomeDirection(directionsList, distance, duration, googleMap, location,
                    mapsFragment, address);
        } catch (Exception e) {
            Toast.makeText(mapsFragment.getContext(), "no location found.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void displayHomeDirection(String[] directionsList, String distance, String duration, GoogleMap googleMap,
                                             Location location, MapsFragment mapsFragment,
                                            String address) {
        if (marker != null)
            marker.remove();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mapsFragment.setDestinationMarker(latLng,address,distance,duration);
        for (int i = 0; i < directionsList.length; i++) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.RED)
                    .width(10)
                    .addAll(PolyUtil.decode(directionsList[i]));
            googleMap.addPolyline(polylineOptions);
        }
    }
}



















