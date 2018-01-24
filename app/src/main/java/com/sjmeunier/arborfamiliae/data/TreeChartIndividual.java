package com.sjmeunier.arborfamiliae.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.sjmeunier.arborfamiliae.database.GenderEnum;

@Entity
public class TreeChartIndividual {

    public final int individualId;

    public int ahnenNumber;
    public String name;
    public String dates;
    public GenderEnum gender;
    public float boxCentreX;
    public float boxCentreY;
    public int childAhnenNumber;
    public boolean recordExists;

    public TreeChartIndividual(int individualId, int ahnenNumber, String name, String dates, GenderEnum gender, float boxCentreX, float boxCentreY, int childAhnenNumber, boolean recordExists) {

        this.individualId = individualId;
        this.ahnenNumber = ahnenNumber;

        this.gender = gender;
        this.name = name;
        this.dates = dates;
        this.boxCentreX = boxCentreX;
        this.boxCentreY = boxCentreY;
        this.recordExists = recordExists;
        this.childAhnenNumber = childAhnenNumber;
    }
}
