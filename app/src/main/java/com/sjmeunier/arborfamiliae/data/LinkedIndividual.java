package com.sjmeunier.arborfamiliae.data;

import java.util.ArrayList;
import java.util.List;

public class LinkedIndividual {
    public int individualId;
    public int fatherId;
    public int motherId;
    public List<Integer> childrenIds;

    public LinkedIndividual(int individualId) {
        this.individualId = individualId;
        fatherId = 0;
        motherId = 0;
        childrenIds = new ArrayList<Integer>();
    }
}
