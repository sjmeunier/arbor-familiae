package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addNote(Note note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addNotes(Note... note);

    @Query("select * from note where treeId =:treeId")
    public List<Note> getAllNotes(int treeId);

    @Query("select * from note where noteId = :noteId and treeId = :treeId limit 1")
    public Note getNote(int treeId, int noteId);

    @Query("select * from note where noteId IN(:noteIds) and treeId = :treeId")
    public List<Note> getNotesInList(int treeId, int[] noteIds);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateNote(Note note);

    @Query("select count(*) from note where treeId = :treeId")
    public int getCount(int treeId);

    @Query("delete from note where noteId = :noteId and treeId = :treeId")
    void delete(int treeId, int noteId);

    @Query("delete from note where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
