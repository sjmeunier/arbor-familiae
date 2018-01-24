package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Place {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int treeId;
    public final int placeId;

    public String placeName;
    public float latitude;
    public float longitude;

    public Place(int treeId, int placeId) {

        this.placeId = placeId;
        this.treeId = treeId;

        this.placeName = "";
        this.latitude = -9999;
        this.longitude = -9999;
    }
}
