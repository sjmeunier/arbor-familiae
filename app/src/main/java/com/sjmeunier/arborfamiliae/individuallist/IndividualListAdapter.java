package com.sjmeunier.arborfamiliae.individuallist;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.util.AncestryUtil;

import java.util.List;

public class IndividualListAdapter extends BaseAdapter {
    private Activity activity;
    List<Individual> individualList;
    private LayoutInflater inflater;
    private NameFormat nameFormat;
    Resources resources;
    private OnIndividualListViewClickListener onIndividualListViewClickListener;

    public IndividualListAdapter(Activity activity, List<Individual> individualList, NameFormat nameFormat) {
        this.activity = activity;
        this.nameFormat = nameFormat;
        this.resources = activity.getApplicationContext().getResources();
        this.individualList = individualList;
        inflater = (LayoutInflater.from(activity.getApplicationContext()));
    }

    public void setOnIndividualListViewClickListener(OnIndividualListViewClickListener onIndividualListViewClickListener) {
        this.onIndividualListViewClickListener = onIndividualListViewClickListener;
    }

    @Override
    public int getCount() {
        return individualList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.listview_individuallist, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = individualList.get(position).individualId;
                onIndividualListViewClickListener.OnIndividualListViewClick(id);
            }
        });
        TextView nameView = (TextView) view.findViewById(R.id.individuallist_name);
        TextView datesView = (TextView) view.findViewById(R.id.individuallist_dates);

        nameView.setText(AncestryUtil.generateName(individualList.get(position), nameFormat));
        datesView.setText(AncestryUtil.generateBirthDeathDate(individualList.get(position), true));

        return view;
    }
}