package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSource(Source source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSources(Source... source);

    @Query("select * from source where treeId =:treeId")
    public List<Source> getAllSources(int treeId);

    @Query("select * from source where sourceId = :sourceId and treeId = :treeId limit 1")
    public Source getSource(int treeId, int sourceId);

    @Query("select * from source where sourceId IN(:sourceIds) and treeId = :treeId")
    public List<Source> getSourcesInList(int treeId, int[] sourceIds);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSource(Source source);

    @Query("select count(*) from source where treeId = :treeId")
    public int getCount(int treeId);

    @Query("delete from source where sourceId = :sourceId and treeId = :treeId")
    void delete(int treeId, int sourceId);

    @Query("delete from source where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
