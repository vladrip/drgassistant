package com.vladrip.drgassistant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.vladrip.drgassistant.adapter.BuildViewAdapter;
import com.vladrip.drgassistant.model.Build;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, Integer> wikiUrlToIcon;
    private View wikiView;
    private View buildsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadBuilds();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        }

        wikiUrlToIcon = Map.of(
                getString(R.string.wiki_gg_url), R.drawable.wikigg_logo,
                getString(R.string.fandom_url), R.drawable.fandom_logo
        );
        navView.getMenu().findItem(R.id.navigation_wiki).setIcon(getSelectedWikiIconId(null));
        navView.setItemIconTintList(null);
    }

    private void loadBuilds() {
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("builds", 0);
        List<Build> builds = ((DrgApp) getApplicationContext()).getBuilds();
        builds.clear();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet())
            builds.add(gson.fromJson((String) e.getValue(), Build.class));
        Collections.sort(builds);

        DrgApp app = ((DrgApp) getApplicationContext());
        BuildViewAdapter mainAdapter = app.getMainAdapter();
        if (mainAdapter == null)
            app.setMainAdapter(new BuildViewAdapter(this, R.layout.listview_build, builds, false));
        else mainAdapter.notifyDataSetChanged();
    }

    public View getWikiView() {
        return wikiView;
    }

    public void setWikiView(View fandomView) {
        this.wikiView = fandomView;
    }

    public View getBuildsView() {
        return buildsView;
    }

    public void setBuildsView(View buildsView) {
        this.buildsView = buildsView;
    }

    public int getSelectedWikiIconId(String selectedWiki) {
        Integer iconId;
        if (selectedWiki == null)
            iconId = wikiUrlToIcon.get(getSharedPreferences("wiki", 0)
                    .getString("selected_wiki", getString(R.string.wiki_gg_url)));
        else iconId = wikiUrlToIcon.get(selectedWiki);
        return iconId != null ? iconId : 0;
    }

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("builds", 0).edit();
        editor.clear();
        Gson gson = new Gson();
        for (Build b : ((DrgApp) getApplicationContext()).getBuilds())
            editor.putString(String.valueOf(b.getId()), gson.toJson(b));
        editor.apply();
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }
}