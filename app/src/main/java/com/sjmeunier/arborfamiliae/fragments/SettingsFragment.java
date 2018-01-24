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

        ListPreference generationsPreference = (ListPreference)findPreference("generations_preference");
        generationsPreference.setSummary(generationsPreference.getEntry());
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
