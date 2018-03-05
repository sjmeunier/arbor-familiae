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
import android.widget.ImageView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.charts.ChartSaveAsyncTask;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.charts.RelationshipCanvasView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RelationshipFragment extends Fragment{

    private MainActivity mainActivity;

    private int maxGeneration = 0;

    private Map<Long, Individual> rootTree;
    private Map<Long, Individual> targetTree;

    private List<Integer> rootIndividualIds;
    private List<Integer> targetIndividualIds;

    private List<Individual> rootLineage;
    private List<Individual> targetLineage;
    private Individual ancestorSpouse;

    private RelationshipCanvasView relationshipCanvas;
    private NameFormat nameFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_relationship, container, false);
        setHasOptionsMenu(false);
        mainActivity = (MainActivity)getActivity();

        relationshipCanvas = (RelationshipCanvasView) view.findViewById(R.id.relationship_canvas);

        RelationshipChartLoader relationshipChartLoader = new RelationshipChartLoader(mainActivity);
        relationshipChartLoader.execute();

        ImageView saveButton = (ImageView) view.findViewById(R.id.save_button);
        saveButton.setClickable(true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap bitmap = relationshipCanvas.renderBitmap();
                    ChartSaveAsyncTask chartSave = new ChartSaveAsyncTask(mainActivity);
                    chartSave.execute(bitmap);
                } catch (Exception e) {
                    Toast.makeText(mainActivity, mainActivity.getResources().getText(R.string.error_could_not_share_chart), Toast.LENGTH_SHORT);
                }

            }
        });
        return view;
    }

    private void processNextGeneration(int generation, boolean keepProcessingRoot, boolean keepProcessingTarget, List<Long> previouslyAddedKeysRoot, List<Long> previouslyAddedKeysTarget) {
        Family family;
        Individual father;
        Individual mother;
        boolean rootHasMoreAncestors = false;
        boolean targetHasMoreAncestors = false;
        boolean matchFound = false;

        int matchId = 0;

        List<Long> currentlyAddedKeysRoot = new ArrayList<>();
        List<Long> currentlyAddedKeysTarget = new ArrayList<>();

        //Add next generation for root tree
        if (keepProcessingRoot) {
            for (long key : previouslyAddedKeysRoot) {
                family = mainActivity.familiesInActiveTree.get(rootTree.get(key).parentFamilyId);
                if (family != null) {
                    if (!rootIndividualIds.contains(family.husbandId)) {
                        father = mainActivity.individualsInActiveTree.get(family.husbandId);
                        if (father != null) {
                            rootTree.put(key * 2, mainActivity.individualsInActiveTree.get(family.husbandId));
                            currentlyAddedKeysRoot.add(key * 2);
                            rootIndividualIds.add(family.husbandId);

                            if (!matchFound && targetIndividualIds.contains(family.husbandId)) {
                                matchFound = true;
                                matchId = family.husbandId;
                            }
                            rootHasMoreAncestors = true;
                        }
                    }

                    if (!rootIndividualIds.contains(family.wifeId)) {
                        mother = mainActivity.individualsInActiveTree.get(family.wifeId);
                        if (mother != null) {
                            rootTree.put(key * 2 + 1, mainActivity.individualsInActiveTree.get(family.wifeId));
                            currentlyAddedKeysRoot.add(key * 2 + 1);
                            rootIndividualIds.add(family.wifeId);

                            if (!matchFound && targetIndividualIds.contains(family.wifeId)) {
                                matchFound = true;
                                matchId = family.wifeId;
                            }

                            rootHasMoreAncestors = true;
                        }
                    }
                }
            }
        }

        //Add next generation for target tree
        if (keepProcessingTarget) {
            for (long key : previouslyAddedKeysTarget) {
                family = mainActivity.familiesInActiveTree.get(targetTree.get(key).parentFamilyId);
                if (family != null) {
                    if (!targetIndividualIds.contains(family.husbandId)) {
                        father = mainActivity.individualsInActiveTree.get(family.husbandId);
                        if (father != null) {
                            targetTree.put(key * 2, mainActivity.individualsInActiveTree.get(family.husbandId));
                            currentlyAddedKeysTarget.add(key * 2);
                            targetIndividualIds.add(family.husbandId);

                            if (!matchFound && rootIndividualIds.contains(family.husbandId)){
                                matchFound = true;
                                matchId = family.husbandId;
                            }

                            targetHasMoreAncestors = true;
                        }
                    }

                    if (!targetIndividualIds.contains(family.wifeId)) {
                        mother = mainActivity.individualsInActiveTree.get(family.wifeId);
                        if (mother != null) {
                            targetTree.put(key * 2 + 1, mainActivity.individualsInActiveTree.get(family.wifeId));
                            currentlyAddedKeysTarget.add(key * 2 + 1);
                            targetIndividualIds.add(family.wifeId);

                            if (!matchFound && rootIndividualIds.contains(family.wifeId)){
                                matchFound = true;
                                matchId = family.wifeId;
                            }
                            targetHasMoreAncestors = true;
                        }
                    }
                }
            }
        }

        if (matchFound) {
            rootLineage = new ArrayList<>();
            targetLineage = new ArrayList<>();

            //Create root lineage
            long key = 0;
            for(long treeKey : rootTree.keySet()) {
                if (rootTree.get(treeKey).individualId == matchId) {
                    key = treeKey;
                }
            }

            while (key > 0) {
                rootLineage.add(rootTree.get(key));
                key = key / 2;
            }

            //Create target lineage
            key = 0;
            for(long treeKey : targetTree.keySet()) {
                if (targetTree.get(treeKey).individualId == matchId) {
                    key = treeKey;
                }
            }

            while (key > 0) {
                targetLineage.add(targetTree.get(key));
                key = key / 2;
            }

            if (rootLineage.size() > 1 || targetLineage.size() > 1) {
                Family spouseFamily;

                if (rootLineage.size() == 1) {
                    //We only have a target lineage so use that family
                    spouseFamily = mainActivity.familiesInActiveTree.get(targetLineage.get(1).parentFamilyId);
                    if (targetLineage.get(0).gender == GenderEnum.Male)
                        ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.wifeId);
                    else
                        ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.husbandId);
                } else if (targetLineage.size() == 1) {
                    //We only have a root lineage so use that family
                    spouseFamily = mainActivity.familiesInActiveTree.get(rootLineage.get(1).parentFamilyId);
                    if (rootLineage.get(0).gender == GenderEnum.Male)
                        ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.wifeId);
                    else
                        ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.husbandId);
                } else {
                    if (rootLineage.get(1).parentFamilyId == targetLineage.get(1).parentFamilyId) {
                        //Only get spouse if both branches descend from same marriage
                        spouseFamily = mainActivity.familiesInActiveTree.get(rootLineage.get(1).parentFamilyId);
                        if (rootLineage.get(0).gender == GenderEnum.Male)
                            ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.wifeId);
                        else
                            ancestorSpouse = mainActivity.individualsInActiveTree.get(spouseFamily.husbandId);
                    }
                }
            }

        }

        if ((targetHasMoreAncestors || rootHasMoreAncestors) && !matchFound)
            processNextGeneration(generation + 1, rootHasMoreAncestors, targetHasMoreAncestors, currentlyAddedKeysRoot, currentlyAddedKeysTarget);
    }

    private class RelationshipChartLoader extends AsyncTask<Void, Integer, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;

        public RelationshipChartLoader (Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (mainActivity.activeIndividual == null || mainActivity.activeTree == null)
                return false;

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
            nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

            rootTree = new HashMap<>();
            targetTree = new HashMap<>();
            rootIndividualIds = new ArrayList<>();
            targetIndividualIds = new ArrayList<>();
            rootLineage = new ArrayList<>();
            targetLineage = new ArrayList<>();
            ancestorSpouse = null;


            if (mainActivity.rootIndividualId != 0 && mainActivity.activeIndividual != null) {
                rootTree.put((long)1, mainActivity.individualsInActiveTree.get(mainActivity.rootIndividualId));
                rootIndividualIds.add(mainActivity.rootIndividualId);
                targetTree.put((long)1, mainActivity.activeIndividual);
                targetIndividualIds.add(mainActivity.activeIndividual.individualId);
                rootLineage.add(mainActivity.individualsInActiveTree.get(mainActivity.rootIndividualId));
                targetLineage.add(mainActivity.activeIndividual);

                List<Long> currentRootKeys = new ArrayList<>();
                List<Long> currentTargetKeys = new ArrayList<>();
                currentRootKeys.add((long)1);
                currentTargetKeys.add((long)1);

                if (mainActivity.rootIndividualId != mainActivity.activeIndividual.individualId) {
                    processNextGeneration(1, true, true, currentRootKeys, currentTargetKeys);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressDialog.dismiss();

            relationshipCanvas.configureChart(rootLineage, targetLineage, ancestorSpouse, nameFormat);
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
