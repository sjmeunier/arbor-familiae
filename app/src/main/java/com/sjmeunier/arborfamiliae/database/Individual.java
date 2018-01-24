package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Individual {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public final int individualId;
    public final int treeId;

    public String givenName;
    public String surname;
    public String prefix;
    public String suffix;
    public GenderEnum gender;
    public String birthDate;
    public int birthPlace;
    public String occupation;
    public String description;
    public String nationality;
    public String diedDate;
    public int diedPlace;
    public String diedCause;
    public int parentFamilyId;
    public String baptismDate;
    public int baptismPlace;
    public String burialDate;
    public int burialPlace;

    public Individual(int individualId, int treeId) {

        this.individualId = individualId;
        this.treeId = treeId;

        this.gender = GenderEnum.Unknown;
        this.givenName = "";
        this.surname = "";
        this.prefix = "";
        this.suffix = "";
        this.birthDate = "";
        this.birthPlace = -1;
        this.occupation = "";
        this.description = "";
        this.nationality = "";
        this.diedDate = "";
        this.diedPlace = -1;
        this.diedCause = "";
        this.baptismDate = "";
        this.baptismPlace = -1;
        this.burialDate = "";
        this.burialPlace = -1;
        this.parentFamilyId = 0;

    }

}
