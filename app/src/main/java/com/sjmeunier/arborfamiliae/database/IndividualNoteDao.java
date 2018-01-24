package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndividualNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualNote(IndividualNote individualNote);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualNotes(IndividualNote... individualNote);

    @Query("select * from individual_note where treeId =:treeId and individualId = :individualId")
    public List<IndividualNote> getAllIndividualNotes(int treeId, int individualId);

    @Query("select * from individual_note where individualId = :individualId and treeId = :treeId and noteId = :noteId limit 1")
    public IndividualNote getIndividualNote(int treeId, int individualId, int noteId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIndividualNote(IndividualNote individualNote);

    @Query("delete from individual_note where individualId = :individualId and treeId = :treeId and noteId = :noteId")
    void delete(int treeId, int individualId, int noteId);

    @Query("delete from individual_note where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
