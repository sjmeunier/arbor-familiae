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
import com.sjmeunier.arborfamiliae.reports.MtDNAReport;
import com.sjmeunier.arborfamiliae.reports.ReportTypes;
import com.sjmeunier.arborfamiliae.reports.AncestryDetailedReport;
import com.sjmeunier.arborfamiliae.reports.AncestrySummaryReport;
import com.sjmeunier.arborfamiliae.reports.BaseReport;
import com.sjmeunier.arborfamiliae.reports.DescendantReport;
import com.sjmeunier.arborfamiliae.reports.YDNAReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment{

    private MainActivity mainActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_reports, container, false);

        mainActivity = (MainActivity)getActivity();

        Spinner reportTypeSpinner = (Spinner) view.findViewById(R.id.report_type);
        List<String> list = new ArrayList<String>();
        list.add("Ancestry - Summary");
        list.add("Ancestry - Detailed");
        list.add("Descendant");
        list.add("Y-DNA");
        list.add("mt-DNA");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(dataAdapter);

        EditText maximumGenerations = view.findViewById(R.id.maximum_generations);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        maximumGenerations.setText(settings.getString("reports_maxgenerations", "10"));

        try {
            reportTypeSpinner.setSelection(settings.getInt("reports_type", 0));
        } catch (Exception e) {

        }

        reportTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView reportDescription = (TextView) mainActivity.findViewById(R.id.report_description);
                if (reportDescription == null)
                    return;
                if (adapterView.getSelectedItem().equals("Descendant"))
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_descendant));
                else if (adapterView.getSelectedItem().equals("Ancestry - Detailed"))
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ancestry_detailed));
                else if (adapterView.getSelectedItem().equals("Y-DNA"))
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ydna));
                else if (adapterView.getSelectedItem().equals("mt-DNA"))
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_mtdna));
                else
                    reportDescription.setText(mainActivity.getResources().getText(R.string.report_ancestry_summary));
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

                Spinner reportTypeSpinner = mainActivity.findViewById(R.id.report_type);
                if (reportTypeSpinner.getSelectedItem().equals("Descendant"))
                    reportType = ReportTypes.Decendant;
                else if (reportTypeSpinner.getSelectedItem().equals("Ancestry - Detailed"))
                    reportType = ReportTypes.AncestryDetailed;
                else if (reportTypeSpinner.getSelectedItem().equals("Y-DNA"))
                    reportType = ReportTypes.YDNA;
                else if (reportTypeSpinner.getSelectedItem().equals("mt-DNA"))
                    reportType = ReportTypes.MtDNA;

                EditText maximumGenerations = mainActivity.findViewById(R.id.maximum_generations);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("reports_maxgenerations", maximumGenerations.getText().toString());
                editor.putInt("reports_type", reportTypeSpinner.getSelectedItemPosition());
                editor.commit();

                try {
                    generateReportAsyncTask.execute(reportType.ordinal(), Integer.parseInt(maximumGenerations.getText().toString()));
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
            boolean success = false;
            try {
                BaseReport report = null;
                if (reportType == ReportTypes.Decendant)
                    report = new DescendantReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.AncestryDetailed)
                    report = new AncestryDetailedReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.YDNA)
                    report = new YDNAReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else if (reportType == ReportTypes.MtDNA)
                    report = new MtDNAReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);
                else
                    report = new AncestrySummaryReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);

                success = report.generateReport("report.txt", mainActivity.activeIndividual.individualId);
            } catch (Exception e) {
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressDialog.dismiss();
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
}
