package com.sjmeunier.arborfamiliae.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sjmeunier.arborfamiliae.R;


public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.fragment_preferences);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(spChanged);

        ListPreference fanchartGenerationsPreference = (ListPreference)findPreference("fanchart_generations_preference");
        fanchartGenerationsPreference.setSummary(fanchartGenerationsPreference.getEntry());
        ListPreference treechartGenerationsPreference = (ListPreference)findPreference("treechart_generations_preference");
        treechartGenerationsPreference.setSummary(treechartGenerationsPreference.getEntry());
        ListPreference heatmapGenerationsPreference = (ListPreference)findPreference("heatmap_generations_preference");
        heatmapGenerationsPreference.setSummary(heatmapGenerationsPreference.getEntry());
        ListPreference nameFormatPreference = (ListPreference)findPreference("nameformat_preference");
        nameFormatPreference.setSummary(nameFormatPreference.getEntry());
        ListPreference heatmapEventsPreference = (ListPreference)findPreference("heatmap_events_preference");
        heatmapEventsPreference.setSummary(heatmapEventsPreference.getEntry());
    }

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    Preference pref = findPreference(key);

                    if (pref instanceof ListPreference) {
                        ListPreference listPref = (ListPreference) pref;
                        pref.setSummary(listPref.getEntry());
                    }
                }
            };
}
