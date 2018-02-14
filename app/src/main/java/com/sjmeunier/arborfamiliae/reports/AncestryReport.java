package com.sjmeunier.arborfamiliae.reports;

import android.content.Context;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import java.io.IOException;
import java.util.Map;

public class AncestryReport extends BaseReport {

    public AncestryReport(Context context, AppDatabase database, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
        super(context, database, placesInActiveTree, individualsInActiveTree, familiesInActiveTree, nameFormat, maxGenerations, treeId);
    }

    @Override
    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        return false;
    }
}
