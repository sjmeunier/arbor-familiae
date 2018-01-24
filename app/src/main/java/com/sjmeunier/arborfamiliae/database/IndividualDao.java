package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IndividualDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividual(Individual individual);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addIndividuals(Individual... individuals);

    @Query("select * from individual where treeId = :treeId")
    public List<Individual> getAllIndividuals(int treeId);

    @Query("select individual.individualId as individualId, family.husbandId as fatherId, family.wifeId as motherId, family.familyId as familyId from individual left join family on individual.parentFamilyId = family.familyId where individual.treeId = :treeId and family.treeId = :treeId")
    public List<IndividualIdWithParents> getAllIndividualIdsWithParents(int treeId);

    @Query("select * from individual where individualId = :individualId and treeId = :treeId limit 1")
    public Individual getIndividual(int treeId, int individualId);

    @Query("select * from individual where treeId = :treeId and (givenName LIKE :searchText or surname LIKE :searchText or suffix LIKE :searchText) order by surname, givenName LIMIT :limit")
    public List<Individual> findIndividualsByName(int treeId, String searchText, int limit);

    @Query("select * from individual where individualId IN(:individualIds) and treeId = :treeId")
    public List<Individual> getIndividualsInList(int treeId, int[] individualIds);

    @Query("select count(*) from individual where treeId = :treeId")
    public int getCount(int treeId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIndividual(Individual individual);

    @Query("delete from individual where individualId = :individualId and treeId = :treeId")
    void delete(int treeId, int individualId);

    @Query("delete from individual where treeId = :treeId")
    void deleteAllInTree(int treeId);
}
