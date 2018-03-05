package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.charts.ChartSaveAsyncTask;
import com.sjmeunier.arborfamiliae.charts.FanchartCanvasView;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.Map;

public class FanchartFragment extends Fragment{

    private MainActivity mainActivity;

    private int maxGeneration = 0;

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

        ImageView saveButton = (ImageView) view.findViewById(R.id.save_button);
        saveButton.setClickable(true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = fanchartCanvas.renderBitmap();
                    ChartSaveAsyncTask chartSave = new ChartSaveAsyncTask(mainActivity);
                    chartSave.execute(bitmap);
                } catch (Exception e) {
                   Toast.makeText(mainActivity, mainActivity.getResources().getText(R.string.error_could_not_share_chart), Toast.LENGTH_SHORT);
                }

            }
        });

        drawChart();
        return view;
    }

    private void drawChart() {
        if (mainActivity.activeIndividual == null || mainActivity.activeTree == null)
            return;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        maxGeneration = Integer.parseInt(settings.getString("fanchart_generations_preference", "4"));
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        fanchartCanvas.configureChart(mainActivity.activeIndividual, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity, maxGeneration, nameFormat);
    }
}
