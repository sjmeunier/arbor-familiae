package com.sjmeunier.arborfamiliae.data;

import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.ArrayList;
import java.util.List;

public class FamilyIndividuals {
    public Individual spouse;
    public List<Individual> children;

    public FamilyIndividuals() {
        spouse = null;
        children = new ArrayList<Individual>();

    }
}
