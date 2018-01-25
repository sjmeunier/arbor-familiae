package com.sjmeunier.arborfamiliae.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.R;
import com.sjmeunier.arborfamiliae.data.Events;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatmapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MainActivity mainActivity;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private List<LatLng> locations;
    private AppDatabase database;
    private int treeId;
    private LatLng rootLocation;
    private int maxGeneration;
    private Events events;
    Map<Integer, Individual> individuals;

    private int RADIUS = 20;
    private float OPACITY = 0.7f;
    private final static int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.rgb(102, 225, 0),
            Color.rgb(255, 0, 0)
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_heatmap, container, false);
        setHasOptionsMenu(false);

        mainActivity = (MainActivity)getActivity();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        maxGeneration = Integer.parseInt(settings.getString("generations_preference", "4"));
        events = Events.values()[Integer.parseInt(settings.getString("heatmap_events_preference", "0"))];

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocations();

        float startVal = (events == Events.AllEvents) ? 2f : 1.2f;
        float endVal = (events == Events.AllEvents) ? 15f : 8f;
        float[] HEATMAP_GRADIENT_START_POINTS = {
                (startVal / (float)locations.size()) , (endVal / (float)locations.size())
        };

        Gradient HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
                HEATMAP_GRADIENT_START_POINTS);

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(locations)
                .radius(RADIUS)
                .opacity(OPACITY)
                .gradient(HEATMAP_GRADIENT)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(rootLocation));
    }

    private void getLocations() {
        locations = new ArrayList<LatLng>();

        if (mainActivity.activeIndividual == null || mainActivity.activeTree == null)
            return;

        database = AppDatabase.getDatabase(mainActivity);
        treeId = mainActivity.activeTree.id;

        individuals = new HashMap<Integer, Individual>();

        if (mainActivity.activeIndividual != null) {
            Individual root = mainActivity.activeIndividual;
            individuals.put(1, root);

            addLocations(root);
            processGeneration(1, 1, root.parentFamilyId);
        }
        if (locations.size() > 0)
            rootLocation = locations.get(0);
        else
            rootLocation = new LatLng(0, 0);

    }

    private void processGeneration(int generation, int childAhnenNumber, int familyId) {
        Family family = mainActivity.familiesInActiveTree.get(familyId);
        if (family == null)
            return;

        addFamilyLocations(family);

        Individual father = mainActivity.individualsInActiveTree.get(family.husbandId);
        if (father != null) {
            addLocations(father);
            individuals.put(childAhnenNumber * 2, father);
            if (generation < maxGeneration && father.parentFamilyId != 0) {
                processGeneration(generation + 1, childAhnenNumber * 2, father.parentFamilyId);
            }
        }
        Individual mother = mainActivity.individualsInActiveTree.get(family.wifeId);
        if (mother != null) {
            addLocations(mother);
            individuals.put((childAhnenNumber * 2) + 1, mother);
            if (generation < maxGeneration && mother.parentFamilyId != 0) {
                processGeneration(generation + 1, (childAhnenNumber * 2) + 1, mother.parentFamilyId);
            }
        }
    }

    private void addLocation(int placeId) {
        Place place = mainActivity.placesInActiveTree.get(placeId);
        if (place.longitude != 0 && place.latitude != 0)
            locations.add(new LatLng(place.latitude, place.longitude));
    }
    private void addLocations(Individual individual) {
        if ((events == Events.AllEvents || events == Events.Births) && (individual.birthPlace != -1))
            addLocation(individual.birthPlace);
        if ((events == Events.AllEvents || events == Events.Baptisms) && (individual.baptismPlace != -1))
            addLocation(individual.baptismPlace);
        if ((events == Events.AllEvents || events == Events.Deaths) && (individual.diedPlace != -1))
            addLocation(individual.diedPlace);
        if ((events == Events.AllEvents || events == Events.Burials) && (individual.burialPlace != -1))
            addLocation(individual.burialPlace);
    }

    private void addFamilyLocations(Family family) {
        if ((events == Events.AllEvents || events == Events.Marriages) && (family.marriagePlace != -1))
            addLocation(family.marriagePlace);
    }
}
