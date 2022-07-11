package com.vladrip.drgassistant;

import android.app.Application;

import com.vladrip.drgassistant.fr_builds.Build;
import com.vladrip.drgassistant.fr_builds.BuildViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DrgApp extends Application {
    private final List<Build> builds = new ArrayList<>();
    private BuildViewAdapter mainAdapter;

    public List<Build> getBuilds() {
        return builds;
    }
    public BuildViewAdapter getMainAdapter() {
        return mainAdapter;
    }

    void setMainAdapter(BuildViewAdapter mainAdapter) {
        this.mainAdapter = mainAdapter;
    }

    public int getBuildsSize() {
        return builds.size();
    }
}
