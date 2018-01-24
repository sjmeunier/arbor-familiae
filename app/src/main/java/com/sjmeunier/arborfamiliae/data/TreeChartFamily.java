package com.sjmeunier.arborfamiliae.data;

import android.arch.persistence.room.Entity;

import com.sjmeunier.arborfamiliae.database.GenderEnum;

import java.util.ArrayList;
import java.util.List;

@Entity
public class TreeChartFamily {
    public TreeChartIndividual spouse;
    public List<TreeChartIndividual> children;

    public TreeChartFamily() {
        children = new ArrayList<TreeChartIndividual>();

    }
}
