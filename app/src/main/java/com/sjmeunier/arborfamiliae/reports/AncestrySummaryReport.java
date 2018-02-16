package com.sjmeunier.arborfamiliae.reports;

import android.content.Context;
import android.text.TextUtils;

import com.sjmeunier.arborfamiliae.AncestryUtil;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AncestrySummaryReport extends BaseReport {

    private Map<Long, Integer> ahnenNumbersAncestorId;
    private Map<Integer, Long> ancestorIdsAhnenNumbers;
    private List<Long> ahnenNumbers;

    public AncestrySummaryReport(Context context, AppDatabase database, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
        super(context, database, placesInActiveTree, individualsInActiveTree, familiesInActiveTree, nameFormat, maxGenerations, treeId);
    }

    @Override
    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        this.configureOutputFile(filename);
        ahnenNumbers = new ArrayList<Long>();
        ahnenNumbersAncestorId = new HashMap<>();
        ancestorIdsAhnenNumbers = new HashMap<>();

        this.processIndividual(activeIndividualId, 1, 1);
        this.outputReport();
        this.closeFile();

        return true;
    }

    private void outputReport() throws IOException {
        int currentGeneration = -1;

        Collections.sort(ahnenNumbers);
        for(Long ahnenNumber : ahnenNumbers) {
            int individualGeneration = AncestryUtil.getGenerationNumberFromAhnenNumber(ahnenNumber) + 1;
            if (individualGeneration > currentGeneration) {
                currentGeneration = individualGeneration;
                this.writeLine("Generation " + String.valueOf(currentGeneration));
                this.writeLine("----------------------------");
                this.writeLine("");
            }
            Individual individual = this.individualsInActiveTree.get(ahnenNumbersAncestorId.get(ahnenNumber));

            this.writeLine(String.valueOf(ahnenNumber) + ". " + AncestryUtil.generateName(individual, nameFormat) + " " + AncestryUtil.generateBirthDeathDateWithPlace(individual, this.placesInActiveTree));

            this.writeLine("");
        }
    }

    private void processIndividual(int individualId, int generation, long ahnenNumber) {
        if (!this.individualsInActiveTree.containsKey(individualId))
            return;

        Individual individual = this.individualsInActiveTree.get(individualId);

        if (!ancestorIdsAhnenNumbers.containsKey(individualId)) {
            ancestorIdsAhnenNumbers.put(individualId, ahnenNumber);
            ahnenNumbersAncestorId.put(ahnenNumber, individualId);
            ahnenNumbers.add(ahnenNumber);
        }

        if (generation < this.maxGenerations) {
            if (individual.parentFamilyId != -1 && this.familiesInActiveTree.containsKey(individual.parentFamilyId)) {
                Family family = this.familiesInActiveTree.get(individual.parentFamilyId);
                if (family.husbandId != -1) {
                    processIndividual(family.husbandId, generation + 1, ahnenNumber * 2);
                }
                if (family.wifeId != -1) {
                    processIndividual(family.wifeId, generation + 1, ahnenNumber * 2 + 1);
                }
            }
        }

    }
}
