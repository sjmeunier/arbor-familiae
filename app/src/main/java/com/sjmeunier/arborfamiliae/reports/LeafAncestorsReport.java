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

public class LeafAncestorsReport extends BaseReport {

    private Map<Long, Integer> ahnenNumbersAncestorId;
    private Map<Integer, Long> ancestorIdsAhnenNumbers;
    private List<Long> ahnenNumbers;

    public LeafAncestorsReport(Context context, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, List<FamilyChild> familyChildrenInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
        super(context, placesInActiveTree, individualsInActiveTree, familiesInActiveTree, familyChildrenInActiveTree, nameFormat, maxGenerations, treeId);
    }

    @Override
    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        this.configureOutputFile(filename);
        ahnenNumbers = new ArrayList<Long>();
        ahnenNumbersAncestorId = new HashMap<>();
        ancestorIdsAhnenNumbers = new HashMap<>();
        if (this.individualsInActiveTree.containsKey(activeIndividualId)) {
            Individual individual = this.individualsInActiveTree.get(activeIndividualId);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            this.writeLine("Leaf ancestor report of " + AncestryUtil.generateName(individual, this.nameFormat));
            this.writeLine("Generated by Arbor Familiae at " + timeStamp);
            this.writeLine("");
            this.writeLine("");
        }
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
            } else {
                if (ahnenNumber > 1)
                    this.writeLine("-------");
            }

            Individual individual = this.individualsInActiveTree.get(ahnenNumbersAncestorId.get(ahnenNumber));

            this.writeLine(String.valueOf(ahnenNumber) + ". " + AncestryUtil.generateName(individual, nameFormat));

            String birth = AncestryUtil.getBirthDateAndPlace(individual, this.placesInActiveTree);
            if (birth.length() > 3) {
                this.writeLine("Birth: " + birth);
            }

            String baptism = AncestryUtil.getBaptismDateAndPlace(individual, this.placesInActiveTree);
            if (baptism.length() > 3) {
                this.writeLine("Baptism: " + baptism);
            }

            String death = AncestryUtil.getDeathDateAndPlace(individual, this.placesInActiveTree);
            if (death.length() > 3) {
                this.writeLine("Death: " + death);
            }

            String burial = AncestryUtil.getBurialDateAndPlace(individual, this.placesInActiveTree);
            if (burial.length() > 3) {
                this.writeLine("Burial: " + burial);
            }


            if (!TextUtils.isEmpty(individual.occupation)) {
                this.writeLine("Occupation: " + individual.occupation);
            }

            //Family section
            this.writeLine("");

            //Parents
            boolean anyParent = false;
            if (individual.parentFamilyId != -1 && this.familiesInActiveTree.containsKey(individual.parentFamilyId)) {
                Family family = this.familiesInActiveTree.get(individual.parentFamilyId);
                if (family != null) {
                    Individual parent;

                    if (this.familiesInActiveTree.containsKey(family.husbandId)) {
                        parent = this.individualsInActiveTree.get(family.husbandId);
                        if (parent != null) {
                            this.writeLine("Father: " + AncestryUtil.generateName(parent, this.nameFormat));
                            anyParent = true;
                        }
                    }
                    if (this.familiesInActiveTree.containsKey(family.wifeId)) {
                        parent = this.individualsInActiveTree.get(family.wifeId);
                        if (parent != null) {
                            this.writeLine("Mother: " + AncestryUtil.generateName(parent, this.nameFormat));
                            anyParent = true;
                        }
                    }
                }
            }

            this.writeLine("");
        }
    }

    private void processIndividual(int individualId, int generation, long ahnenNumber) {
        if (!this.individualsInActiveTree.containsKey(individualId))
            return;

        Individual individual = this.individualsInActiveTree.get(individualId);

        if (!ancestorIdsAhnenNumbers.containsKey(individualId)) {
            ancestorIdsAhnenNumbers.put(individualId, ahnenNumber);
        } else {
            return;
        }

        if (individual.parentFamilyId == -1) {
            ahnenNumbersAncestorId.put(ahnenNumber, individualId);
            ahnenNumbers.add(ahnenNumber);
        }
        if (individual.parentFamilyId != -1 && this.familiesInActiveTree.containsKey(individual.parentFamilyId)) {
            Family family = this.familiesInActiveTree.get(individual.parentFamilyId);

            if (family.husbandId == -1 || family.wifeId == -1) {
                ahnenNumbersAncestorId.put(ahnenNumber, individualId);
                ahnenNumbers.add(ahnenNumber);
            }
            if (generation < this.maxGenerations) {
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
