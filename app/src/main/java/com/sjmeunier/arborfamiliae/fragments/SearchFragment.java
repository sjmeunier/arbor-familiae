package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sjmeunier.arborfamiliae.IndividualListAdapter;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.OnIndividualListViewClickListener;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Individual;

import java.util.List;

public class SearchFragment extends Fragment{

    MainActivity mainActivity;
    private NameFormat nameFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity)getActivity();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        setHasOptionsMenu(true);
        return view;
    }

    public void searchForIndividuals(String text) {
        List<Individual> individuals =  mainActivity.database.individualDao().findIndividualsByName(mainActivity.activeTree.id, "%" + text + "%", 100);


        ListView resultList = (ListView) getView().findViewById(R.id.result_list);
        IndividualListAdapter resultListAdapter = new IndividualListAdapter(mainActivity, individuals, nameFormat);
        resultList.setAdapter(resultListAdapter);

        resultListAdapter.setOnIndividualListViewClickListener(new OnIndividualListViewClickListener() {
            @Override
            public void OnIndividualListViewClick(int individualId) {
                mainActivity.setActiveIndividual(individualId);
                mainActivity.redirectToIndividual();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Implementing ActionBar Search inside a fragment
        MenuItem item = menu.add(mainActivity.getResources().getText(R.string.action_search));
        item.setIcon(R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(getActivity());
        sv.setQueryHint(mainActivity.getResources().getText(R.string.search_hint));
        sv.setIconifiedByDefault(false);

        // implementing the listener
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchForIndividuals(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        item.setActionView(sv);
    }
}
