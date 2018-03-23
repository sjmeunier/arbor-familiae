package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "individual_alternative_name")
public class IndividualAlternativeName {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int individualId;
    public final int treeId;

    public String givenName;
    public String surname;
    public String prefix;
    public String suffix;

    public IndividualAlternativeName(int individualId, int treeId) {

        this.individualId = individualId;
        this.treeId = treeId;

        this.givenName = "";
        this.surname = "";
        this.prefix = "";
        this.suffix = "";

    }

}
