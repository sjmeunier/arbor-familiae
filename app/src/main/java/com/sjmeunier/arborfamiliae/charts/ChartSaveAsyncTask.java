package com.sjmeunier.arborfamiliae.charts;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ChartSaveAsyncTask extends AsyncTask<Bitmap, Integer, File> {
    private Context context;
    private ProgressDialog progressDialog;

    public ChartSaveAsyncTask (Context context){
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
            intent.setType("image/png");
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