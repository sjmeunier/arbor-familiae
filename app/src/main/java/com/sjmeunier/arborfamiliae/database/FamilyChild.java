package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "family_child")
public class FamilyChild {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public final int treeId;
    public final int individualId;
    public final int familyId;


    public FamilyChild(int treeId, int individualId, int familyId) {
        this.treeId = treeId;
        this.individualId = individualId;
        this.familyId = familyId;
    }

}
