package com.vladrip.drgassistant.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vladrip.drgassistant.R;
import com.vladrip.drgassistant.model.Build;
import com.vladrip.drgassistant.model.Tier;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuildViewAdapter extends ArrayAdapter<Build> {
    private final int resource;
    private final OverclockViewAdapter ocIconFactory;
    private final int OC_PADDING;
    private final boolean multiCheck;
    private final Set<Build> checkedItems = new HashSet<>();

    public BuildViewAdapter(@NonNull Context context, int resource, @NonNull List<Build> objects, boolean multiCheck) {
        super(context, resource, objects);
        this.resource = resource;
        ocIconFactory = new OverclockViewAdapter(context, 0, new Tier.TierItem[0], false);
        OC_PADDING = context.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp);
        this.multiCheck = multiCheck;
        sort(Comparator.reverseOrder());
        setNotifyOnChange(true);
    }

    private void attachOverclock(Tier.TierItem oc, View parent, boolean isPrimary) {
        int ocViewId = isPrimary ? R.id.primary_overclock : R.id.secondary_overclock;
        ImageView ocIcon = ocIconFactory.getOverclockView(getContext(), oc, parent.findViewById(ocViewId));
        ocIcon.setPadding(OC_PADDING, OC_PADDING, OC_PADDING, OC_PADDING);
        if (oc == null)
            ocIcon.setImageDrawable(null);
    }

    public Set<Build> getCheckedItems() {
        return Collections.unmodifiableSet(checkedItems);
    }
    public void selectAll(boolean isSelect) {
        if (isSelect)
            for (int i = 0; i < getCount(); i++)
                checkedItems.add(getItem(i));
        else checkedItems.clear();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Build b = getItem(position);
        View v = convertView;
        Context c = getContext();
        if (v == null) {
            v = LayoutInflater.from(c).inflate(resource, null);
        }

        ((TextView)v.findViewById(R.id.build_name)).setText(b.getName());
        int colorId;
        switch(b.getDrgClass()) {
            case DRILLER: colorId = R.color.driller; break;
            case ENGINEER: colorId = R.color.engineer; break;
            case GUNNER: colorId = R.color.gunner; break;
            case SCOUT: colorId = R.color.scout; break;
            default: throw new IllegalArgumentException("DRG class doesn't exist: " + b.getDrgClass());
        }
        v.findViewById(R.id.drg_class_color).setBackgroundResource(colorId);

        Build.BuildItem[] items = b.itemsAsArray();
        ((ImageView)v.findViewById(R.id.primary_icon)).setImageDrawable(items[0].getIconDrawable(c));
        ((ImageView)v.findViewById(R.id.secondary_icon)).setImageDrawable(items[1].getIconDrawable(c));
        ((ImageView)v.findViewById(R.id.eq1_icon)).setImageDrawable(items[2].getIconDrawable(c));
        ((ImageView)v.findViewById(R.id.eq2_icon)).setImageDrawable(items[3].getIconDrawable(c));
        ((ImageView)v.findViewById(R.id.eq3_icon)).setImageDrawable(items[4].getIconDrawable(c));

        TextView primary = v.findViewById(R.id.primary_build);
        primary.setText(items[0].asNumberStr());
        attachOverclock(items[0].getOverclock().getSelectedItem(), v, true);
        TextView secondary = v.findViewById(R.id.secondary_build);
        secondary.setText(items[1].asNumberStr());
        attachOverclock(items[1].getOverclock().getSelectedItem(), v, false);

        ((TextView)v.findViewById(R.id.eq1_build)).setText(items[2].asNumberStr());
        ((TextView)v.findViewById(R.id.eq2_build)).setText(items[3].asNumberStr());
        ((TextView)v.findViewById(R.id.eq3_build)).setText(items[4].asNumberStr());

        ImageView selGrenade = v.findViewById(R.id.selected_grenade);
        selGrenade.setImageDrawable(b.getSelectedThrowable().getIconDrawable(c));

        ImageView mark = v.findViewById(R.id.mark_as_favorite);
        mark.setColorFilter(b.isFavorite() ? Color.YELLOW : Color.BLACK);
        if (!multiCheck)
            mark.setOnClickListener(imgV -> {
                boolean isSelected = !b.isFavorite();
                b.setFavorite(isSelected);
                ((ImageView)imgV).setColorFilter(isSelected ? Color.YELLOW : Color.BLACK);
                sort(Comparator.reverseOrder());
            });
        else {
            CheckBox checkBox = v.findViewById(R.id.build_checkbox);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setClickable(true);
            checkBox.setChecked(checkedItems.contains(b));
            checkBox.setOnClickListener(box -> {
                Build pressedOn = getItem(position);
                if (((CheckBox)box).isChecked())
                    checkedItems.add(pressedOn);
                else checkedItems.remove(pressedOn);
            });
        }

        return v;
    }
}
