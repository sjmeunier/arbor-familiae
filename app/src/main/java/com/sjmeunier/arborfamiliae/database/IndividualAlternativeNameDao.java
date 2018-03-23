package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndividualAlternativeNameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualAlternativeName(IndividualAlternativeName individualAlternativeName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividualAlternativeNames(IndividualAlternativeName... individualAlternativeNames);

    @Query("select * from individual_alternative_name where treeId = :treeId")
    public List<IndividualAlternativeName> getAllIndividualAlternativeNames(int treeId);

    @Query("select * from individual_alternative_name where treeId = :treeId and individualId = :individualId")
    public List<IndividualAlternativeName> getAllIndividualAlternativeNameForIndividual(int treeId, int individualId);

    @Query("select * from individual_alternative_name where id = :individualAlternativeNameId and treeId = :treeId limit 1")
    public IndividualAlternativeName getIndividualAlternativeName(int treeId, int individualAlternativeNameId);

    @Query("select count(*) from individual_alternative_name where treeId = :treeId")
    public int getCount(int treeId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIndividualAlternativeName(IndividualAlternativeName individualAlternativeName);

    @Query("delete from individual_alternative_name where individualId = :individualId and treeId = :treeId")
    void delete(int treeId, int individualId);

    @Query("delete from individual_alternative_name where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
