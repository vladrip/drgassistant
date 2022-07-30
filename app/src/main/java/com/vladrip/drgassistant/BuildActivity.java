package com.vladrip.drgassistant;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.vladrip.drgassistant.fr_builds.Build;
import com.vladrip.drgassistant.fr_builds.OverclockViewAdapter;
import com.vladrip.drgassistant.fr_builds.RecyclerBuildAdapter;

public class BuildActivity extends AppCompatActivity {
    private final static Uri EXCEL_BUILDS_URI = Uri.parse("https://docs.google.com/spreadsheets/d/1cet1j7oWgf9_UjtttDUrumRdctBjsczZwjnY6x_Q4y0/edit#gid=1977218441");
    private final Gson gson = new Gson();
    private Build build;
    private EditText name;
    private Dialog grenades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);

        build = gson.fromJson(getIntent().getStringExtra("build"), Build.class);
        name = findViewById(R.id.build_name);
        name.setText(build.getName());
        name.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                    InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
                    build.setName(String.valueOf(name.getText()));
                    name.clearFocus();
                    return true;
                }
            }
            return false;
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recycler = findViewById(R.id.recycler_build_items);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(new RecyclerBuildAdapter(build, this));
        recycler.setItemViewCacheSize(5); //щоб постійно bind не викликався
        new PagerSnapHelper().attachToRecyclerView(recycler); //щоб перелистувалися як сторінки

        AlertDialog confirmDeletion = new AlertDialog.Builder(this).setMessage(R.string.delete_confirmation)
                .setNegativeButton(R.string.no, (d, arg) -> d.dismiss())
                .setPositiveButton(R.string.yes, (d, arg) -> returnResult(true)).create();
        name.clearFocus(); //for some reason it focuses on Pixel API24 emulator

        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                getMenuInflater().inflate(R.menu.build_options_menu, menu);
                menu.getItem(1).setIcon(build.getSelectedThrowable().getIconDrawable(getBaseContext()));
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == android.R.id.home) {
                    returnResult(false);
                } else if (id == R.id.choose_grenade) {
                    if (grenades == null)
                        initGrenadeChooser(menuItem);
                    grenades.show();
                } else if (id == R.id.open_excel_builds) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, EXCEL_BUILDS_URI);
                    startActivity(browserIntent);
                } else return false;
                return true;
            }
        });
    }

    private void initGrenadeChooser(MenuItem item) {
        grenades = new Dialog(this);
        grenades.setContentView(R.layout.overclock_popup);

        OverclockViewAdapter adapter = new OverclockViewAdapter(this,
                R.layout.listview_overclock, build.getThrowable().getItems(), true);
        ListView grenadesList = grenades.findViewById(R.id.overclocks_listview);
        grenadesList.setAdapter(adapter);
        grenadesList.setOnItemClickListener((parent, view, position, id) -> {
            build.getThrowable().setSelected(position);
            item.setIcon(adapter.getItem(position).getIconDrawable(this));
            grenades.dismiss();
        });
    }

    private void returnResult(boolean isDelete) {
        build.setName(String.valueOf(name.getText()));
        Intent i = new Intent();
        i.putExtra("build", gson.toJson(build));
        setResult(isDelete ? RESULT_CANCELED : RESULT_OK, i);
        finish();
    }

    @Override
    public void onBackPressed() {
        returnResult(false);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        getSharedPreferences("builds", 0).edit()
                .putString(String.valueOf(build.getId()), gson.toJson(build)).apply();
        super.onStop();
    }
}