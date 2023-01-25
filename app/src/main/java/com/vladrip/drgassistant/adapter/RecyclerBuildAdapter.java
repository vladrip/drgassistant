package com.vladrip.drgassistant.adapter;

import static com.intuit.sdp.R.dimen._12sdp;
import static com.intuit.sdp.R.dimen._24sdp;
import static com.intuit.sdp.R.dimen._40sdp;
import static com.intuit.sdp.R.dimen._4sdp;
import static com.intuit.sdp.R.dimen._60sdp;
import static com.intuit.sdp.R.dimen._6sdp;
import static com.intuit.sdp.R.dimen._70sdp;
import static com.intuit.sdp.R.dimen._7sdp;
import static com.intuit.ssp.R.dimen._12ssp;
import static com.intuit.ssp.R.dimen._6ssp;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vladrip.drgassistant.R;
import com.vladrip.drgassistant.model.Build;
import com.vladrip.drgassistant.model.Tier;

import java.util.Arrays;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class RecyclerBuildAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ADDITIONAL_VIEW_HOLDERS = 2;
    private static final int ITEM = 0, INFO = 1, GRENADE = 2;
    private final Build build;
    private final Context c;

    public RecyclerBuildAdapter(Build build, Context c) {
        this.build = build;
        this.c = c;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View item = inflater.inflate(R.layout.build_item, parent, false);
                viewHolder = new ItemViewHolder(item, c);
                break;

            case INFO:
                View info = inflater.inflate(R.layout.build_info, parent, false);
                viewHolder = new InfoViewHolder(info, c, build);
                break;

            case GRENADE:
                View grenade = inflater.inflate(R.layout.build_item, parent, false);
                viewHolder = new GrenadeViewHolder(grenade, c, build.getThrowable());
                break;

            default:
                View empty = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                ((TextView) empty.findViewById(android.R.id.text1)).setText(android.R.string.unknownName);
                viewHolder = new RecyclerView.ViewHolder(empty) {};
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM:
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                itemViewHolder.setItem(build.itemsAsArray()[position - 1]);
                itemViewHolder.initItemView(build, itemViewHolder.getItem());
                break;

            case INFO:
                InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                infoViewHolder.initInfoView();
                break;

            case GRENADE:
                GrenadeViewHolder grenadeViewHolder = (GrenadeViewHolder) holder;
                grenadeViewHolder.initGrenadeView();
                break;

            default:
                throw new IllegalArgumentException(
                        "ViewHolder with type \"" + holder.getItemViewType() + "\" doesn't exist");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return INFO;
        if (position == getItemCount() - 1) return GRENADE;
        return ITEM;
    }

    @Override
    public int getItemCount() {
        return build.itemsAsArray().length + ADDITIONAL_VIEW_HOLDERS;
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final int LVL_COLOR;
        private final int PADDING_PX;
        private final int VERTICAL_MARGIN_PX;
        private final LinearLayout.LayoutParams DIVIDER;
        private final LinearLayout.LayoutParams TIER_ITEM;

        private final Context c;
        private final GridLayout grid;
        private Build.BuildItem item;
        private ImageView overclockIcon;

        public ItemViewHolder(@NonNull View itemView, Context c) {
            super(itemView);
            this.c = c;
            grid = itemView.findViewById(R.id.tiers_grid);

            LVL_COLOR = c.getColor(R.color.gray);
            Resources res = c.getResources();
            VERTICAL_MARGIN_PX = res.getDimensionPixelSize(_7sdp);
            PADDING_PX = VERTICAL_MARGIN_PX/3 + VERTICAL_MARGIN_PX/4;
            DIVIDER = new LinearLayout.LayoutParams(
                    res.getDimensionPixelSize(_12sdp),
                    res.getDimensionPixelSize(_6sdp));
            int fitBetweenPx = VERTICAL_MARGIN_PX/2 - VERTICAL_MARGIN_PX;
            DIVIDER.setMargins(fitBetweenPx, VERTICAL_MARGIN_PX, fitBetweenPx, VERTICAL_MARGIN_PX);
            DIVIDER.gravity = Gravity.CENTER_VERTICAL;
            TIER_ITEM = new LinearLayout.LayoutParams(
                    res.getDimensionPixelSize(_60sdp),
                    res.getDimensionPixelSize(_40sdp));
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
            int size = c.getResources().getDimensionPixelSize(_70sdp);
            params.width = size;
            params.height = size;
            params.setMargins(0, VERTICAL_MARGIN_PX*2, 0, VERTICAL_MARGIN_PX/2);
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
                    int cords = (Integer)v.getTag();
                    if (nowSelected) {
                        item.getTiers()[cords/10].setSelected(cords%10);
                        deselectTierItems((LinearLayout) v.getParent());
                        v.setBackgroundResource(R.drawable.hexagon_background_selected);
                    } else {
                        item.getTiers()[cords/10].setSelected(-1);
                        v.setBackgroundResource(R.drawable.hexagon_background);
                    }
                    v.setSelected(nowSelected);
                });
            }
        }

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
                lvl.setText(c.getString(R.string.tier_level, tiers[i].getReqLevel()));
                grid.addView(lvl, params);

                fillTier(tiers[i], i);
            }

            Tier overClock = item.getOverclock();
            if (item.getOverclock() != null)
                initOverclockPopup(overClock);
        }

        public void initItemView(Build build, Build.BuildItem item) {
            setItem(item);
            TextView nameIcon = itemView.findViewById(R.id.current_equip);
            nameIcon.setText(item.getName());
            Drawable equipIcon = item.getIconDrawable(c);
            equipIcon.setColorFilter(BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(c.getColor(R.color.light_gray), BlendModeCompat.SRC_IN));
            nameIcon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, equipIcon);

            int prOrSc = build.isPrimaryOrSecondary(item);
            if (prOrSc != 0) {
                nameIcon.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(c, nameIcon);
                    Menu menu = popup.getMenu();
                    Build.BuildItem[] items = prOrSc == 1 ? build.getPrimaries() : build.getSecondaries();
                    for (Build.BuildItem it : items)
                        menu.add(it.getName());

                    popup.setOnMenuItemClickListener(menuItem -> {
                        String menuItemTitle = String.valueOf(menuItem.getTitle());
                        for (int i = 0; i < items.length; i++) {
                            if (items[i].getName().equals(menuItemTitle)) {
                                if (prOrSc == 1)
                                    build.setSelectedPrimary(i);
                                else build.setSelectedSecondary(i);
                                grid.removeAllViews();
                                initItemView(build, items[i]);
                                break;
                            }
                        }
                        return true;
                    });
                    popup.show();
                });
            }

            fillGrid();
        }
    }


    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        private final Context c;
        private final Build build;
        private final EditText buildName;
        private final RichEditor descriptionEditor;
        private final View.OnClickListener editorButtonsListener;

        public InfoViewHolder(@NonNull View itemView, Context c, Build build) {
            super(itemView);
            this.c = c;
            this.build = build;
            buildName = itemView.findViewById(R.id.build_name);
            descriptionEditor = itemView.findViewById(R.id.description);
            editorButtonsListener = v -> {
                int id = v.getId();

                if (List.of(R.id.text_bold, R.id.text_italic, R.id.text_underline, R.id.text_strikethrough,
                        R.id.text_subscript, R.id.text_superscript).contains(id)) {
                    v.setSelected(!v.isSelected());
                    ((ImageButton) v).setColorFilter(ContextCompat
                            .getColor(c, v.isSelected() ? R.color.indian_red : R.color.light_gray));
                }

                if (id == R.id.text_undo) descriptionEditor.undo();
                else if (id == R.id.text_redo) descriptionEditor.redo();
                else if (id == R.id.text_bold) descriptionEditor.setBold();
                else if (id == R.id.text_italic) descriptionEditor.setItalic();
                else if (id == R.id.text_underline) descriptionEditor.setUnderline();
                else if (id == R.id.text_strikethrough) descriptionEditor.setStrikeThrough();
                else if (id == R.id.text_h1) descriptionEditor.setHeading(1);
                else if (id == R.id.text_h2) descriptionEditor.setHeading(2);
                else if (id == R.id.text_h3) descriptionEditor.setHeading(3);
                else if (id == R.id.text_h4) descriptionEditor.setHeading(4);
                else if (id == R.id.text_subscript) descriptionEditor.setSubscript();
                else if (id == R.id.text_superscript) descriptionEditor.setSuperscript();
                else if (id == R.id.text_left) descriptionEditor.setAlignLeft();
                else if (id == R.id.text_center) descriptionEditor.setAlignCenter();
                else if (id == R.id.text_right) descriptionEditor.setAlignRight();
                else if (id == R.id.text_indent) descriptionEditor.setIndent();
                else if (id == R.id.text_outdent) descriptionEditor.setOutdent();
                else if (id == R.id.text_ul) descriptionEditor.setBullets();
                else if (id == R.id.text_ol) descriptionEditor.setNumbers();
            };

            initInfoView();
        }

        public void initInfoView() {
            buildName.clearFocus(); //for some reason it focuses on Pixel API24 emulator
            buildName.setText(build.getName());
            buildName.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                        InputMethodManager imm = (InputMethodManager)
                                c.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(buildName.getWindowToken(), 0);
                        build.setName(String.valueOf(buildName.getText()));
                        buildName.clearFocus();
                        return true;
                    }
                }
                return false;
            });
            buildName.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) build.setName(String.valueOf(buildName.getText()));
            });

            Resources res = c.getResources();
            descriptionEditor.setPlaceholder("Edit build description here...");
            descriptionEditor.setBackgroundColor(Color.LTGRAY);
            descriptionEditor.setFontSize(res.getDimensionPixelSize(_6ssp));
            descriptionEditor.setVerticalScrollBarEnabled(true);
            int padding = res.getDimensionPixelSize(_4sdp);
            descriptionEditor.setPadding(padding, padding, padding, padding);
            descriptionEditor.setHtml(build.getDescription());
            descriptionEditor.setOnTextChangeListener(build::setDescription);

            ViewGroup editorButtons = itemView.findViewById(R.id.editor_buttons);
            for (int i = 0; i < editorButtons.getChildCount(); i++)
                editorButtons.getChildAt(i).setOnClickListener(editorButtonsListener);
        }
    }


    public static class GrenadeViewHolder extends RecyclerView.ViewHolder {
        private final int STATS_FONT_SIZE;
        private final int ROW_BOTTOM_MARGIN;
        private final Context c;
        private final GridLayout grid;
        private final Tier grenades;

        public GrenadeViewHolder(@NonNull View itemView, Context c, Tier grenades) {
            super(itemView);
            this.c = c;
            if (grenades.getSelected() == -1) grenades.setSelected(0);
            this.grenades = grenades;
            grid = itemView.findViewById(R.id.tiers_grid);

            Resources res = c.getResources();
            grid.setBackgroundResource(R.color.build_darker);
            int margin = res.getDimensionPixelSize(_24sdp);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) grid.getLayoutParams();
            layoutParams.setMargins(margin, margin, margin, 0);

            STATS_FONT_SIZE = res.getDimensionPixelSize(_12ssp);
            ROW_BOTTOM_MARGIN = res.getDimensionPixelSize(_12sdp);
        }

        private void fillGrid() {
            Tier.TierItem grenade = grenades.getSelectedItem();
            String[] stats = grenade.getEffect().split("\n");
            grid.removeAllViews();
            grid.setRowCount(stats.length);

            for (int i = 0; i < stats.length; i++) {
                String[] stat = stats[i].split("=");
                if (stat.length != 2)
                    throw new IllegalArgumentException("Wrong stats format: " + grenade.getEffect());

                GridLayout.LayoutParams params0 = new GridLayout.LayoutParams();
                params0.rowSpec = GridLayout.spec(i, 1);
                params0.columnSpec = GridLayout.spec(0, 3f);
                params0.bottomMargin = ROW_BOTTOM_MARGIN;
                TextView statName = new TextView(c);
                statName.setTextSize(TypedValue.COMPLEX_UNIT_PX, STATS_FONT_SIZE);
                statName.setMaxWidth(grid.getWidth() * 3 / 5);
                statName.setText(stat[0]);
                grid.addView(statName, params0);

                GridLayout.LayoutParams params1 = new GridLayout.LayoutParams();
                params1.rowSpec = GridLayout.spec(i, 1);
                params1.columnSpec = GridLayout.spec(1, 1f);
                params0.bottomMargin = ROW_BOTTOM_MARGIN;
                TextView statValue = new TextView(c);
                statValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, STATS_FONT_SIZE);
                statValue.setTextColor(c.getColor(R.color.tier_item_selected));
                statValue.setGravity(Gravity.END);
                statValue.setText(stat[1]);
                grid.addView(statValue, params1);
            }
        }

        public void initGrenadeView() {
            Tier.TierItem grenade = grenades.getSelectedItem();
            TextView nameIcon = itemView.findViewById(R.id.current_equip);
            nameIcon.setText(grenade.getName());
            Drawable equipIcon = grenade.getIconDrawable(c);
            equipIcon.setColorFilter(BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(c.getColor(R.color.light_gray), BlendModeCompat.SRC_IN));
            nameIcon.setCompoundDrawablesWithIntrinsicBounds(null, null, null, equipIcon);

            nameIcon.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(c, nameIcon);
                Menu menu = popup.getMenu();
                Tier.TierItem[] items = grenades.getItems();
                Arrays.stream(items).forEach(item -> menu.add(item.getName()));

                popup.setOnMenuItemClickListener(menuItem -> {
                    String menuItemTitle = String.valueOf(menuItem.getTitle());
                    for (int i = 0; i < items.length; i++) {
                        if (items[i].getName().equals(menuItemTitle)) {
                            grenades.setSelected(i);
                            initGrenadeView();
                            break;
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