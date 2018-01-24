package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FamilySourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilySource(FamilySource individualSource);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilySources(FamilySource... individualSource);

    @Query("select * from family_source where treeId =:treeId and familyId = :familyId")
    public List<FamilySource> getAllFamilySources(int treeId, int familyId);

    @Query("select * from family_source where familyId = :familyId and treeId = :treeId and sourceId = :sourceId limit 1")
    public FamilySource getFamilySource(int treeId, int familyId, int sourceId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFamilySource(FamilySource familySource);

    @Query("delete from family_source where familyId = :familyId and treeId = :treeId and sourceId = :sourceId")
    void delete(int treeId, int familyId, int sourceId);

    @Query("delete from family_source where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
