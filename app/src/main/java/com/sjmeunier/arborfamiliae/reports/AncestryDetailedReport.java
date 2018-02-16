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
import com.sjmeunier.arborfamiliae.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AncestryDetailedReport extends BaseReport {

    private Map<Long, Integer> ahnenNumbersAncestorId;
    private Map<Integer, Long> ancestorIdsAhnenNumbers;
    private List<Long> ahnenNumbers;

    public AncestryDetailedReport(Context context, AppDatabase database, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
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
            List<FamilyChild> parentFamilies =  this.database.familyChildDao().getAllFamiliesWithChild(individual.treeId, individual.individualId);
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

            if (anyParent) {
                this.writeLine("");
            }

            //Marriages
            List<Family> marriages = this.database.familyDao().getAllFamiliesForHusbandOrWife(individual.treeId, individual.individualId);
            for(int i = 0; i < marriages.size(); i++) {
                if ( i > 0) {
                    this.writeLine("");
                }

                Family marriage = marriages.get(i);
                Individual spouse = null;
                int spouseId;
                if (marriage.husbandId == individual.individualId) {
                    spouseId = marriage.wifeId;
                } else {
                    spouseId = marriage.husbandId;
                }

                if (spouseId != -1 && this.individualsInActiveTree.containsKey(spouseId)) {
                    spouse = this.individualsInActiveTree.get(spouseId);
                    if (spouse != null) {
                        this.writeLine(AncestryUtil.getMarriageLine(spouse, marriage, nameFormat, this.placesInActiveTree, false));
                    } else {
                        this.writeLine("x <Unknown>");
                    }
                }

                List<FamilyChild> familyChildren = this.database.familyChildDao().getAllFamilyChildren(individual.treeId, marriage.familyId);
                int[] childIds = new int[familyChildren.size()];
                for(int j = 0; j < familyChildren.size(); j++) {
                    childIds[j] = familyChildren.get(j).individualId;
                }

                for(FamilyChild familyChild : familyChildren) {
                    if (this.individualsInActiveTree.containsKey(familyChild.individualId)) {
                        this.writeLine(AncestryUtil.getChildLine(this.individualsInActiveTree.get(familyChild.individualId), nameFormat, false));
                    }
                }
            }

            this.writeLine("");
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
