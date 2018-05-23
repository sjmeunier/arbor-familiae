package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.reports.CountriesReport;
import com.sjmeunier.arborfamiliae.reports.IncludedIndividuals;
import com.sjmeunier.arborfamiliae.reports.LeafAncestorsReport;
import com.sjmeunier.arborfamiliae.reports.LifespanReport;
import com.sjmeunier.arborfamiliae.reports.MtDNAReport;
import com.sjmeunier.arborfamiliae.reports.PlacesReport;
import com.sjmeunier.arborfamiliae.reports.ReportTypes;
import com.sjmeunier.arborfamiliae.reports.AncestryDetailedReport;
import com.sjmeunier.arborfamiliae.reports.AncestrySummaryReport;
import com.sjmeunier.arborfamiliae.reports.BaseReport;
import com.sjmeunier.arborfamiliae.reports.DescendantReport;
import com.sjmeunier.arborfamiliae.reports.SurnameReport;
import com.sjmeunier.arborfamiliae.reports.YDNAReport;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReportsFragment extends Fragment{

    private MainActivity mainActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_reports, container, false);

        mainActivity = (MainActivity)getActivity();

        Spinner reportTypeSpinner = (Spinner) view.findViewById(R.id.report_type);
        List<String> typeList = new ArrayList<String>();
        typeList.add("Ancestry - Summary");
        typeList.add("Ancestry - Detailed");
        typeList.add("Leaf Ancestors");
        typeList.add("Descendant");
        typeList.add("Y-DNA");
        typeList.add("mt-DNA");
        typeList.add("Lifespan");
        typeList.add("Places");
        typeList.add("Countries");
        typeList.add("Surname");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, typeList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(dataAdapter);

        Spinner includedIndividualsSpinner = (Spinner) view.findViewById(R.id.included_individuals);
        List<String> includedIndividualsList = new ArrayList<String>();
        includedIndividualsList.add("Ancestors");
        includedIndividualsList.add("Descendants");
        includedIndividualsList.add("Whole Tree");

        final ArrayAdapter<String> includedIndividualDataAdapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, includedIndividualsList);
        includedIndividualDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        includedIndividualsSpinner.setAdapter(includedIndividualDataAdapter);

        Spinner maximumGenerationsSpinner = (Spinner) view.findViewById(R.id.maximum_generations);
        List<String> maximumGenerationsList = new ArrayList<String>();
        maximumGenerationsList.add("5");
        maximumGenerationsList.add("10");
        maximumGenerationsList.add("15");
        maximumGenerationsList.add("20");
        maximumGenerationsList.add("All");

        final ArrayAdapter<String> maximumGenerationsDataAdapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, maximumGenerationsList);
        maximumGenerationsDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maximumGenerationsSpinner.setAdapter(maximumGenerationsDataAdapter);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);

        TextView reportOutput = view.findViewById(R.id.report_output);
        reportOutput.setVisibility(View.GONE);
        try {
            maximumGenerationsSpinner.setSelection(settings.getInt("reports_maximum_generations", 1));
        } catch (Exception e) {

        }

        try {
            reportTypeSpinner.setSelection(settings.getInt("reports_type", 0));
        } catch (Exception e) {

        }

        try {
            includedIndividualsSpinner.setSelection(settings.getInt("included_individuals", 0));
        } catch (Exception e) {

        }

        reportTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView reportDescription = (TextView) mainActivity.findViewById(R.id.report_description);
                if (reportDescription == null)
                    return;

                Spinner includedIndividualsSpinner = (Spinner) mainActivity.findViewById(R.id.included_individuals);
                TextView includedIndividualsLabel = (TextView) mainActivity.findViewById(R.id.label_included_individuals);

                if (adapterView.getSelectedItem().equals("Descendant")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_descendant));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                } else if (adapterView.getSelectedItem().equals("Ancestry - Detailed")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ancestry_detailed));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                } else if (adapterView.getSelectedItem().equals("Leaf Ancestors")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_leaf_ancestors));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                } else if (adapterView.getSelectedItem().equals("Y-DNA")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ydna));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                } else if (adapterView.getSelectedItem().equals("mt-DNA")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_mtdna));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                } else if (adapterView.getSelectedItem().equals("Lifespan")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_lifespan));
                    includedIndividualsSpinner.setVisibility(View.VISIBLE);
                    includedIndividualsLabel.setVisibility(View.VISIBLE);
                } else if (adapterView.getSelectedItem().equals("Places")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_places));
                    includedIndividualsSpinner.setVisibility(View.VISIBLE);
                    includedIndividualsLabel.setVisibility(View.VISIBLE);
                } else if (adapterView.getSelectedItem().equals("Countries")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_countries));
                    includedIndividualsSpinner.setVisibility(View.VISIBLE);
                    includedIndividualsLabel.setVisibility(View.VISIBLE);
                } else if (adapterView.getSelectedItem().equals("Surname")) {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_surname));
                    includedIndividualsSpinner.setVisibility(View.VISIBLE);
                    includedIndividualsLabel.setVisibility(View.VISIBLE);
                } else {
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ancestry_summary));
                    includedIndividualsSpinner.setVisibility(View.GONE);
                    includedIndividualsLabel.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        final Button generateButton = (Button) view.findViewById(R.id.button_generate_report);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateReportAsyncTask generateReportAsyncTask = new GenerateReportAsyncTask(mainActivity);
                ReportTypes reportType = ReportTypes.AncestrySummary;
                IncludedIndividuals includedIndividuals = IncludedIndividuals.WholeTree;

                Spinner reportTypeSpinner = mainActivity.findViewById(R.id.report_type);
                if (reportTypeSpinner.getSelectedItem().equals("Descendant"))
                    reportType = ReportTypes.Decendant;
                else if (reportTypeSpinner.getSelectedItem().equals("Ancestry - Detailed"))
                    reportType = ReportTypes.AncestryDetailed;
                else if (reportTypeSpinner.getSelectedItem().equals("Leaf Ancestors"))
                    reportType = ReportTypes.LeafAncestors;
                else if (reportTypeSpinner.getSelectedItem().equals("Y-DNA"))
                    reportType = ReportTypes.YDNA;
                else if (reportTypeSpinner.getSelectedItem().equals("mt-DNA"))
                    reportType = ReportTypes.MtDNA;
                else if (reportTypeSpinner.getSelectedItem().equals("Lifespan"))
                    reportType = ReportTypes.Lifespan;
                else if (reportTypeSpinner.getSelectedItem().equals("Places"))
                    reportType = ReportTypes.Places;
                else if (reportTypeSpinner.getSelectedItem().equals("Countries"))
                    reportType = ReportTypes.Countries;
                else if (reportTypeSpinner.getSelectedItem().equals("Surname"))
                    reportType = ReportTypes.Surname;

                Spinner includedIndividualsSpinner = mainActivity.findViewById(R.id.included_individuals);
                if (includedIndividualsSpinner != null) {
                    if (includedIndividualsSpinner.getSelectedItem().equals("Ancestors"))
                        includedIndividuals = IncludedIndividuals.Ancestors;
                    else if (includedIndividualsSpinner.getSelectedItem().equals("Descendants"))
                        includedIndividuals = IncludedIndividuals.Decendants;
                }

                Spinner maximumGenerationsSpinner = (Spinner) mainActivity.findViewById(R.id.maximum_generations);

                int maximumGenerations = 0;
                if (maximumGenerationsSpinner.getSelectedItem().equals("All")) {
                    maximumGenerations = 999;
                } else {
                    try {
                        maximumGenerations = Integer.parseInt(maximumGenerationsSpinner.getSelectedItem().toString());
                    } catch (Exception e) {
                        maximumGenerations = 10;
                    }
                }

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("reports_type", reportTypeSpinner.getSelectedItemPosition());
                editor.putInt("reports_maximum_generations", maximumGenerationsSpinner.getSelectedItemPosition());
                editor.putInt("included_individuals", includedIndividualsSpinner.getSelectedItemPosition());
                editor.commit();

                TextView reportOutput = mainActivity.findViewById(R.id.report_output);
                reportOutput.setVisibility(View.GONE);

                try {
                    generateReportAsyncTask.execute(reportType.ordinal(), maximumGenerations, includedIndividuals.ordinal());
                } catch(Exception e) {
                    Toast.makeText(mainActivity, "Unable to parse maximum generations", Toast.LENGTH_SHORT);
                }
           }
        });

        final Button displayButton = (Button) view.findViewById(R.id.button_display_report);
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayReportAsyncTask displayReportAsyncTask = new DisplayReportAsyncTask(mainActivity);
                ReportTypes reportType = ReportTypes.AncestrySummary;
                IncludedIndividuals includedIndividuals = IncludedIndividuals.WholeTree;

                Spinner reportTypeSpinner = mainActivity.findViewById(R.id.report_type);
                if (reportTypeSpinner.getSelectedItem().equals("Descendant"))
                    reportType = ReportTypes.Decendant;
                else if (reportTypeSpinner.getSelectedItem().equals("Ancestry - Detailed"))
                    reportType = ReportTypes.AncestryDetailed;
                else if (reportTypeSpinner.getSelectedItem().equals("Leaf Ancestors"))
                    reportType = ReportTypes.LeafAncestors;
                else if (reportTypeSpinner.getSelectedItem().equals("Y-DNA"))
                    reportType = ReportTypes.YDNA;
                else if (reportTypeSpinner.getSelectedItem().equals("mt-DNA"))
                    reportType = ReportTypes.MtDNA;
                else if (reportTypeSpinner.getSelectedItem().equals("Lifespan"))
                    reportType = ReportTypes.Lifespan;
                else if (reportTypeSpinner.getSelectedItem().equals("Places"))
                    reportType = ReportTypes.Places;
                else if (reportTypeSpinner.getSelectedItem().equals("Countries"))
                    reportType = ReportTypes.Countries;
                else if (reportTypeSpinner.getSelectedItem().equals("Surname"))
                    reportType = ReportTypes.Surname;

                Spinner includedIndividualsSpinner = mainActivity.findViewById(R.id.included_individuals);
                if (includedIndividualsSpinner != null) {
                    if (includedIndividualsSpinner.getSelectedItem().equals("Ancestors"))
                        includedIndividuals = IncludedIndividuals.Ancestors;
                    else if (includedIndividualsSpinner.getSelectedItem().equals("Descendants"))
                        includedIndividuals = IncludedIndividuals.Decendants;
                }

                Spinner maximumGenerationsSpinner = (Spinner) mainActivity.findViewById(R.id.maximum_generations);

                int maximumGenerations = 0;
                if (maximumGenerationsSpinner.getSelectedItem().equals("All")) {
                    maximumGenerations = 999;
                } else {
                    try {
                        maximumGenerations = Integer.parseInt(maximumGenerationsSpinner.getSelectedItem().toString());
                    } catch (Exception e) {
                        maximumGenerations = 10;
                    }
                }

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("reports_type", reportTypeSpinner.getSelectedItemPosition());
                editor.putInt("reports_maximum_generations", maximumGenerationsSpinner.getSelectedItemPosition());
                editor.putInt("included_individuals", includedIndividualsSpinner.getSelectedItemPosition());
                editor.commit();

                TextView reportOutput = mainActivity.findViewById(R.id.report_output);
                reportOutput.setVisibility(View.GONE);

                try {
                    displayReportAsyncTask.execute(reportType.ordinal(), maximumGenerations, includedIndividuals.ordinal());
                } catch(Exception e) {
                    Toast.makeText(mainActivity, "Unable to parse maximum generations", Toast.LENGTH_SHORT);
                }
            }
        });

        setHasOptionsMenu(false);

        return view;
    }

    private class GenerateReportAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
        private MainActivity activity;
        private ProgressDialog progressDialog;

        public GenerateReportAsyncTask (MainActivity activity){
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            ReportTypes reportType = ReportTypes.values()[params[0]];
            Integer maximumGenerations = params[1];
            IncludedIndividuals includedIndividuals = IncludedIndividuals.values()[params[2]];
            boolean success = false;
            try {
                BaseReport report = null;
                if (reportType == ReportTypes.Decendant)
                    report = new DescendantReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.AncestryDetailed)
                    report = new AncestryDetailedReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.LeafAncestors)
                    report = new LeafAncestorsReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.YDNA)
                    report = new YDNAReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.MtDNA)
                    report = new MtDNAReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Lifespan)
                    report = new LifespanReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Places)
                    report = new PlacesReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Countries)
                    report = new CountriesReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Surname)
                    report = new SurnameReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else
                    report = new AncestrySummaryReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);

                success = report.generateReport("report.txt", mainActivity.activeIndividual.individualId);
            } catch (Exception e) {
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result) {
                File file = new File(activity.getFilesDir(), "report.txt");
                Uri sharedFileUri = FileProvider.getUriForFile(activity, "com.sjmeunier.arborfamiliae.chartfileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, sharedFileUri);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                Intent chooser = Intent.createChooser(intent, activity.getResources().getText(R.string.dialog_share_report_with));
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(chooser);
                }
            } else {
                Toast.makeText(activity, "Unable to generate report", Toast.LENGTH_SHORT);
            }
            this.activity = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            progressDialog.setTitle(activity.getResources().getText(R.string.progress_generating_report));
            progressDialog.setMessage(activity.getResources().getText(R.string.progress_pleasewait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private class DisplayReportAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
        private MainActivity activity;
        private ProgressDialog progressDialog;

        public DisplayReportAsyncTask (MainActivity activity){
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            ReportTypes reportType = ReportTypes.values()[params[0]];
            Integer maximumGenerations = params[1];
            IncludedIndividuals includedIndividuals = IncludedIndividuals.values()[params[2]];
            boolean success = false;
            try {
                BaseReport report = null;
                if (reportType == ReportTypes.Decendant)
                    report = new DescendantReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.AncestryDetailed)
                    report = new AncestryDetailedReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.LeafAncestors)
                    report = new LeafAncestorsReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.YDNA)
                    report = new YDNAReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.MtDNA)
                    report = new MtDNAReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Lifespan)
                    report = new LifespanReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Places)
                    report = new PlacesReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Countries)
                    report = new CountriesReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.Surname)
                    report = new SurnameReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, includedIndividuals, maximumGenerations, mainActivity.activeTree.id);
                else
                    report = new AncestrySummaryReport(mainActivity, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.familyChildrenInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);

                success = report.generateReport("report.txt", mainActivity.activeIndividual.individualId);
            } catch (Exception e) {
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result) {
                TextView reportOutput = mainActivity.findViewById(R.id.report_output);
                reportOutput.setVisibility(View.VISIBLE);
                reportOutput.setText("");

                File file = new File(activity.getFilesDir(), "report.txt");

                if (!file.exists()){
                    Toast.makeText(activity, "Unable to generate report", Toast.LENGTH_SHORT);
                    return;
                }

                try {
                    reportOutput.setText(new Scanner(file).useDelimiter("\\Z").next());
                }
                catch (IOException e) {
                    Toast.makeText(activity, "Unable to generate report", Toast.LENGTH_SHORT);
                }
            } else {
                Toast.makeText(activity, "Unable to generate report", Toast.LENGTH_SHORT);
            }
            this.activity = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(activity, R.style.MyProgressDialog);
            progressDialog.setTitle(activity.getResources().getText(R.string.progress_generating_report));
            progressDialog.setMessage(activity.getResources().getText(R.string.progress_pleasewait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
}
