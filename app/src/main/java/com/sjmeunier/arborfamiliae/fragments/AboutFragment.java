package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjmeunier.arborfamiliae.BuildConfig;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.util.Utility;

public class AboutFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Resources resources = getActivity().getResources();
        StringBuilder sb = new StringBuilder();
        sb.append("<b>" + resources.getText(R.string.app_name) + "</b><br/>");
        sb.append(resources.getText(R.string.version) + " " + BuildConfig.VERSION_NAME + "<br/>");
        sb.append(resources.getText(R.string.about_author) + "<br/>");
        sb.append(resources.getText(R.string.about_copyright) + "<br/>");
        sb.append("<br/>");
        sb.append(resources.getText(R.string.about_description) + "<br/>");
        sb.append("<br/>");

        sb.append("<b>" + resources.getText(R.string.about_treelist_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_treelist_description) + "<br/>");
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_individualview_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_individualview_description) + "<br/>");
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_search_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_search_description) + "<br/>");
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_tree_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_tree_description) + "<br/>");
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_fanchart_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_fanchart_description) + "<br/>");
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_relationship_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_relationship_description));
        sb.append("<br/>");
        sb.append("<b>" + resources.getText(R.string.about_heatmap_header) + "</b><br/>");
        sb.append(resources.getText(R.string.about_heatmap_description));

        TextView aboutText = view.findViewById(R.id.about_text);
        aboutText.setText(Utility.getHtmlString(sb.toString()));
        setHasOptionsMenu(false);

        return view;
    }
}
