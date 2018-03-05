package com.sjmeunier.arborfamiliae.util;

import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListSearchUtils {
    public static List<Family> findFamiliesForWifeOrHusband(int individualId, Map<Integer, Family> families) {
        List<Family> result = new ArrayList<>();
        if (families == null)
            return result;

        for(Family family : families.values()) {
            if (family.husbandId == individualId || family.wifeId == individualId)
                result.add(family);
        }
        return result;
    }

    public static List<FamilyChild> findChildrenForFamily(int familyId, List<FamilyChild> familyChildren) {
        List<FamilyChild> result = new ArrayList<>();
        if (familyChildren == null)
            return result;

        for(FamilyChild familyChild : familyChildren) {
            if (familyChild.familyId == familyId)
                result.add(familyChild);
        }
        return result;
    }
}
