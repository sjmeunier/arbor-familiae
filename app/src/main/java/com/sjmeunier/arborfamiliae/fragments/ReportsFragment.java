package com.sjmeunier.arborfamiliae.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.sjmeunier.arborfamiliae.data.ReportTypes;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Tree;
import com.sjmeunier.arborfamiliae.gedcom.GedcomParser;

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

                generateReportAsyncTask.execute(reportType.ordinal(), Integer.parseInt(maximumGenerations.getText().toString()));
           }
        });
        setHasOptionsMenu(false);

        return view;
    }

    private class GenerateReportAsyncTask extends AsyncTask<Integer, Integer, File> {
        private Context context;
        private ProgressDialog progressDialog;

        public GenerateReportAsyncTask (Context context){
            this.context = context;
        }

        @Override
        protected File doInBackground(Integer... params) {
            Integer reportType = params[0];
            Integer maximumGenerations = params[0];

            File file  = new File("");
            return file;
        }

        @Override
        protected void onPostExecute(File result) {

            progressDialog.dismiss();
            this.context = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context, R.style.MyProgressDialog);
            progressDialog.setTitle(context.getResources().getText(R.string.progress_generating_report));
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
