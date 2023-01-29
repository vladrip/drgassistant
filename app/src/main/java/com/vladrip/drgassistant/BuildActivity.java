package com.vladrip.drgassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.vladrip.drgassistant.adapter.RecyclerBuildAdapter;
import com.vladrip.drgassistant.model.Build;

public class BuildActivity extends AppCompatActivity {
    private final Gson gson = new Gson();
    private Uri buildPresetsExcelUri;
    private Build build;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);

        build = gson.fromJson(getIntent().getStringExtra("build"), Build.class);
        buildPresetsExcelUri = parseExcelUriFor(build);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycler_build_items);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new RecyclerBuildAdapter(build, this));
        recyclerView.setItemViewCacheSize(10);
        new PagerSnapHelper().attachToRecyclerView(recyclerView);

        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                getMenuInflater().inflate(R.menu.build_options_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == android.R.id.home) {
                    returnResult();
                } else if (id == R.id.open_excel_builds) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, buildPresetsExcelUri);
                    startActivity(browserIntent);
                } else return false;
                return true;
            }
        });
    }

    private Uri parseExcelUriFor(Build build) {
        int excelUrlId = R.string.excel_driller;
        switch (build.getDrgClass()) {
            case ENGINEER:
                excelUrlId = R.string.excel_engineer;
                break;
            case GUNNER:
                excelUrlId = R.string.excel_gunner;
                break;
            case SCOUT:
                excelUrlId = R.string.excel_scout;
                break;
        }
        return Uri.parse(getString(excelUrlId));
    }

    private void returnResult() {
        Intent i = new Intent();
        i.putExtra("build", gson.toJson(build));
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnResult();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        getSharedPreferences("builds", 0).edit()
                .putString(String.valueOf(build.getId()), gson.toJson(build))
                .apply();
        super.onStop();
    }
}