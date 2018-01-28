package com.sjmeunier.arborfamiliae.data;

import com.sjmeunier.arborfamiliae.database.GenderEnum;

public class FanChartIndividual {

    public final int individualId;

    public String name;
    public GenderEnum gender;
    public float startAngle;
    public float sweepAngle;
    public int generation;


    public FanChartIndividual(int individualId, String name, GenderEnum gender, float startAngle, float sweepAngle, int generation) {

        this.individualId = individualId;

        this.gender = gender;
        this.name = name;
        this.startAngle = startAngle;
        this.sweepAngle = sweepAngle;
        this.generation = generation;
    }
}
