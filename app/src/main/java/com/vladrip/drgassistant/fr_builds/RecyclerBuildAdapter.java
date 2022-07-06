package com.vladrip.drgassistant.fr_builds;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vladrip.drgassistant.R;

public class RecyclerBuildAdapter extends RecyclerView.Adapter<RecyclerBuildAdapter.ViewHolder> {
    private final Build build;
    private final Context c;

    public RecyclerBuildAdapter(Build build, Context c) {
        this.build = build;
        this.c = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_build, parent, false);
        return new ViewHolder(c, v, build.getSelectedPrimary());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setItem(build.itemsAsArray()[position]);
        holder.initViewFor(build, holder.item);
    }

    @Override
    public int getItemCount() {
        return build.itemsAsArray().length;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private Build.BuildItem item;
        private final Context c;
        private final GridLayout grid;
        private ImageView overclockIcon;

        private final int LVL_COLOR;
        private final int PADDING_PX;
        private final int VERTICAL_MARGIN_PX;
        private final LinearLayout.LayoutParams DIVIDER;
        private final LinearLayout.LayoutParams TIER_ITEM;

        public ViewHolder(Context c, View view, Build.BuildItem item) {
            super(view);
            this.c = c;
            setItem(item);
            grid = view.findViewById(R.id.tiers_grid);

            LVL_COLOR = c.getColor(R.color.darkest_white);
            Resources res = c.getResources();
            VERTICAL_MARGIN_PX = res.getDimensionPixelSize(com.intuit.sdp.R.dimen._7sdp);
            PADDING_PX = VERTICAL_MARGIN_PX/3 + VERTICAL_MARGIN_PX/4;
            DIVIDER = new LinearLayout.LayoutParams(
                    res.getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp),
                    res.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp));
            int fitBetweenPx = VERTICAL_MARGIN_PX/2 - VERTICAL_MARGIN_PX;
            DIVIDER.setMargins(fitBetweenPx, VERTICAL_MARGIN_PX, fitBetweenPx, VERTICAL_MARGIN_PX);
            DIVIDER.gravity = Gravity.CENTER_VERTICAL;
            TIER_ITEM = new LinearLayout.LayoutParams(
                    res.getDimensionPixelSize(com.intuit.sdp.R.dimen._60sdp),
                    res.getDimensionPixelSize(com.intuit.sdp.R.dimen._40sdp));
            TIER_ITEM.setMargins(0, VERTICAL_MARGIN_PX, 0, VERTICAL_MARGIN_PX);
            TIER_ITEM.gravity = Gravity.CENTER_VERTICAL;
        }

        public Build.BuildItem getItem() {
            return item;
        }
        public void setItem(Build.BuildItem item) {
            this.item = item;
        }

        private void deselectTierItems(LinearLayout layout) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                if (v.getClass() == ImageView.class && v.isSelected()) {
                    v.setBackgroundResource(R.drawable.hexagon_background);
                    v.setSelected(false);
                }
            }
        }

        private void initOverclockPopup(Tier overclock) {
            Dialog dialog = new Dialog(c);
            dialog.setContentView(R.layout.overclock_popup);

            OverclockViewAdapter adapter = new OverclockViewAdapter(c,
                    R.layout.listview_overclock, item.getOverclock().getItems(), false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(item.tiersAmount(), 1);
            params.columnSpec = GridLayout.spec(0, 2);
            int size = c.getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._70sdp);
            params.width = size;
            params.height = size;
            params.setMargins(0, VERTICAL_MARGIN_PX*3/2, 0, VERTICAL_MARGIN_PX/2);
            params.setGravity(Gravity.CENTER);
            overclockIcon = adapter.getOverclockView(c, overclock.getSelectedItem(), null);
            overclockIcon.setOnClickListener(v -> dialog.show());
            grid.addView(overclockIcon, params);

            ListView overclocks = dialog.findViewById(R.id.overclocks_listview);
            overclocks.setAdapter(adapter);
            overclocks.setOnItemClickListener((parent, view, position, id) -> {
                if (position == overclock.getSelected()) {
                    overclock.setSelected(-1);
                    overclockIcon.setImageDrawable(null);
                    adapter.getOverclockView(c, null, overclockIcon);
                } else {
                    overclock.setSelected(position);
                    adapter.getOverclockView(c, adapter.getItem(position), overclockIcon);
                }
                dialog.dismiss();
            });
        }

        private void fillTier(Tier t, int row) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(row, 1);
            params.columnSpec = GridLayout.spec(1, 1);
            LinearLayout tierLayout = new LinearLayout(c);
            tierLayout.setTag(row);
            grid.addView(tierLayout, params);

            Tier.TierItem[] items = t.getItems();
            int selected = t.getSelected();
            for (int i = 0; i < items.length; i++) {
                if (i != 0) {
                    View divider = new View(c);
                    divider.setZ(-1);
                    divider.setBackgroundResource(R.color.build_darker);
                    tierLayout.addView(divider, DIVIDER);
                }

                ImageView tierItem = new ImageView(c);
                tierItem.setTag(row * 10 + i);
                if (i == selected) {
                    tierItem.setBackgroundResource(R.drawable.hexagon_background_selected);
                    tierItem.setSelected(true);
                }
                else tierItem.setBackgroundResource(R.drawable.hexagon_background);
                tierItem.setPadding(PADDING_PX, PADDING_PX, PADDING_PX, PADDING_PX);
                tierItem.setImageDrawable(items[i].getIconDrawable(c));
                TooltipCompat.setTooltipText(tierItem, items[i].getEffect());
                tierLayout.addView(tierItem, TIER_ITEM);

                tierItem.setOnClickListener(v -> {
                    boolean nowSelected = !v.isSelected();
                    int coords = (Integer)v.getTag();
                    if (nowSelected) {
                        item.getTiers()[coords/10].setSelected(coords%10);
                        deselectTierItems((LinearLayout) v.getParent());
                        v.setBackgroundResource(R.drawable.hexagon_background_selected);
                    } else {
                        item.getTiers()[coords/10].setSelected(-1);
                        v.setBackgroundResource(R.drawable.hexagon_background);
                    }
                    v.setSelected(nowSelected);
                });
            }
        }

        @SuppressLint("SetTextI18n")
        private void fillGrid() {
            Tier[] tiers = item.getTiers();
            for (int i = 0; i < item.tiersAmount(); i++) {
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1);
                params.columnSpec = GridLayout.spec(0, 1);
                params.setGravity(Gravity.CENTER_VERTICAL);
                params.setMargins(VERTICAL_MARGIN_PX/4, VERTICAL_MARGIN_PX/2, VERTICAL_MARGIN_PX, VERTICAL_MARGIN_PX/2);
                TextView lvl = new TextView(c);
                lvl.setTextColor(LVL_COLOR);
                lvl.setText("Level " + tiers[i].getReqLevel());
                grid.addView(lvl, params);

                fillTier(tiers[i], i);
            }

            Tier overClock = item.getOverclock();
            if (item.getOverclock() != null)
                initOverclockPopup(overClock);
        }

        public void initViewFor(Build build, Build.BuildItem item) {
            setItem(item);

            TextView nameIcon = itemView.findViewById(R.id.current_equip);
            nameIcon.setText(item.getName());
            Drawable equipIcon = item.getIconDrawable(c);
            equipIcon.setColorFilter(c.getColor(R.color.darker_white), PorterDuff.Mode.SRC_IN);
            nameIcon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, equipIcon);

            int prOrSc = build.isPrimaryOrSecondary(item);
            if (prOrSc != 0)
               nameIcon.setOnClickListener(v -> {
                   PopupMenu popup = new PopupMenu(c, nameIcon);
                   Menu menu = popup.getMenu();
                   Build.BuildItem[] items = prOrSc == 1 ? build.getPrimaries() : build.getSecondaries();
                   for (Build.BuildItem it : items)
                        menu.add(it.getName());

                   popup.setOnMenuItemClickListener((menuItem) -> {
                       for (int i = 0; i < items.length; i++) {
                           if (items[i].getName().contentEquals(menuItem.getTitle())) {
                               if (prOrSc == 1)
                                   build.setSelectedPrimary(i);
                               else build.setSelectedSecondary(i);
                               grid.removeAllViews();
                               initViewFor(build, items[i]);
                           }
                       }
                       return true;
                   });
                   popup.show();
               });

            fillGrid();
        }
    }
}
