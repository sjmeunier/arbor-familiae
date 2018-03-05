package com.sjmeunier.arborfamiliae.treelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.database.Tree;

import java.util.List;

public class TreeListAdapter  extends BaseAdapter {
    private Activity activity;
    public List<Tree> treeList;
    private LayoutInflater inflater;
    Resources resources;
    private OnTreeListViewDeleteListener onTreeListViewDeleteListener;
    private OnTreeListViewClickListener onTreeListViewClickListener;
    private OnTreeListViewLongPressListener onTreeListViewLongPressListener;

    public TreeListAdapter(Activity activity, List<Tree> treeList) {
        this.activity = activity;
        this.resources = activity.getApplicationContext().getResources();
        this.treeList = treeList;
        inflater = (LayoutInflater.from(activity.getApplicationContext()));
    }

    public void setOnTreeListViewDeleteListener(OnTreeListViewDeleteListener onTreeListViewDeleteListener) {
        this.onTreeListViewDeleteListener = onTreeListViewDeleteListener;
    }

    public void setOnTreeListViewClickListener(OnTreeListViewClickListener onTreeListViewClickListener) {
        this.onTreeListViewClickListener = onTreeListViewClickListener;
    }

    public void setOnTreeListViewLongPressListener(OnTreeListViewLongPressListener onTreeListViewLongPressListener) {
        this.onTreeListViewLongPressListener = onTreeListViewLongPressListener;
    }


    @Override
    public int getCount() {
        return treeList.size();
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
        view = inflater.inflate(R.layout.listview_treelist, null);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = treeList.get(position).id;
                onTreeListViewClickListener.OnTreeListViewClick(id);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int id = treeList.get(position).id;
                onTreeListViewLongPressListener.OnTreeListViewLongPress(id);
                return true;
            }
        });

        TextView treeNameView = (TextView) view.findViewById(R.id.treelist_tree_name);
        TextView individualCountView = (TextView) view.findViewById(R.id.treelist_individual_count);

        treeNameView.setText(treeList.get(position).name);
        individualCountView.setText(String.format(this.resources.getString(R.string.treelist_individuals), treeList.get(position).individualCount) + ", " + String.format(this.resources.getString(R.string.treelist_families), treeList.get(position).familyCount));

        ImageButton deleteButton = (ImageButton)view.findViewById(R.id.treelist_delete);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);

        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String name = treeList.get(position).name;
                new AlertDialog.Builder(activity, android.R.style.Theme_Holo_Dialog)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete tree " + name + "?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int id = treeList.get(position).id;
                                treeList.remove(position);
                                onTreeListViewDeleteListener.OnTreeListViewDelete(id, name);
                                notifyDataSetChanged();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });
        return view;
    }
}