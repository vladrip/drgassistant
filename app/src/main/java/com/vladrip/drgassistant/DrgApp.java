package com.vladrip.drgassistant;

import android.app.Application;

import com.vladrip.drgassistant.adapter.BuildViewAdapter;
import com.vladrip.drgassistant.model.Build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DrgApp extends Application {
    private final List<Build> builds = new ArrayList<>();
    private BuildViewAdapter mainAdapter;

    public static long getUniqueId(Collection<Build> builds) {
        return builds.stream().map(Build::getId).max(Long::compareTo).orElse(0L) + 1;
    }

    public static Collection<Build> checkUniqueId(Collection<Build> builds) {
        for (Build b : builds)
            b.setId(getUniqueId(builds));
        return builds;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public BuildViewAdapter getMainAdapter() {
        return mainAdapter;
    }

    void setMainAdapter(BuildViewAdapter mainAdapter) {
        this.mainAdapter = mainAdapter;
    }
}
