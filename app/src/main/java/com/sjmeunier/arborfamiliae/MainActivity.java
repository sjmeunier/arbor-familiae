package com.sjmeunier.arborfamiliae;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.database.Place;
import com.sjmeunier.arborfamiliae.database.Tree;
import com.sjmeunier.arborfamiliae.fragments.*;
import com.sjmeunier.arborfamiliae.util.AncestryUtil;

import java.util.ArrayList;
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
    public List<FamilyChild> familyChildrenInActiveTree;

    public int rootIndividualId;

    private RecentList recentIndividuals = new RecentList(10);
    private TextView activeIndividualName;
    private TextView rootIndividualName;

    public NameFormat nameFormat;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getDatabase(getApplicationContext());

        placesInActiveTree = new HashMap<>();
        individualsInActiveTree = new HashMap<>();
        familiesInActiveTree = new HashMap();
        familyChildrenInActiveTree = new ArrayList<>();

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
        activeIndividualName = findViewById(R.id.active_individual_name);
        rootIndividualName = findViewById(R.id.root_individual_name);

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
            setActiveIndividual(rootIndividualId, true);
            FragmentManager fragmentManager = getFragmentManager();
            try {
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.main_fragment_container);
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, currentFragment.getClass().newInstance()).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // redirectToIndividual();
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
            case R.id.nav_lines_of_descent:
                fragmentClass = LinesOfDescentFragment.class;
                break;
            case R.id.nav_reports:
                fragmentClass = ReportsFragment.class;
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
    public void setActiveIndividual(int individualId, boolean updateView) {
        if (activeTree == null) {
            activeIndividual = null;
            return;
        }
        activeIndividual = database.individualDao().getIndividual(activeTree.id, individualId);

        if (activeIndividual == null) {
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("activeIndividual_" + String.valueOf(activeTree.id), individualId);
        editor.commit();

        if (updateView && activeIndividualName != null)
            activeIndividualName.setText(AncestryUtil.generateName(activeIndividual, nameFormat));

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

        if (rootIndividualName != null)
            rootIndividualName.setText(AncestryUtil.generateName(activeIndividual, nameFormat));

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
        TreeLoader treeLoader = new TreeLoader(this);
        treeLoader.execute(treeId);
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

    private class TreeLoader extends AsyncTask<Integer, Integer, Boolean> {
        private Context context;
        private ProgressDialog progressDialog;

        public TreeLoader (Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int treeId = params[0];
            activeTree = database.treeDao().getTree(treeId);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.context);
            if (activeTree != null) {
                //Load cached data
                List<Place> places = database.placeDao().getAllPlaces(treeId);
                placesInActiveTree = new HashMap<Integer, Place>(places.size());
                for(Place place : places) {
                    placesInActiveTree.put(place.placeId, place);
                }
                places = null;

                List<Individual> individuals = database.individualDao().getAllIndividuals(treeId);
                individualsInActiveTree = new HashMap<Integer, Individual>(individuals.size());
                for(Individual individual : individuals) {
                    individualsInActiveTree.put(individual.individualId, individual);
                }
                individuals = null;

                List<Family> families = database.familyDao().getAllFamilies(treeId);
                familiesInActiveTree = new HashMap<Integer, Family>(families.size());
                for(Family family : families) {
                    familiesInActiveTree.put(family.familyId, family);
                }
                families = null;

                familyChildrenInActiveTree = database.familyChildDao().getAllFamilyChildrenForTree(treeId);

                //Load recent list
                String recentIds = settings.getString("recentIndividuals_" + String.valueOf(activeTree.id), "");
                recentIndividuals.deserialize(recentIds);

                //Set root individual
                int id = settings.getInt("activeIndividual_" + String.valueOf(activeTree.id), 0);
                if (id > 0) {
                    setActiveIndividual(id, false);
                } else {
                    activeIndividual = database.individualDao().getIndividual(activeTree.id, activeTree.defaultIndividual);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("activeIndividual_" + String.valueOf(activeTree.id), activeTree.defaultIndividual);
                    editor.commit();

                    addRecentIndividual(activeTree.defaultIndividual);
                }

                getRootIndividual();

            } else {
                deleteTreePreferences(treeId);
                activeIndividual = null;
                recentIndividuals.clear();
            }

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("activeTree", treeId);
            editor.commit();
            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (activeTree != null) {
                //Update views
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                TextView navTreeText = headerView.findViewById(R.id.selected_tree_text);
                if (navTreeText != null)
                    navTreeText.setText(activeTree.name);
                if (rootIndividualName != null)
                    rootIndividualName.setText(AncestryUtil.generateName(individualsInActiveTree.get(rootIndividualId), nameFormat));
                if (activeIndividualName != null)
                    activeIndividualName.setText(AncestryUtil.generateName(activeIndividual, nameFormat));
                Toast.makeText(context, context.getResources().getText(R.string.message_tree_selected) + " " + activeTree.name, Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
            this.context = null;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context, R.style.MyProgressDialog);
            progressDialog.setTitle(context.getResources().getText(R.string.progress_tree));
            progressDialog.setMessage(context.getResources().getText(R.string.progress_pleasewait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }
}
