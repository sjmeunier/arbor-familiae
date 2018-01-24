package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "family_source")
public class FamilySource {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public final int treeId;
    public final int familyId;
    public final int sourceId;


    public FamilySource(int treeId, int familyId, int sourceId) {
        this.treeId = treeId;
        this.familyId = familyId;
        this.sourceId = sourceId;
    }

}
