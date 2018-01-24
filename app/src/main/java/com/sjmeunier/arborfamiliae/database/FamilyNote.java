package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "family_note")
public class FamilyNote {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public final int treeId;
    public final int familyId;
    public final int noteId;


    public FamilyNote(int treeId, int familyId, int noteId) {
        this.treeId = treeId;
        this.familyId = familyId;
        this.noteId = noteId;
    }

}
