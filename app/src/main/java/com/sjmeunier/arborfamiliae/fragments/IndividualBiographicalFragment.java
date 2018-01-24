package com.sjmeunier.arborfamiliae.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.sjmeunier.arborfamiliae.AncestryUtil;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.IndividualNote;
import com.sjmeunier.arborfamiliae.database.IndividualSource;
import com.sjmeunier.arborfamiliae.database.Note;
import com.sjmeunier.arborfamiliae.database.Source;
import com.sjmeunier.arborfamiliae.util.Utility;

import java.util.List;
import java.util.jar.Attributes;

public class IndividualBiographicalFragment extends Fragment {

    private MainActivity mainActivity;
    private NameFormat nameFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_individual_biographical, container, false);
        mainActivity = (MainActivity)getActivity();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        updateBiographicalData();
    }

    private void updateBiographicalData() {
        View view = getView();
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        TextView notFoundTextView = view.findViewById(R.id.individual_not_found);
        TableLayout biographicalTable = view.findViewById(R.id.individual_biographical_table);

        setHasOptionsMenu(true);

        if (mainActivity.activeIndividual != null) {
            notFoundTextView.setVisibility(View.GONE);
            biographicalTable.setVisibility(View.VISIBLE);
            biographicalTable.removeAllViews();

            TextView label;
            TextView text;

            //Biographical section
            View row = inflater.inflate(R.layout.tablerow_individual_header, null, false);
            text = row.findViewById(R.id.tablerow_individual_header_text);
            text.setText(mainActivity.getResources().getText(R.string.header_biographical));
            biographicalTable.addView(row);

            row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
            label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
            label.setText(mainActivity.getResources().getText(R.string.label_name));
            text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
            text.setText(AncestryUtil.generateName(mainActivity.activeIndividual, nameFormat));
            biographicalTable.addView(row);

            String birth = AncestryUtil.getBirthDateAndPlace(mainActivity.activeIndividual, mainActivity.placesInActiveTree);
            if (birth.length() > 3) {
                row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                label.setText(mainActivity.getResources().getText(R.string.label_birth));
                text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                text.setText(birth);
                biographicalTable.addView(row);
            }

            String baptism = AncestryUtil.getBaptismDateAndPlace(mainActivity.activeIndividual, mainActivity.placesInActiveTree);
            if (baptism.length() > 3) {
                row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                label.setText(mainActivity.getResources().getText(R.string.label_baptism));
                text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                text.setText(baptism);
                biographicalTable.addView(row);
            }

            String death = AncestryUtil.getDeathDateAndPlace(mainActivity.activeIndividual, mainActivity.placesInActiveTree);
            if (death.length() > 3) {
                row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                label.setText(mainActivity.getResources().getText(R.string.label_death));
                text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                text.setText(death);
                biographicalTable.addView(row);
            }

            String burial = AncestryUtil.getBurialDateAndPlace(mainActivity.activeIndividual, mainActivity.placesInActiveTree);
            if (burial.length() > 3) {
                row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                label.setText(mainActivity.getResources().getText(R.string.label_burial));
                text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                text.setText(burial);
                biographicalTable.addView(row);
            }


            if (!TextUtils.isEmpty(mainActivity.activeIndividual.occupation)) {
                row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                label.setText(mainActivity.getResources().getText(R.string.label_occupation));
                text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                text.setText(mainActivity.activeIndividual.occupation);
                biographicalTable.addView(row);
            }

            //Family section
            row = inflater.inflate(R.layout.tablerow_individual_header, null, false);
            text = row.findViewById(R.id.tablerow_individual_header_text);
            text.setText(mainActivity.getResources().getText(R.string.header_family));
            biographicalTable.addView(row);

            //Parents
            List<FamilyChild> parentFamilies =  mainActivity.database.familyChildDao().getAllFamiliesWithChild(mainActivity.activeIndividual.treeId, mainActivity.activeIndividual.individualId);
            boolean anyParent = false;
            if (parentFamilies.size() > 0) {
                Family family = mainActivity.database.familyDao().getFamily(mainActivity.activeIndividual.treeId, parentFamilies.get(0).familyId);
                if (family != null) {
                    Individual parent;

                    parent = mainActivity.database.individualDao().getIndividual(mainActivity.activeIndividual.treeId, family.husbandId);
                    if (parent != null){
                        row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                        label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                        label.setText(mainActivity.getResources().getText(R.string.label_father));
                        text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                        text.setText(Utility.getHtmlString(AncestryUtil.generateBoldNameWithDates(parent, nameFormat)));
                        text.setTag(parent.individualId);
                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if ((int)view.getTag() > 0)
                                    setActiveIndividual((int)view.getTag());
                            }
                        });
                        biographicalTable.addView(row);
                        anyParent = true;
                    }

                    parent = mainActivity.database.individualDao().getIndividual(mainActivity.activeIndividual.treeId, family.wifeId);
                    if (parent != null){
                        row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
                        label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
                        label.setText(mainActivity.getResources().getText(R.string.label_mother));
                        text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
                        text.setTag(parent.individualId);
                        text.setText(Utility.getHtmlString(AncestryUtil.generateBoldNameWithDates(parent, nameFormat)));
                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if ((int)view.getTag() > 0)
                                    setActiveIndividual((int)view.getTag());
                            }
                        });
                        biographicalTable.addView(row);
                        anyParent = true;
                    }

                }
            }
            if (anyParent) {
                row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                text.setText(" ");
                biographicalTable.addView(row);
            }

            //Marriages
            List<Family> marriages = mainActivity.database.familyDao().getAllFamiliesForHusbandOrWife(mainActivity.activeIndividual.treeId, mainActivity.activeIndividual.individualId);
            for(int i = 0; i < marriages.size(); i++) {
                if ( i > 1) {
                    row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                    text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                    text.setText(" ");
                    biographicalTable.addView(row);
                }

                Family marriage = marriages.get(i);
                Individual spouse = null;
                int spouseId;
                if (marriage.husbandId == mainActivity.activeIndividual.individualId) {
                    spouseId = marriage.wifeId;
                } else {
                    spouseId = marriage.husbandId;
                }

                spouse = mainActivity.database.individualDao().getIndividual(mainActivity.activeIndividual.treeId, spouseId);
                if (spouse != null) {
                    row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                    text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                    text.setText(Utility.getHtmlString(AncestryUtil.getMarriageLine(spouse, marriage, nameFormat, mainActivity.placesInActiveTree)));
                    text.setTag(spouse.individualId);
                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((int)view.getTag() > 0)
                                setActiveIndividual((int) view.getTag());
                        }
                    });
                    biographicalTable.addView(row);
                } else {
                    row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                    text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                    text.setText(Utility.getHtmlString("x &lt;Unknown&gt;"));
                    text.setTag(0);
                    biographicalTable.addView(row);
                }

                List<FamilyChild> familyChildren = mainActivity.database.familyChildDao().getAllFamilyChildren(mainActivity.activeIndividual.treeId, marriage.familyId);
                int[] childIds = new int[familyChildren.size()];
                for(int j = 0; j < familyChildren.size(); j++) {
                    childIds[j] = familyChildren.get(j).individualId;
                }

                List<Individual> children = mainActivity.database.individualDao().getIndividualsInList(mainActivity.activeIndividual.treeId, childIds);
                for(int j = 0; j < children.size(); j++) {

                    row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                    text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                    text.setText(Utility.getHtmlString(AncestryUtil.getChildLine(children.get(j), nameFormat)));
                    text.setTag(children.get(j).individualId);
                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if ((int)view.getTag() > 0)
                                setActiveIndividual((int)view.getTag());
                        }
                    });
                    biographicalTable.addView(row);
                }
            }

            //Notes section
            row = inflater.inflate(R.layout.tablerow_individual_header, null, false);
            text = row.findViewById(R.id.tablerow_individual_header_text);
            text.setText(mainActivity.getResources().getText(R.string.header_notes));
            biographicalTable.addView(row);

            List<IndividualNote> individualNotes = mainActivity.database.individualNoteDao().getAllIndividualNotes(mainActivity.activeIndividual.treeId, mainActivity.activeIndividual.individualId);
            int[] noteIds = new int[individualNotes.size()];
            for(int j = 0; j < individualNotes.size(); j++) {
                noteIds[j] = individualNotes.get(j).noteId;
            }

            List<Note> notes = mainActivity.database.noteDao().getNotesInList(mainActivity.activeIndividual.treeId, noteIds);
            for(int i = 0; i < notes.size(); i++) {
                row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                text.setText(notes.get(i).text);
                biographicalTable.addView(row);
            }

            //Sources section
            row = inflater.inflate(R.layout.tablerow_individual_header, null, false);
            text = row.findViewById(R.id.tablerow_individual_header_text);
            text.setText(mainActivity.getResources().getText(R.string.header_sources));
            biographicalTable.addView(row);

            List<IndividualSource> individualSources = mainActivity.database.individualSourceDao().getAllIndividualSources(mainActivity.activeIndividual.treeId, mainActivity.activeIndividual.individualId);
            int[] sourceIds = new int[individualSources.size()];
            for(int j = 0; j < individualSources.size(); j++) {
                sourceIds[j] = individualSources.get(j).sourceId;
            }

            List<Source> sources = mainActivity.database.sourceDao().getSourcesInList(mainActivity.activeIndividual.treeId, sourceIds);
            for(int i = 0; i < sources.size(); i++) {
                row = inflater.inflate(R.layout.tablerow_individual_onecolumn, null, false);
                text = row.findViewById(R.id.tablerow_individual_onecolumn_col1);
                text.setText(sources.get(i).text);
                biographicalTable.addView(row);
            }

            row = inflater.inflate(R.layout.tablerow_individual_twocolumn, null, false);
            label = row.findViewById(R.id.tablerow_individual_twocolumn_col1);
            label.setText(mainActivity.getResources().getText(R.string.label_gedcomid));
            text = row.findViewById(R.id.tablerow_individual_twocolumn_col2);
            text.setText(Integer.toString(mainActivity.activeIndividual.individualId));
            biographicalTable.addView(row);
        } else {
            notFoundTextView.setVisibility(View.VISIBLE);
            biographicalTable.setVisibility(View.GONE);
        }
    }

    private void setActiveIndividual(int individualId) {
        mainActivity.setActiveIndividual(individualId);

        updateBiographicalData();
    }
}
