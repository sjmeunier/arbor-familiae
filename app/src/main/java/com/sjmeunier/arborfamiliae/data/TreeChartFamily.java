package com.sjmeunier.arborfamiliae.data;

import android.arch.persistence.room.Entity;

import com.sjmeunier.arborfamiliae.database.GenderEnum;

import java.util.ArrayList;
import java.util.List;

public class TreeChartFamily {
    public TreeChartIndividual spouse;
    public List<TreeChartIndividual> children;

    public TreeChartFamily() {
        spouse = null;
        children = new ArrayList<TreeChartIndividual>();

    }
}
