package com.vladrip.drgassistant.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vladrip.drgassistant.R;
import com.vladrip.drgassistant.model.Tier;

public class OverclockViewAdapter extends ArrayAdapter<Tier.TierItem> {
    private final int resource;
    private final int overclockTint;
    private final int SVG_PADDING;
    private final boolean areGrenades;

    public OverclockViewAdapter(@NonNull Context context, int resource, @NonNull Tier.TierItem[] objects, boolean areGrenades) {
        super(context, resource, objects);
        this.resource = resource;
        overclockTint = context.getColor(R.color.light_gray);
        SVG_PADDING = context.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp);
        this.areGrenades = areGrenades;
    }

    //@TODO: create arrays.xml and make mapping for all drawables, switch slow getIdentifier with getStringArray (cache array somewhere and pass it in this method as parameter)
    @SuppressLint("DiscouragedApi") //getIdentifier()
    public ImageView getOverclockView(Context c, Tier.TierItem item, ImageView convertView) {
        ImageView icon = convertView;
        if (convertView == null)
            icon = new ImageView(c);

        if (!areGrenades) {
            String frame = item == null ? "green" : item.getIcon().split(" ")[0];
            icon.setBackgroundResource(c.getResources()
                    .getIdentifier("overclock_" + frame, "drawable", c.getPackageName()));
            icon.setColorFilter(overclockTint);
            icon.setPadding(SVG_PADDING, SVG_PADDING , SVG_PADDING, SVG_PADDING);
        }

        icon.setColorFilter(overclockTint);
        if (item != null)
            icon.setImageDrawable(item.getIconDrawable(c));
        return icon;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        Context c = getContext();
        if (v == null) {
            v = LayoutInflater.from(c).inflate(resource, null);
        }

        Tier.TierItem item = getItem(position);
        getOverclockView(c, item, v.findViewById(R.id.overclock_icon));
        ((TextView) v.findViewById(R.id.overclock_name)).setText(item.getName());
        ((TextView) v.findViewById(R.id.overclock_effect)).setText(item.getEffect());

        return v;
    }
}
