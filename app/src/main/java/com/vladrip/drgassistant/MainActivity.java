package com.vladrip.drgassistant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.vladrip.drgassistant.databinding.ActivityMainBinding;
import com.vladrip.drgassistant.fr_builds.Build;
import com.vladrip.drgassistant.fr_builds.BuildViewAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private View fandomView;
    private View buildsView;
    private static BuildViewAdapter adapter;
    public static final List<Build> builds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this);
        loadBuilds();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration
                    .Builder(R.id.navigation_builds, R.id.navigation_fandom)
                    .build();
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
    }

    private void loadBuilds() {
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("builds", 0);
        builds.clear();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet())
            builds.add(gson.fromJson((String)e.getValue(), Build.class));
        Collections.sort(builds);

        if (adapter == null)
            adapter = new BuildViewAdapter(this, R.layout.listview_build, builds, false);
        else adapter.notifyDataSetChanged();
    }

    public View getFandomView() {
        return fandomView;
    }
    public View getBuildsView() {
        return buildsView;
    }
    public static BuildViewAdapter getAdapter() {
        return adapter;
    }

    public void setFandomView(View fandomView) {
        this.fandomView = fandomView;
    }
    public void setBuildsView(View buildsView) {
        this.buildsView = buildsView;
    }

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("builds", 0).edit();
        editor.clear();
        Gson gson = new Gson();
        for (Build b : builds)
            editor.putString(String.valueOf(b.getId()), gson.toJson(b));
        editor.apply();
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }
}