package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjmeunier.arborfamiliae.AncestryUtil;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.TreeChartCanvasView;
import com.sjmeunier.arborfamiliae.data.FamilyIndividuals;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeChartFragment extends Fragment{

    private MainActivity mainActivity;
    private TreeChartCanvasView treeChartCanvas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tree, container, false);
        setHasOptionsMenu(false);
        mainActivity = (MainActivity)getActivity();

        treeChartCanvas = (TreeChartCanvasView) view.findViewById(R.id.treechart_canvas);
        configureChart();
        return view;
    }

    private void configureChart() {
        if (mainActivity.activeIndividual == null || mainActivity.activeTree == null)
            return;

        AppDatabase database = AppDatabase.getDatabase(mainActivity);
        int treeId = mainActivity.activeTree.id;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        int maxGeneration = Integer.parseInt(settings.getString("generations_preference", "4"));
        NameFormat nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        treeChartCanvas.configureChart(mainActivity.individualsInActiveTree.get(mainActivity.activeIndividual.individualId), mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, database, treeId, mainActivity, maxGeneration, nameFormat);
    }
}
