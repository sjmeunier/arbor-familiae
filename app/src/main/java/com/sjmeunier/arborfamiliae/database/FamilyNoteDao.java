package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FamilyNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilyNote(FamilyNote individualNote);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilyNotes(FamilyNote... individualNote);

    @Query("select * from family_note where treeId =:treeId and familyId = :familyId")
    public List<FamilyNote> getAllFamilyNotes(int treeId, int familyId);

    @Query("select * from family_note where familyId = :familyId and treeId = :treeId and noteId = :noteId limit 1")
    public FamilyNote getFamilyNote(int treeId, int familyId, int noteId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFamilyNote(FamilyNote familyNote);

    @Query("delete from family_note where familyId = :familyId and treeId = :treeId and noteId = :noteId")
    void delete(int treeId, int familyId, int noteId);

    @Query("delete from family_note where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
