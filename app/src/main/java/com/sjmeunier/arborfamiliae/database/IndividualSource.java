package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "individual_source")
public class IndividualSource {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public final int treeId;
    public final int individualId;
    public final int sourceId;


    public IndividualSource(int treeId, int individualId, int sourceId) {
        this.treeId = treeId;
        this.individualId = individualId;
        this.sourceId = sourceId;
    }

}
