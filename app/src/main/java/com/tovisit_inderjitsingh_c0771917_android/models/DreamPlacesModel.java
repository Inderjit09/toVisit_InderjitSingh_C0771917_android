package com.tovisit_inderjitsingh_c0771917_android.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "placesData")
public class DreamPlacesModel {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "name")
    String name;
    @ColumnInfo(name = "latitude")
    Double latitude;
    @ColumnInfo(name = "longitude")
    Double longitude;
    @ColumnInfo(name = "isVisited")
    boolean isVisited;

    public DreamPlacesModel() {

    }

    public DreamPlacesModel(Double mlatitude, Double mlongitude, String name, boolean isVisit, int id) {
        this.latitude = mlatitude;
        this.longitude = mlongitude;
        this.name = name;
        this.isVisited = isVisit;
        this.id = id;
    }

    public boolean getVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public DreamPlacesModel(Double latitude, Double longitude, String address, boolean isVisited) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = address;
        this.isVisited = isVisited;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
