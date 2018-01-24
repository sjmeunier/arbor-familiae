package com.sjmeunier.arborfamiliae;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

public class RecentList {
    private ArrayList items;
    private int maxSize;

    public RecentList(int maxSize) {
        items = new ArrayList();
        this.maxSize = maxSize;
    }

    public RecentList(int maxSize, int[] initialItems) {
        items = new ArrayList();
        this.maxSize = maxSize;
        for(int i = 0; i < initialItems.length; i++) {
            items.add(initialItems[i]);
        }
    }

    public void addItem(int item) {
        if (item < 1)
            return;
        //check if already in list and if so remove it
        int position = items.indexOf(item);
        if (position >= 0)
            items.remove(position);
        items.add(0, item);

        if (items.size() > maxSize)
            items.remove(items.size() - 1);
    }

    public int getSize() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public int[] getItems() {
        int[] returnItems = new int[items.size()];
        for(int i = 0; i < items.size(); i++)
            returnItems[i] = (int)items.get(i);
        return returnItems;
    }

    public String serialize() {
        String result = "";
        for(int i = 0; i < items.size() - 1; i++)
            result += String.valueOf((int)items.get(i)) + ";";
        if (items.size() > 0)
            result += String.valueOf((int)items.get(items.size() - 1));
        return result;
    }

    public void deserialize(String str) {
        String[] values = str.split(";");

        items.clear();
        try {
            for (int i = 0; i < values.length; i++)
                items.add(Integer.parseInt(values[i]));
        } catch (Exception e) {
            items.clear();
        }
    }
}
