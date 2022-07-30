package com.vladrip.drgassistant.fr_builds;

import android.content.Context;

import com.google.gson.Gson;
import com.vladrip.drgassistant.DrgApp;
import com.vladrip.drgassistant.R;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildFactory {
    private static final Gson gson = new Gson();

    public static Build createBuildPreset(DRGClass drgClass, Context context) {
        int jsonId;
        switch (drgClass) {
            case DRILLER: jsonId = R.raw.presets_driller; break;
            case ENGINEER: jsonId = R.raw.presets_engineer; break;
            case GUNNER: jsonId = R.raw.presets_gunner; break;
            default: jsonId = R.raw.presets_scout;
        }

        try (InputStreamReader isr = new InputStreamReader(context.getResources().openRawResource(jsonId))) {
            Build build = gson.fromJson(isr, Build.class);
            long id = DrgApp.getUniqueId(((DrgApp)context.getApplicationContext()).getBuilds());
            build.setId(id);
            build.setName(drgClass.toString().toLowerCase() + " build" + id);

            List<Build.BuildItem> items = new ArrayList<>(Arrays.asList(build.getPrimaries()));
            items.addAll(Arrays.asList(build.getSecondaries()));
            items.add(build.getEq1());
            items.add(build.getEq2());
            items.add(build.getEq3());
            for (Build.BuildItem item : items) {
                Tier overclock = item.getOverclock();
                if (overclock != null)
                    overclock.setSelected(-1);
                for (Tier tiers : item.getTiers())
                    tiers.setSelected(-1);
            }

            return build;
        } catch (Exception e) {
            return null;
        }
    }
}
