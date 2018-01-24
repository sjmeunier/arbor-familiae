package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjmeunier.arborfamiliae.FanchartCanvasView;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.HashMap;
import java.util.Map;

public class FanchartFragment extends Fragment{

    private MainActivity mainActivity;

    private int maxGeneration = 0;
    private AppDatabase database;
    private int treeId = 0;

    private Map<Integer, Individual> individuals;
    private FanchartCanvasView fanchartCanvas;

    private NameFormat nameFormat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fanchart, container, false);
        setHasOptionsMenu(false);

        mainActivity = (MainActivity)getActivity();
        fanchartCanvas = (FanchartCanvasView) view.findViewById(R.id.fanchart_canvas);

        drawChart();
        return view;
    }

    private void drawChart() {
        if (mainActivity.activeIndividual == null || mainActivity.activeTree == null)
            return;

        database = AppDatabase.getDatabase(mainActivity);
        treeId = mainActivity.activeTree.id;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        maxGeneration = Integer.parseInt(settings.getString("generations_preference", "4"));
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        individuals = new HashMap<Integer, Individual>();

        individuals.put(1, mainActivity.individualsInActiveTree.get(mainActivity.activeIndividual.individualId));

        processGeneration(1, 1, mainActivity.activeIndividual.parentFamilyId);

        fanchartCanvas.configureChart(individuals, maxGeneration, nameFormat);
    }

    private void processGeneration(int generation, int childAhnenNumber, int familyId) {
        Family family = mainActivity.familiesInActiveTree.get(familyId);
        if (family == null)
            return;

        Individual father = mainActivity.individualsInActiveTree.get(family.husbandId);
        if (father != null) {
            individuals.put(childAhnenNumber * 2, father);
            if (generation < maxGeneration && father.parentFamilyId != 0) {
                processGeneration(generation + 1, childAhnenNumber * 2, father.parentFamilyId);
            }
        }
        Individual mother = mainActivity.individualsInActiveTree.get(family.wifeId);
        if (mother != null) {
            individuals.put((childAhnenNumber * 2) + 1, mother);
            if (generation < maxGeneration && mother.parentFamilyId != 0) {
                processGeneration(generation + 1, (childAhnenNumber * 2) + 1, mother.parentFamilyId);
            }
        }
    }
}
