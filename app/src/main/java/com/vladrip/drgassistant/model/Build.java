package com.vladrip.drgassistant.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;


public class Build implements Comparable<Build> {
    private final DRGClass drgClass;
    private final BuildItem[] primaries;
    private final BuildItem[] secondaries;
    private final BuildItem eq1;
    private final BuildItem eq2;
    private final BuildItem eq3;
    private final Tier throwable;
    private long id;
    private String name;
    private String description;
    private boolean isFavorite;
    private int selectedPrimary = 0, selectedSecondary = 0;

    public Build(DRGClass drgClass, BuildItem eq1, BuildItem eq2, BuildItem eq3, BuildItem[] primaries, BuildItem[] secondaries, Tier throwable) {
        this.drgClass = drgClass;
        this.eq1 = eq1;
        this.eq2 = eq2;
        this.eq3 = eq3;
        this.primaries = primaries;
        this.secondaries = secondaries;
        this.throwable = throwable;
    }

    public DRGClass getDrgClass() {
        return drgClass;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public BuildItem[] getPrimaries() {
        return primaries;
    }

    public BuildItem[] getSecondaries() {
        return secondaries;
    }

    public BuildItem getEq1() {
        return eq1;
    }

    public BuildItem getEq2() {
        return eq2;
    }

    public BuildItem getEq3() {
        return eq3;
    }

    public Tier getThrowable() {
        return throwable;
    }

    public Tier.TierItem getSelectedThrowable() {
        return throwable.getSelectedItem();
    }

    public void setSelectedPrimary(int selectedPrimary) {
        this.selectedPrimary = selectedPrimary;
    }

    public void setSelectedSecondary(int selectedSecondary) {
        this.selectedSecondary = selectedSecondary;
    }

    public BuildItem[] itemsAsArray() {
        BuildItem[] items = new BuildItem[5];
        items[0] = primaries[selectedPrimary];
        items[1] = secondaries[selectedSecondary];
        items[2] = eq1;
        items[3] = eq2;
        items[4] = eq3;
        return items;
    }

    public int isPrimaryOrSecondary(Build.BuildItem item) {
        if (item.getOverclock() == null)
            return 0;

        String name = item.getName();
        for (BuildItem pr : primaries)
            if (pr.getName().equals(name))
                return 1;
        for (BuildItem sc : secondaries)
            if (sc.getName().equals(name))
                return -1;
        return 0;
    }

    @Override
    public int compareTo(Build o) {
        int res = Boolean.compare(this.isFavorite, o.isFavorite);
        if (res == 0)
            res = o.drgClass.compareTo(this.drgClass); //so DRILLER > ENGINEER
        if (res == 0)
            res = o.name.compareTo(this.name); //driller build1 > driller build2
        return res != 0 ? res : Long.compare(o.id, this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Build) o).id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }


    public static class BuildItem {
        private final String name;
        private final String icon;
        private final Tier[] tiers;
        private final Tier overclock;

        public BuildItem(String name, String icon, int tiersCount, Tier overclock) {
            this.name = name;
            this.icon = icon;
            tiers = new Tier[tiersCount];
            this.overclock = overclock;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public int tiersAmount() {
            return tiers.length;
        }

        public Tier[] getTiers() {
            return tiers;
        }

        public Tier getOverclock() {
            return overclock;
        }

        public Tier.TierItem getSelectedOverclock() {
            return overclock == null ? null : overclock.getSelectedItem();
        }

        public String asNumberStr() {
            StringBuilder sb = new StringBuilder();
            for (Tier t : tiers)
                sb.append(t.getSelected() + 1);
            return sb.toString();
        }

        //@TODO: create arrays.xml and make mapping for all drawables, switch slow getIdentifier with obtainTypedArray (cache array somewhere and pass it in this method as parameter)
        @SuppressLint("DiscouragedApi") //getIdentifier()
        public Drawable getIconDrawable(Context c) {
            return ContextCompat.getDrawable
                    (c, c.getResources().getIdentifier(icon, "drawable", c.getPackageName()));
        }
    }
}
