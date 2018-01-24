package com.sjmeunier.arborfamiliae;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;
import com.sjmeunier.arborfamiliae.database.Tree;
import com.sjmeunier.arborfamiliae.gedcom.GedcomImportService;
import com.sjmeunier.arborfamiliae.fragments.*;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String PREFS_NAME = "ArborFamiliaePrefs";

    public AppDatabase database;

    public Tree activeTree;
    public Individual activeIndividual;
    public HashMap<Integer, Place> placesInActiveTree;
    public HashMap<Integer, Individual> individualsInActiveTree;
    public HashMap<Integer, Family> familiesInActiveTree;

    public int rootIndividualId;

    private RecentList recentIndividuals = new RecentList(10);
    private GedcomRequestReceiver receiver;

    private NameFormat nameFormat;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getDatabase(getApplicationContext());

        IntentFilter filter = new IntentFilter(GedcomRequestReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new GedcomRequestReceiver();
        registerReceiver(receiver, filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.menu = navigationView.getMenu();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        nameFormat = NameFormat.values()[Integer.parseInt(settings.getString("nameformat_preference", "0"))];

        int treeId = settings.getInt("activeTree", 0);
        if (treeId > 0) {
            setActiveTree(treeId);
        }
        if (findViewById(R.id.main_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            if (treeId > 0) {
                TreeListFragment defaultFragment = new TreeListFragment();
                defaultFragment.setArguments(getIntent().getExtras());

                getFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_container, defaultFragment).commit();
            } else {
                IndividualBiographicalFragment defaultFragment = new IndividualBiographicalFragment();
                defaultFragment.setArguments(getIntent().getExtras());

                getFragmentManager().beginTransaction()
                        .add(R.id.main_fragment_container, defaultFragment).commit();
            }
        }
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_jump_to_root) {
            setActiveIndividual(rootIndividualId);
            redirectToIndividual();
            return true;
        } else if (id == R.id.action_set_home_person) {
            setRootIndividual();
        }

        return super.onOptionsItemSelected(item);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_treelist:
                fragmentClass = TreeListFragment.class;
                break;
            case R.id.nav_individual_details:
                fragmentClass = IndividualBiographicalFragment.class;
                break;
            case R.id.nav_search:
                fragmentClass = SearchFragment.class;
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.nav_tree:
                fragmentClass = TreeChartFragment.class;
                break;
            case R.id.nav_heatmap:
                fragmentClass = HeatmapFragment.class;
                break;
            case R.id.nav_fanchart:
                fragmentClass = FanchartFragment.class;
                break;
            case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                break;
            case R.id.nav_recent:
                fragmentClass = RecentFragment.class;
                break;
            case R.id.nav_relationship:
                fragmentClass = RelationshipFragment.class;
                break;
            default:
                fragmentClass = TreeListFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void redirectToIndividual() {
        MenuItem menuItem = menu.findItem(R.id.nav_individual_details);
        if (menuItem != null) {
            menuItem.setChecked(true);
            // Set action bar title
            setTitle(menuItem.getTitle());
        }

        FragmentManager fragmentManager = getFragmentManager();
        try {
            fragmentManager.beginTransaction().replace(R.id.main_fragment_container, (Fragment)IndividualBiographicalFragment.class.newInstance()).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void setActiveIndividual(int individualId) {
        activeIndividual = database.individualDao().getIndividual(activeTree.id, individualId);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activeIndividual_" + String.valueOf(activeTree.id), individualId);
        editor.commit();

        addRecentIndividual(individualId);
    }

    public void getRootIndividual() {
        if (activeTree == null || activeIndividual == null)
            rootIndividualId = 0;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        rootIndividualId = settings.getInt("rootIndividual_" + String.valueOf(activeTree.id), activeTree.defaultIndividual);
    }

    public void setRootIndividual() {
        if (activeTree == null || activeIndividual == null)
            return;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("rootIndividual_" + String.valueOf(activeTree.id), activeIndividual.individualId);
        editor.commit();

        Toast.makeText(this, getResources().getText(R.string.message_root_person_set) + " " + AncestryUtil.generateName(activeIndividual, nameFormat), Toast.LENGTH_SHORT).show();

        rootIndividualId = activeIndividual.individualId;
    }


    public void clearActiveTree() {
        activeIndividual = null;
        activeTree = null;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activeTree", 0);
        editor.commit();

        clearRecentIndividuals();
        rootIndividualId = 0;
    }
    public void setActiveTree(int treeId) {
        activeTree = database.treeDao().getTree(treeId);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (activeTree != null) {
            //Load cached data
            List<Place> places = database.placeDao().getAllPlaces(treeId);
            this.placesInActiveTree = new HashMap<Integer, Place>(places.size());
            for(Place place : places) {
                this.placesInActiveTree.put(place.placeId, place);
            }
            places = null;

            List<Individual> individuals = database.individualDao().getAllIndividuals(treeId);
            this.individualsInActiveTree = new HashMap<Integer, Individual>(individuals.size());
            for(Individual individual : individuals) {
                this.individualsInActiveTree.put(individual.individualId, individual);
            }
            individuals = null;

            List<Family> families = database.familyDao().getAllFamilies(treeId);
            this.familiesInActiveTree = new HashMap<Integer, Family>(families.size());
            for(Family family : families) {
                this.familiesInActiveTree.put(family.familyId, family);
            }
            families = null;

            //Load recent list
            String recentIds = settings.getString("recentIndividuals_" + String.valueOf(activeTree.id), "");
            recentIndividuals.deserialize(recentIds);

            //Set root individual
            int id = settings.getInt("activeIndividual_" + String.valueOf(activeTree.id), 0);
            if (id > 0) {
                setActiveIndividual(id);
            } else {
                activeIndividual = database.individualDao().getIndividual(activeTree.id, activeTree.defaultIndividual);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("activeIndividual_" + String.valueOf(activeTree.id), activeTree.defaultIndividual);
                editor.commit();

                addRecentIndividual(activeTree.defaultIndividual);
            }

            getRootIndividual();

            //Update views
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            TextView navTreeText = headerView.findViewById(R.id.selected_tree_text);
            if (navTreeText != null)
                navTreeText.setText(activeTree.name);
            Toast.makeText(this, getResources().getText(R.string.message_tree_selected) + " " + activeTree.name, Toast.LENGTH_SHORT).show();
        } else {
            activeIndividual = null;
            recentIndividuals.clear();
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("activeTree", treeId);
        editor.commit();

    }
    public void deleteTreePreferences(int treeId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("recentIndividuals_" + String.valueOf(treeId), "");
        editor.putInt("activeIndividual" + String.valueOf(treeId), 0);
        editor.commit();

    }

    public void clearRecentIndividuals() {
        recentIndividuals.clear();

        if (activeTree != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("recentIndividuals_" + String.valueOf(activeTree.id), "");
            editor.commit();
        }
    }

    public void addRecentIndividual(int id) {
        if (activeTree == null)
            return;

        recentIndividuals.addItem(id);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("recentIndividuals_" + String.valueOf(activeTree.id), recentIndividuals.serialize());
        editor.commit();
    }

    public int[] getRecentIndividuals() {
        return recentIndividuals.getItems();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
        return true;
    }

    public class GedcomRequestReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.sjmeunier.arborfamiliae.intent.action.GEDCOM_PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(GedcomImportService.PARAM_OUT_MSG);
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
