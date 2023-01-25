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
    private final static Uri EXCEL_BUILDS_URI = Uri.parse("https://docs.google.com/spreadsheets/d/1cet1j7oWgf9_UjtttDUrumRdctBjsczZwjnY6x_Q4y0/edit#gid=1977218441");
    private final Gson gson = new Gson();
    private Build build;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);

        build = gson.fromJson(getIntent().getStringExtra("build"), Build.class);

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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, EXCEL_BUILDS_URI);
                    startActivity(browserIntent);
                } else return false;
                return true;
            }
        });
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