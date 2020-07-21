package com.tovisit_inderjitsingh_c0771917_android.networking.volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VolleyParser {

    /**
     * parse method to parse the jason data retrieved from directions api
     * @param jsonObject
     * @return a dictionary of distance and duration
     * */
    public ArrayList<String> parseDistance(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }

    private ArrayList<String> getDuration(JSONArray jsonArray) {
        ArrayList<String> distanceDurationDict = new ArrayList<>();
        String distance = "";
        String duration = "";
        String address = "";

        try {
            distance = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");
            duration = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
            address = jsonArray.getJSONObject(0).getString("end_address");

            distanceDurationDict.add(distance);
            distanceDurationDict.add(duration);
            distanceDurationDict.add(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distanceDurationDict;
    }

    public String[] parseDirections(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
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
}
