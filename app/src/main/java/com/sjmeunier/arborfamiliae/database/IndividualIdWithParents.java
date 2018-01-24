package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class IndividualIdWithParents {

    public int individualId;
    public int fatherId;
    public int motherId;
    public int familyId;


    public IndividualIdWithParents() {
        individualId = 0;
        fatherId = 0;
        motherId = 0;
        familyId = 0;
    }

}
