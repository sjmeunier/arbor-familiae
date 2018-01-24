package com.sjmeunier.arborfamiliae.gedcom;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.database.AppDatabase;

import java.io.IOException;

public class GedcomImportService extends IntentService {

    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

    public GedcomImportService() {
        super("GedcomImportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        Uri gedcomResource = Uri.parse(intent.getStringExtra(PARAM_IN_MSG));

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        String message = "";
        int treeId = 0;
        try {
            GedcomParser parser = new GedcomParser(database, getContentResolver());
            treeId = parser.parseGedcom(getBaseContext(), gedcomResource);
            message = "Finished importing tree";
            Log.d("ARBORFAMILIAE-LOG", "Finished tree");
        } catch (IOException ex) {
            message = "Unable to import tree";
            Log.d("ARBORFAMILIAE-LOG", ex.getMessage());
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.GedcomRequestReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, message);
        sendBroadcast(broadcastIntent);

    }
}
