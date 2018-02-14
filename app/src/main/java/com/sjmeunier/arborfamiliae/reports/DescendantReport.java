package com.sjmeunier.arborfamiliae.reports;

import android.content.Context;

import com.sjmeunier.arborfamiliae.AncestryUtil;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DescendantReport extends BaseReport {

    public DescendantReport(Context context, AppDatabase database, Map<Integer, Place> placesInActiveTree, Map<Integer, Individual> individualsInActiveTree, Map<Integer, Family> familiesInActiveTree, NameFormat nameFormat, int maxGenerations, int treeId) {
        super(context, database, placesInActiveTree, individualsInActiveTree, familiesInActiveTree, nameFormat, maxGenerations, treeId);
    }

    @Override
    public boolean generateReport(String filename, int activeIndividualId) throws IOException {
        this.configureOutputFile(filename);
        processPerson(activeIndividualId, 1);
        closeFile();
        return true;
    }

    private void processPerson(int individualId, int generation) throws IOException  {
        if (!this.individualsInActiveTree.containsKey(individualId))
            return;

        Individual individual = this.individualsInActiveTree.get(individualId);
        this.writeLine(new String(new char[generation * 2]).replace('\0', ' ') + "- " + AncestryUtil.generateName(individual, this.nameFormat) + " " + AncestryUtil.generateBirthDeathDateWithPlace(individual, this.placesInActiveTree));

        List<Family> families = database.familyDao().getAllFamiliesForHusbandOrWife(this.treeId, individualId);
        for(Family family : families) {
            //Spouse
            if (family.wifeId == individualId) {
                if (family.husbandId == -1 || !this.individualsInActiveTree.containsKey(family.husbandId)) {
                    this.writeLine(new String(new char[generation * 2]).replace('\0', ' ') + "  x <Unknown>");
                } else {
                    this.writeLine(new String(new char[generation * 2]).replace('\0', ' ') + "  " + AncestryUtil.getMarriageLine(this.individualsInActiveTree.get(family.husbandId), family, nameFormat, this.placesInActiveTree, false));
                }
            } else {
                if (family.wifeId == -1 || !this.individualsInActiveTree.containsKey(family.wifeId)) {
                    this.writeLine(new String(new char[generation * 2]).replace('\0', ' ') + "  x <Unknown>");
                } else {
                    this.writeLine(new String(new char[generation * 2]).replace('\0', ' ') + "  " + AncestryUtil.getMarriageLine(this.individualsInActiveTree.get(family.wifeId), family, nameFormat, this.placesInActiveTree, false));
                }
            }

            //Children
            if (generation < this.maxGenerations) {
                List<FamilyChild> familyChildren = database.familyChildDao().getAllFamilyChildren(this.treeId, family.familyId);
                for(FamilyChild familyChild : familyChildren) {
                    processPerson(familyChild.individualId, generation + 1);
                }
            }
        }
    }
}
