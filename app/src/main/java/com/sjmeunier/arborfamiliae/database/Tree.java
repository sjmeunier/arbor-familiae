package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity
public class Tree {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public Date lastUpdated;
    public int individualCount;
    public int familyCount;
    public int sourceCount;
    public int noteCount;
    public int placeCount;
    public int defaultIndividual;

    public Tree(String name, Date lastUpdated) {
        this.name = name;
        this.lastUpdated = lastUpdated;

        this.individualCount = 0;
        this.familyCount = 0;
        this.sourceCount = 0;
        this.noteCount = 0;
        this.placeCount = 0;
        this.defaultIndividual = 0;

    }
}
