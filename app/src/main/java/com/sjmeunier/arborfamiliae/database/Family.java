package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Family {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int treeId;
    public final int familyId;

    public int husbandId;
    public int wifeId;
    public String marriageDate;
    public int marriagePlace;

    public Family(int familyId, int treeId) {

        this.treeId = treeId;
        this.familyId = familyId;

        this.husbandId = 0;
        this.wifeId = 0;
        this.marriageDate = "";
        this.marriagePlace = -1;
    }

}
