package com.vladrip.drgassistant;

import android.app.Application;

import com.vladrip.drgassistant.fr_builds.Build;

import java.util.ArrayList;
import java.util.List;

public class DrgApp extends Application {
    private final List<Build> builds = new ArrayList<>();

    public List<Build> getBuilds() {
        return builds;
    }

    public int getBuildsSize() {
        return builds.size();
    }
}
