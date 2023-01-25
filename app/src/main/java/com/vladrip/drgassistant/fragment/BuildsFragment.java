package com.vladrip.drgassistant.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vladrip.drgassistant.BuildActivity;
import com.vladrip.drgassistant.DrgApp;
import com.vladrip.drgassistant.MainActivity;
import com.vladrip.drgassistant.MultiChoiceActivity;
import com.vladrip.drgassistant.MultiChoiceActivity.MultiAction;
import com.vladrip.drgassistant.R;
import com.vladrip.drgassistant.model.Build;
import com.vladrip.drgassistant.model.BuildFactory;
import com.vladrip.drgassistant.model.DRGClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class BuildsFragment extends DrgBaseFragment {
    //private FragmentBuildsBinding binding;
    private MainActivity main;
    private ListView listView;
    private final Gson gson = new Gson();
    private final ActivityResultLauncher<Intent> buildLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (data == null) return;

                String json = data.getStringExtra("build");
                Build b = gson.fromJson(json, Build.class);

                List<Build> builds = ((DrgApp)main.getApplicationContext()).getBuilds();
                builds.set(builds.indexOf(b), b);
                ((DrgApp)main.getApplicationContext()).getMainAdapter().notifyDataSetChanged();
            });
    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent res = result.getData();
                if (res == null || result.getResultCode() != RESULT_OK)
                    return;

                Uri uri = result.getData().getData();
                try (InputStream is = main.getContentResolver().openInputStream(uri)) {
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(is));
                    StringBuilder json = new StringBuilder();
                    for (String line; (line = bfr.readLine()) != null; )
                        json.append(line).append('\n');
                    ((DrgApp)main.getApplicationContext()).getMainAdapter()
                            .addAll(DrgApp.checkUniqueId(gson.fromJson(json.toString(),
                            new TypeToken<List<Build>>(){}.getType())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return getPersistentView(((MainActivity)requireActivity()).getBuildsView(),
                inflater, container, R.layout.fragment_builds);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        main = (MainActivity) requireActivity();
        listView = rootView.findViewById(R.id.builds_listview);
        listView.setAdapter(((DrgApp)main.getApplicationContext()).getMainAdapter());
        initListeners();
        Resources r = getResources();
        TooltipCompat.setTooltipText(rootView.findViewById(R.id.add_build), r.getText(R.string.tip_add));
        TooltipCompat.setTooltipText(rootView.findViewById(R.id.multi_share_build), r.getText(R.string.tip_share));
        TooltipCompat.setTooltipText(rootView.findViewById(R.id.import_build), r.getText(R.string.tip_import));
        TooltipCompat.setTooltipText(rootView.findViewById(R.id.multi_delete_build), r.getText(R.string.tip_delete));
    }

    public void initListeners() {
        rootView.findViewById(R.id.add_build).setOnClickListener(v -> {
            v.animate().rotationBy(90).setDuration(400).start();

            PopupMenu popup = new PopupMenu(main, v);
            popup.inflate(R.menu.choose_drgclass_menu);
            popup.setOnMenuItemClickListener((item) -> {
                if (item.getItemId() == R.id.drgclasses_driller)
                    addBuildPreset(DRGClass.DRILLER);
                else if (item.getItemId() == R.id.drgclasses_engineer)
                    addBuildPreset(DRGClass.ENGINEER);
                else if (item.getItemId() == R.id.drgclasses_gunner)
                    addBuildPreset(DRGClass.GUNNER);
                else addBuildPreset(DRGClass.SCOUT);
                return true;
            });
            popup.show();
        });

        rootView.findViewById(R.id.multi_share_build).setOnClickListener(v -> multiChoiceActivity(MultiAction.SHARE));
        rootView.findViewById(R.id.multi_delete_build).setOnClickListener(v -> multiChoiceActivity(MultiAction.DELETE));
        rootView.findViewById(R.id.import_build).setOnClickListener(v -> {
            Intent openFile = new Intent();
            openFile.setAction(Intent.ACTION_OPEN_DOCUMENT);
            openFile.setType("application/json");
            importLauncher.launch(openFile);
        });

        listView.setOnItemClickListener((parent, v, pos, id)  -> {
            Intent i = new Intent(main, BuildActivity.class);
            String json = gson.toJson(((DrgApp)main.getApplicationContext()).getBuilds().get(pos));
            i.putExtra("build", json);
            buildLauncher.launch(i);
        });
    }

    private void addBuildPreset(DRGClass drgClass) {
        Build build = BuildFactory.createBuildPreset(drgClass, main);
        ((DrgApp)main.getApplicationContext()).getMainAdapter().add(build);
    }

    private void multiChoiceActivity(MultiAction action) {
        Intent i = new Intent(main, MultiChoiceActivity.class);
        i.putExtra("action", action.toString());
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((DrgApp)main.getApplicationContext()).getMainAdapter().notifyDataSetChanged();
        main.setBuildsView(rootView);
    }
}