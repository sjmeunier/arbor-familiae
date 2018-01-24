package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Source {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int treeId;
    public final int sourceId;

    public String text;


    public Source(int sourceId, int treeId) {

        this.sourceId = sourceId;
        this.treeId = treeId;

        this.text = "";
    }
}
