package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FamilyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamily(Family family);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFamilies(Family... family);

    @Query("select * from family where treeId =:treeId")
    public List<Family> getAllFamilies(int treeId);

    @Query("select * from family where familyId = :familyId and treeId = :treeId limit 1")
    public Family getFamily(int treeId, int familyId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFamily(Family family);

    @Query("delete from family where familyId = :familyId and treeId = :treeId")
    void delete(int treeId, int familyId);

    @Query("select count(*) from family where treeId = :treeId")
    public int getCount(int treeId);


    @Query("delete from family where treeId = :treeId")
    void deleteAllInTree(int treeId);

    @Query("select * from family where (husbandId = :individualId or wifeId = :individualId) and treeId = :treeId")
    public List<Family> getAllFamiliesForHusbandOrWife(int treeId, int individualId);

}
