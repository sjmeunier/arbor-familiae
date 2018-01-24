package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int treeId;
    public final int noteId;

    public String text;


    public Note(int noteId, int treeId) {

        this.noteId = noteId;
        this.treeId = treeId;

        this.text = "";
    }
}
