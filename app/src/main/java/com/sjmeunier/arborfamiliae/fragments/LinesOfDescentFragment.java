package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.charts.ChartSaveAsyncTask;
import com.sjmeunier.arborfamiliae.charts.LinesOfDescentCanvasView;
import com.sjmeunier.arborfamiliae.charts.RelationshipCanvasView;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LinesOfDescentFragment extends Fragment{

    private MainActivity mainActivity;

    private List<List<Integer>> lineages;
    private List<Integer> currentLineage;
    private int maxGenerations = 100;

    private LinesOfDescentCanvasView linesOfDescentCanvas;
    private NameFormat nameFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lines_of_descent, container, false);
        setHasOptionsMenu(false);
        mainActivity = (MainActivity)getActivity();

        linesOfDescentCanvas = (LinesOfDescentCanvasView) view.findViewById(R.id.lines_of_descent_canvas);

        LinesOfDescentChartLoader linesOfDescentChartLoader = new LinesOfDescentChartLoader(mainActivity);
        linesOfDescentChartLoader.execute();

        ImageView saveButton = (ImageView) view.findViewById(R.id.save_button);
        saveButton.setClickable(true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = linesOfDescentCanvas.renderBitmap();
                    ChartSaveAsyncTask chartSave = new ChartSaveAsyncTask(mainActivity);
                    chartSave.execute(bitmap);
                } catch (Exception e) {
                    Toast.makeText(mainActivity, mainActivity.getResources().getText(R.string.error_could_not_share_chart), Toast.LENGTH_SHORT);
                }

            }
        });

        Button prevButton = (Button) view.findViewById(R.id.button_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linesOfDescentCanvas.showPrevLineage();
            }
        });
        Button nextButton = (Button) view.findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linesOfDescentCanvas.showNextLineage();
            }
        });

        return view;
    }

    private void processNextGeneration(int generation, int targetIndividualId, int individualId) {
        Family family;
        Individual father;
        Individual mother;

         if (generation > maxGenerations)
             return;

        currentLineage.add(individualId);

        if (individualId == targetIndividualId) {
            lineages.add((ArrayList)((ArrayList)currentLineage).clone());
        } else {
            Individual individual = mainActivity.individualsInActiveTree.get(individualId);
            family = mainActivity.familiesInActiveTree.get(individual.parentFamilyId);

            if (family != null) {
                father = mainActivity.individualsInActiveTree.get(family.husbandId);
                if (father != null) {
                    processNextGeneration(generation + 1, targetIndividualId, father.individualId);

                }
                mother = mainActivity.individualsInActiveTree.get(family.wifeId);
                if (mother != null) {
                    processNextGeneration(generation, targetIndividualId, mother.individualId);
                }
            }

        }
        currentLineage.remove(currentLineage.size() - 1);
    }

    private class LinesOfDescentChartLoader extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;

        public LinesOfDescentChartLoader (Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            lineages = null;
            if (mainActivity.activeIndividual == null || mainActivity.activeTree == null || mainActivity.rootIndividualId <= 0)
                return false;

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];
           // maxGenerations = Integer.parseInt(settings.getString("lines_of_descent_generations_preference", "10"));
            lineages = new ArrayList<>();
            currentLineage = new ArrayList<>();

            if (mainActivity.rootIndividualId != mainActivity.activeIndividual.individualId) {
                processNextGeneration(1, mainActivity.activeIndividual.individualId, mainActivity.rootIndividualId);

                if (lineages.size() == 0) {
                    processNextGeneration(1, mainActivity.rootIndividualId, mainActivity.activeIndividual.individualId);
                }
            } else {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            linesOfDescentCanvas.configureChart(mainActivity.activeIndividual != null && mainActivity.rootIndividualId > 0 && mainActivity.rootIndividualId == mainActivity.activeIndividual.individualId, lineages, mainActivity, nameFormat);
            this.context = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context, R.style.MyProgressDialog);
            progressDialog.setTitle(context.getResources().getText(R.string.progress_calculatingrelationship));
            progressDialog.setMessage(context.getResources().getText(R.string.progress_pleasewait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
}
