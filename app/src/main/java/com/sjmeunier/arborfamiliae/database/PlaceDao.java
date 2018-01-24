package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addPlace(Place place);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addPlaces(Place... places);

    @Query("select * from place where treeId =:treeId")
    public List<Place> getAllPlaces(int treeId);

    @Query("select * from place where placeId = :placeId and treeId = :treeId limit 1")
    public Place getPlace(int treeId, int placeId);

    @Query("select * from place where placeId IN(:placeIds) and treeId = :treeId")
    public List<Place> getPlacesInList(int treeId, int[] placeIds);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePlace(Place place);

    @Query("select count(*) from place where treeId = :treeId")
    public int getCount(int treeId);

    @Query("delete from place where placeId = :placeId and treeId = :treeId")
    void delete(int treeId, int placeId);

    @Query("delete from place where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
