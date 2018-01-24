package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface TreeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addTree(Tree tree);

    @Query("select * from tree")
    public List<Tree> getAllTrees();

    @Query("select * from tree where id = :treeId limit 1")
    public Tree getTree(int treeId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTree(Tree tree);

    @Query("delete from tree where id =:treeId")
    void delete(int treeId);
}
