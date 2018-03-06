package com.sjmeunier.arborfamiliae.reports;

import android.content.Context;
import android.text.TextUtils;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;
import com.sjmeunier.arborfamiliae.util.AncestryUtil;
import com.sjmeunier.arborfamiliae.util.ListSearchUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurnameReport extends BaseReport {

    private Map<String, Integer> surnamesWithCount;
    private List<String> surnames;
    private List<Integer> ancestorIds;

    private IncludedIndividuals includedIndividuals;

    public SurnameReport(Context context, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, List<FamilyChild> familyChildrenInActiveTree, NameFormat nameFormat, IncludedIndividuals includedIndividuals, int maxGenerations, int treeId) {
        super(context, placesInActiveTree, individualsInActiveTree, familiesInActiveTree, familyChildrenInActiveTree, nameFormat, maxGenerations, treeId);
        this.includedIndividuals = includedIndividuals;
    }

    @Override
    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        this.configureOutputFile(filename);
        ancestorIds = new ArrayList<>();
        surnamesWithCount = new HashMap<>();
        surnames = new ArrayList<>();

        if (this.individualsInActiveTree.containsKey(activeIndividualId)) {
            Individual individual = this.individualsInActiveTree.get(activeIndividualId);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            if (includedIndividuals == IncludedIndividuals.Ancestors)
                this.writeLine("Surname report for the ancestors of " + AncestryUtil.generateName(individual, this.nameFormat));
            else if (includedIndividuals == IncludedIndividuals.Decendants)
                this.writeLine("Surname report for the descendants of " + AncestryUtil.generateName(individual, this.nameFormat));
            else
                this.writeLine("Surname report for all individuals in the tree");

            this.writeLine("Generated by Arbor Familiae at " + timeStamp);
            this.writeLine("");
            this.writeLine("");
        }

        if (includedIndividuals == IncludedIndividuals.Ancestors)
            this.processAncestorsIndividual(activeIndividualId, 1);
        else if (includedIndividuals == IncludedIndividuals.Decendants)
            this.processDescendantsIndividual(activeIndividualId, 1);
        else
            this.processAllIndividuals();

        this.outputReport();
        this.closeFile();

        return true;
    }

    private void outputReport() throws IOException {
        Collections.sort(surnames);
        for(String country : surnames) {
            this.writeLine(country + " (" + String.valueOf(surnamesWithCount.get(country)) + ")");
        }
    }

    private void getSurnameFromIndividual(Individual individual) {
        String surname = individual.surname.trim();

        if (TextUtils.isEmpty(surname)) {
            if (surnamesWithCount.containsKey("<Unknown>")) {
                surnamesWithCount.put("<Unknown>", surnamesWithCount.get("<Unknown>") + 1);
            } else {
                surnamesWithCount.put("<Unknown>", 1);
                surnames.add("<Unknown>");
            }
        } else {
            if (surnamesWithCount.containsKey(surname)) {
                surnamesWithCount.put(surname, surnamesWithCount.get(surname) + 1);
            } else {
                surnamesWithCount.put(surname, 1);
                surnames.add(surname);
            }
        }
    }

    private void processAncestorsIndividual(int individualId, int generation) {
        if (!this.individualsInActiveTree.containsKey(individualId))
            return;

        if (!ancestorIds.contains(individualId) && this.individualsInActiveTree.containsKey(individualId)) {
            ancestorIds.add(individualId);
            Individual individual = this.individualsInActiveTree.get(individualId);
            getSurnameFromIndividual(individual);

            if (generation < this.maxGenerations) {
                if (individual.parentFamilyId != -1 && this.familiesInActiveTree.containsKey(individual.parentFamilyId)) {
                    Family family = this.familiesInActiveTree.get(individual.parentFamilyId);
                    if (family.husbandId != -1) {
                        processAncestorsIndividual(family.husbandId, generation + 1);
                    }
                    if (family.wifeId != -1) {
                        processAncestorsIndividual(family.wifeId, generation + 1);
                    }
                }
            }
        } else {
            return;
        }

    }

    private void processDescendantsIndividual(int individualId, int generation) {
        if (!this.individualsInActiveTree.containsKey(individualId))
            return;

        if (!ancestorIds.contains(individualId) && this.individualsInActiveTree.containsKey(individualId)) {
            ancestorIds.add(individualId);
            Individual individual = this.individualsInActiveTree.get(individualId);
            getSurnameFromIndividual(individual);

            if (generation < this.maxGenerations) {
                List<Family> families = ListSearchUtils.findFamiliesForWifeOrHusband(individualId, familiesInActiveTree);
                for (Family family : families) {
                    List<FamilyChild> familyChildren = ListSearchUtils.findChildrenForFamily(family.familyId, this.familyChildrenInActiveTree);
                    for (FamilyChild familyChild : familyChildren) {
                        if (this.individualsInActiveTree.containsKey(familyChild.individualId)) {
                            processDescendantsIndividual(familyChild.individualId, generation + 1);
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    private void processAllIndividuals() {
        for(Individual individual : this.individualsInActiveTree.values()) {
            getSurnameFromIndividual(individual);
        }
    }
}
