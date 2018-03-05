package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FamilyChildDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilyChild(FamilyChild familyChild);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilyChildren(FamilyChild... familyChild);

    @Query("select * from family_child where treeId =:treeId and familyId = :familyId")
    public List<FamilyChild> getAllFamilyChildren(int treeId, int familyId);

    @Query("select * from family_child where treeId =:treeId")
    public List<FamilyChild> getAllFamilyChildrenForTree(int treeId);

    @Query("select individualId from family_child where treeId =:treeId and familyId = :familyId")
    public List<Integer> getAllFamilyChildrenIds(int treeId, int familyId);

    @Query("select * from family_child where individualId = :individualId and treeId = :treeId and familyId = :familyId limit 1")
    public FamilyChild getFamilyChild(int treeId, int individualId, int familyId);

    @Query("select * from family_child where individualId = :individualId and treeId = :treeId")
    public List<FamilyChild> getAllFamiliesWithChild(int treeId, int individualId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFamilyChild(FamilyChild familyChild);

    @Query("delete from family_child where individualId = :individualId and treeId = :treeId and familyId = :familyId")
    void delete(int treeId, int individualId, int familyId);

    @Query("delete from family_child where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
