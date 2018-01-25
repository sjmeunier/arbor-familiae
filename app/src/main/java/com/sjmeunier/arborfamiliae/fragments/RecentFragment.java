package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sjmeunier.arborfamiliae.IndividualListAdapter;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.OnIndividualListViewClickListener;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment{

    MainActivity mainActivity;
    private NameFormat nameFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);
        mainActivity = (MainActivity)getActivity();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        int[] recentIds = mainActivity.getRecentIndividuals();
        List<Individual> individuals = new ArrayList<Individual>();
        for(int i = 0; i < recentIds.length; i++) {
            Individual individual = mainActivity.database.individualDao().getIndividual(mainActivity.activeTree.id, recentIds[i]);
            if (individual != null)
                individuals.add(individual);
        }

        ListView recentList = (ListView) view.findViewById(R.id.recent_list);
        IndividualListAdapter recentListAdapter = new IndividualListAdapter(mainActivity, individuals, nameFormat);
        recentList.setAdapter(recentListAdapter);

        recentListAdapter.setOnIndividualListViewClickListener(new OnIndividualListViewClickListener() {
            @Override
            public void OnIndividualListViewClick(int individualId) {
                mainActivity.setActiveIndividual(individualId, true);
                mainActivity.redirectToIndividual();
            }
        });
        setHasOptionsMenu(false);

        return view;
    }
}
