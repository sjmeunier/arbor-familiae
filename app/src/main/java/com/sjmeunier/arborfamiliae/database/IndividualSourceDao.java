package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndividualSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualSource(IndividualSource individualSource);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualSources(IndividualSource... individualSource);

    @Query("select * from individual_source where treeId =:treeId and individualId = :individualId")
    public List<IndividualSource> getAllIndividualSources(int treeId, int individualId);

    @Query("select * from individual_source where individualId = :individualId and treeId = :treeId and sourceId = :sourceId limit 1")
    public IndividualSource getIndividualSource(int treeId, int individualId, int sourceId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIndividualSource(IndividualSource individualSource);

    @Query("delete from individual_source where individualId = :individualId and treeId = :treeId and sourceId = :sourceId")
    void delete(int treeId, int individualId, int sourceId);

    @Query("delete from individual_source where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
