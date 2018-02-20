package com.sjmeunier.arborfamiliae.reports;

import java.util.ArrayList;
import java.util.List;

public class DemographicLifespan {
    public String name;
    public int males;
    public int females;
    public int total;
    public int minAge;
    public int maxAge;
    public List<Integer> individualIds;

    public DemographicLifespan(int minAge, int maxAge)
    {
        name = String.format("%d-%d", minAge, maxAge);
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.males = 0;
        this.females = 0;
        this.total = 0;
        this.individualIds = new ArrayList<>();
    }
}
