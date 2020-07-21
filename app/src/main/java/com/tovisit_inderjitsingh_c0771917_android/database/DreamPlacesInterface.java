package com.tovisit_inderjitsingh_c0771917_android.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.tovisit_inderjitsingh_c0771917_android.models.DreamPlacesModel;
import java.util.List;

@Dao
public interface DreamPlacesInterface {
    @Query("Select * from placesData")
    List<DreamPlacesModel> getDreamPlacesList();
    @Insert
    void insertLocation(DreamPlacesModel dreamPlacesModel);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateLocation(DreamPlacesModel dreamPlacesModel);
    @Delete
    void deleteLocation(DreamPlacesModel dreamPlacesModel);
}
