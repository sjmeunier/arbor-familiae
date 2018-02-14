package com.sjmeunier.arborfamiliae.reports;

import android.content.Context;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

public class BaseReport {
    protected Context context;
    protected AppDatabase database;
    protected Map<Integer, Place> placesInActiveTree;
    protected Map<Integer, Individual> individualsInActiveTree;
    protected Map<Integer, Family> familiesInActiveTree;
    protected NameFormat nameFormat;
    protected int maxGenerations;
    protected int treeId;
    protected OutputStreamWriter writer;
    protected FileOutputStream fout;
    protected File file;

    public BaseReport(Context context, AppDatabase database, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
        this.context = context;
        this.database = database;
        this.placesInActiveTree = placesInActiveTree;
        this.individualsInActiveTree = individualsInActiveTree;
        this.familiesInActiveTree = familiesInActiveTree;
        this.nameFormat = nameFormat;
        this.maxGenerations = maxGenerations;
        this.treeId = treeId;
        this.writer = null;
        this.fout = null;
    }

    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        return true;
    }

    protected void configureOutputFile(String filename) throws IOException {
        this.file = new File(context.getFilesDir(), filename);
        this.fout = new FileOutputStream(this.file);
        writer = new OutputStreamWriter(this.fout);
    }

    protected void writeLine(String line) throws IOException {
        writer.write(line + "\r\n");
    }

    protected void closeFile() throws IOException {
        if (this.writer != null) {
            this.writer.flush();
            this.writer.close();
        }
        if (fout != null) {
            this.fout.flush();
            this.fout.close();
        }

    }
}
