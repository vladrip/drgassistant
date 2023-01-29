package com.vladrip.drgassistant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.vladrip.drgassistant.adapter.BuildViewAdapter;
import com.vladrip.drgassistant.model.Build;
import com.vladrip.drgassistant.model.Tier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class MultiChoiceActivity extends AppCompatActivity {
    private MultiAction action;
    private int HORIZONTAL_MARGIN;
    private BuildViewAdapter adapter;
    private final ActivityResultLauncher<String> saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            fileUri -> {
                if (fileUri == null) return;
                try (OutputStream out = getContentResolver().openOutputStream(fileUri)) {
                    out.write(new Gson().toJson(adapter.getCheckedItems()).getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    private boolean allSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_choice);
        action = MultiAction.valueOf(getIntent().getStringExtra("action"));
        HORIZONTAL_MARGIN = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._12sdp);

        initListView();
        placeActionButtons();
    }

    private void initListView() {
        adapter = new BuildViewAdapter(this,
                R.layout.listview_build, ((DrgApp) getApplicationContext()).getBuilds(), true);
        ListView buildsList = findViewById(R.id.multi_builds_listview);
        buildsList.setAdapter(adapter);
    }

    private void placeActionButtons() {
        findViewById(R.id.multi_select_all).setOnClickListener(v -> {
            allSelected = !allSelected;
            selectAllChoices(allSelected);
        });

        LinearLayout panel = findViewById(R.id.multi_buttons_panel);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1f;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        switch (action) {
            case DELETE:
                params.setMargins(HORIZONTAL_MARGIN * 4, 0, HORIZONTAL_MARGIN * 4, 0);
                Dialog confirmDeletion = new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_confirmation)
                        .setNegativeButton(R.string.no, (d, arg) -> d.dismiss())
                        .setPositiveButton(R.string.yes, (d, arg) -> {
                            BuildViewAdapter mainAdapter = ((DrgApp) getApplicationContext()).getMainAdapter();
                            for (Build b : adapter.getCheckedItems())
                                mainAdapter.remove(b);
                            adapter.notifyDataSetChanged();
                            selectAllChoices(false);
                        }).create();

                Button deleteBtn = createButton(R.string.delete);
                deleteBtn.setOnClickListener(v -> confirmDeletion.show());
                panel.addView(deleteBtn, params);
                break;

            case SHARE:
                params.setMargins(HORIZONTAL_MARGIN, 0, HORIZONTAL_MARGIN, 0);
                Button exportBtn = createButton(R.string.export);
                exportBtn.setOnClickListener(v -> exportBuilds());

                Button shareBtn = createButton(R.string.share);
                shareBtn.setOnClickListener(v -> {
                    Intent sendReadable = new Intent();
                    sendReadable.setAction(Intent.ACTION_SEND);
                    sendReadable.setType("text/plain");
                    sendReadable.putExtra(Intent.EXTRA_TEXT, toReadableFormat(adapter.getCheckedItems()));
                    startActivity(Intent.createChooser(sendReadable, "Share builds via"));
                });

                panel.addView(exportBtn, params);
                panel.addView(shareBtn, params);
                break;
        }
    }

    private Button createButton(int textId) {
        Button btn = new Button(this);
        btn.setBackgroundResource(R.color.indian_red);
        btn.setPadding(HORIZONTAL_MARGIN, 0, HORIZONTAL_MARGIN, 0);
        btn.setText(textId);
        return btn;
    }

    private String toReadableFormat(Collection<Build> builds) {
        if (builds.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Build b : builds) {
            sb.append("**\"").append(b.getName()).append("\"**");
            for (Build.BuildItem item : b.itemsAsArray()) {
                sb.append("\n• ").append(item.getName()).append(" --> ").append(item.asNumberStr());

                Tier.TierItem overclock = item.getSelectedOverclock();
                if (overclock != null)
                    sb.append("\noverclock — ").append(overclock.getName());
            }
            sb.append("\n• Throwable — ").append(b.getSelectedThrowable().getName());
            sb.append("\n\n");
        }

        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private void exportBuilds() {
        EditText input = new EditText(this);
        input.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        new AlertDialog.Builder(this).setTitle("File name:").setView(input)
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setPositiveButton("Create", (dialog, id) ->
                        saveFileLauncher.launch(input.getText().toString()))
                .show();
    }

    private void selectAllChoices(boolean isSelect) {
        adapter.selectAll(isSelect);
        adapter.notifyDataSetChanged();
    }

    public enum MultiAction {
        DELETE,
        SHARE
    }
}