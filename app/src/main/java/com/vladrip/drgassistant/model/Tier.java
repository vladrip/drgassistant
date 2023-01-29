package com.vladrip.drgassistant.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class Tier {
    private final TierItem[] items;
    private final int reqLevel;
    private int selected = -1;

    public Tier(int size, int reqLevel) {
        this.reqLevel = reqLevel;
        items = new TierItem[size];
    }

    public TierItem[] getItems() {
        return items;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public TierItem getSelectedItem() {
        return selected == -1 ? null : items[selected];
    }

    public static class TierItem {
        private final String icon;
        private final String name;
        private final String effect;

        public TierItem(String icon, String name, String effect) {
            this.icon = icon;
            this.name = name;
            this.effect = effect;
        }

        public String getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getEffect() {
            return effect;
        }

        //@TODO: create arrays.xml and make mapping for all drawables, switch slow getIdentifier with obtainTypedArray (cache array somewhere and pass it in this method as parameter)
        @SuppressLint("DiscouragedApi") //getIdentifier()
        public Drawable getIconDrawable(Context c) {
            String iconName = icon;
            if (icon.contains(" "))
                iconName = icon.split(" ")[1];
            return ContextCompat.getDrawable
                    (c, c.getResources().getIdentifier(iconName, "drawable", c.getPackageName()));
        }
    }
}
