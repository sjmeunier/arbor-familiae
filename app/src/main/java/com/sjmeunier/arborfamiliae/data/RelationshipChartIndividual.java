package com.sjmeunier.arborfamiliae.data;

import android.arch.persistence.room.Entity;

import com.sjmeunier.arborfamiliae.database.GenderEnum;

@Entity
public class RelationshipChartIndividual {

    public final int individualId;

    public int generationNumber;
    public String name;
    public String dates;
    public String relationship;
    public GenderEnum gender;
    public float boxCentreX;
    public float boxCentreY;

    public RelationshipChartIndividual(int individualId, int generationNumber, String name, String dates, String relationship, GenderEnum gender, float boxCentreX, float boxCentreY) {

        this.individualId = individualId;
        this.generationNumber = generationNumber;

        this.gender = gender;
        this.name = name;
        this.dates = dates;
        this.relationship = relationship;
        this.boxCentreX = boxCentreX;
        this.boxCentreY = boxCentreY;
    }
}
