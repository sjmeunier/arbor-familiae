package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "individual_note")
public class IndividualNote {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public final int treeId;
    public final int individualId;
    public final int noteId;


    public IndividualNote(int treeId, int individualId, int noteId) {
        this.treeId = treeId;
        this.individualId = individualId;
        this.noteId = noteId;
    }

}
