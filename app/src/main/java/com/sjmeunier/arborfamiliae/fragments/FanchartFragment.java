package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.ChartSaveAsyncTask;
import com.sjmeunier.arborfamiliae.FanchartCanvasView;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        fanchartCanvas.configureChart(mainActivity.activeIndividual, mainActivity.individualsInActiveTree, mainActivity.familiesInActiveTree, mainActivity.database, mainActivity.activeTree.id, mainActivity, maxGeneration, nameFormat);
    }

    private class RelationshipChartSaver extends AsyncTask<Bitmap, Integer, File> {
        private Context context;
        private ProgressDialog progressDialog;

        public RelationshipChartSaver (Context context){
            this.context = context;
        }

        @Override
        protected File doInBackground(Bitmap... params) {
            File file = null;
            try {
                file = new File(context.getFilesDir(), "chart.png");
                if (file.exists()) {
                    file.delete();

                    file = new File(context.getFilesDir(), "chart.png");
                }

                FileOutputStream fout = new FileOutputStream(file);
                params[0].compress(Bitmap.CompressFormat.PNG, 100, fout);
                fout.flush();
                fout.close();
            } catch (IOException e) {
                Toast.makeText(context, "Unable to save image", Toast.LENGTH_SHORT);
                file = null;
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {

            progressDialog.dismiss();
            if (file != null) {
                Uri sharedFileUri = FileProvider.getUriForFile(context, "com.sjmeunier.arborfamiliae.chartfileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setDataAndType(sharedFileUri, "image/png");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, sharedFileUri);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                Intent chooser = Intent.createChooser(intent, context.getResources().getText(R.string.dialog_share_chart_with));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(chooser);
                }
            }
            this.context = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context, R.style.MyProgressDialog);
            progressDialog.setTitle(context.getResources().getText(R.string.progress_exporting));
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
