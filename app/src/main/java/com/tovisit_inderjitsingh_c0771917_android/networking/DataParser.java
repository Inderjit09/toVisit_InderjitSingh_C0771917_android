package com.tovisit_inderjitsingh_c0771917_android.networking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    /**
     * parse method to parse the json data retrieved from directions api
     * @param jsonData
     * @return a dictionary of distance and duration
     * */
    public HashMap<String, String> parseDistance(String jsonData) {
        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getDuration(jsonArray);
    }

    private HashMap<String, String> getDuration(JSONArray jsonArray) {
        HashMap<String, String> distanceDurationDict = new HashMap<>();
        String distance = "";
        String duration = "";

        try {
            duration = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");
            distanceDurationDict.put("duration", duration);
            distanceDurationDict.put("distance", distance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distanceDurationDict;
    }

    public String[] parseDirections(String jsonData) {
        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPaths(jsonArray);
    }

    private String[] getPaths(JSONArray jsonArray) {
        int count = jsonArray.length();
        String[] polylinePoints = new String[count];

        for (int i=0; i<count; i++) {
            try {
                polylinePoints[i] = getPath(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return polylinePoints;
    }

    private String getPath(JSONObject jsonObject) {
        String polylinePoint = "";
        try {
            polylinePoint = jsonObject.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polylinePoint;
    }

    /**
     * parse method for getting nearby places from Place ApI
     * @param jsonData
     * */
    public List<HashMap<String, String>> parsePlace(String jsonData) {
        JSONArray jsonArray = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int count = jsonArray.length();
        List<HashMap<String, String>> placeList = new ArrayList<>();
        HashMap<String, String> placeDictionary = null;

        for (int i=0; i<count; i++) {
            try {
                placeDictionary = getPlace((JSONObject)jsonArray.get(i));
                placeList.add(placeDictionary);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placeList;
    }

    private HashMap<String, String> getPlace(JSONObject jsonObject) {
        HashMap<String, String> googlePlaceDictionary = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if (!jsonObject.isNull("name"))
                placeName = jsonObject.getString("name");
            if (!jsonObject.isNull("vicinity"))
                vicinity = jsonObject.getString("vicinity");

            latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = jsonObject.getString("reference");

            googlePlaceDictionary.put("place_name", placeName);
            googlePlaceDictionary.put("vicinity", vicinity);
            googlePlaceDictionary.put("latitude", latitude);
            googlePlaceDictionary.put("longitude", longitude);
            googlePlaceDictionary.put("reference", reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceDictionary;
    }
}




















