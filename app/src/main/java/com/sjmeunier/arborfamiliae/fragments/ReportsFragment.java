package com.sjmeunier.arborfamiliae.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.OnTreeListViewClickListener;
import com.sjmeunier.arborfamiliae.OnTreeListViewDeleteListener;
import com.sjmeunier.arborfamiliae.OnTreeListViewLongPressListener;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.TreeListAdapter;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.data.ReportTypes;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Tree;
import com.sjmeunier.arborfamiliae.gedcom.GedcomParser;
import com.sjmeunier.arborfamiliae.reports.AncestryReport;
import com.sjmeunier.arborfamiliae.reports.BaseReport;
import com.sjmeunier.arborfamiliae.reports.DescendantReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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
        list.add("Ancestry");
        list.add("Descendant");
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

        final Button generateButton = (Button) view.findViewById(R.id.button_generate_report);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GenerateReportAsyncTask generateReportAsyncTask = new GenerateReportAsyncTask(mainActivity);
                ReportTypes reportType = ReportTypes.Ancestry;

                Spinner reportTypeSpinner = mainActivity.findViewById(R.id.report_type);
                if (reportTypeSpinner.getSelectedItem().equals("Descendant"))
                    reportType = ReportTypes.Decendant;

                EditText maximumGenerations = mainActivity.findViewById(R.id.maximum_generations);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("reports_maxgenerations", maximumGenerations.getText().toString());
                editor.putInt("reports_type", reportTypeSpinner.getSelectedItemPosition());
                editor.commit();

                generateReportAsyncTask.execute(reportType.ordinal(), Integer.parseInt(maximumGenerations.getText().toString()));
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
                else
                    report = new AncestryReport(mainActivity, mainActivity.database, mainActivity.placesInActiveTree, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.nameFormat, maximumGenerations, mainActivity.activeTree.id);

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
                intent.setDataAndType(sharedFileUri, "text/plain");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, sharedFileUri);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{" "});
                Intent chooser = Intent.createChooser(intent, activity.getResources().getText(R.string.dialog_share_chart_with));
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
