package com.android.hackslash.openehr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "Navigation Activity";
    private DownloadFiles downFile = new DownloadFiles();
    public File[] files;
    LinearLayout homeLL, rootLL;
    Button updateFiles;
    TextView availfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new Thread(new Runnable() {
            public void run() {
                /*
                type =0 check files are there or not, if not download
                type =1 force download files
                 */
                Boolean downloadStatus = downFile.download(0);
                Log.i(TAG, "Download Status : " + downloadStatus);
                if (!downloadStatus) {
                    Toast.makeText(getApplicationContext(), "Unable to download archetypes from" +
                            " server, please try again!", Toast.LENGTH_SHORT).show();
                } else {
                    loadFileNames();
                }
            }
        }).start();

        homeLL = findViewById(R.id.homeLL);
        rootLL = findViewById(R.id.rootLL);
        updateFiles = findViewById(R.id.updateButton);
        availfiles = findViewById(R.id.availfile);

        updateFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Boolean downloadStatus = downFile.download(1);
                        Log.i(TAG, "Download Status : " + downloadStatus);
                        if (!downloadStatus) {
                            Toast.makeText(getApplicationContext(), "Unable to update archetypes from" +
                                    " server, please try again!", Toast.LENGTH_SHORT).show();
                        } else {
                            loadFileNames();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportActionBar().setTitle("OpenEHR");
            rootLL.setVisibility(View.GONE);
            homeLL.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_action) {
            getSupportActionBar().setTitle("Action");
            homeLL.setVisibility(View.GONE);
            rootLL.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("ACTION");
        } else if (id == R.id.nav_evaluation) {
            getSupportActionBar().setTitle("Evaluation");
            homeLL.setVisibility(View.GONE);
            rootLL.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("EVALUATION");
        } else if (id == R.id.nav_observation) {
            getSupportActionBar().setTitle("Observation");
            homeLL.setVisibility(View.GONE);
            rootLL.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("OBSERVATION");
        } else if (id == R.id.nav_instruction) {
            getSupportActionBar().setTitle("Instruction");
            homeLL.setVisibility(View.GONE);
            rootLL.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("INSTRUCTION");
        } else if (id == R.id.nav_settings) {
            getSupportActionBar().setTitle("Settings");
            homeLL.setVisibility(View.GONE);
            rootLL.setVisibility(View.GONE);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFileNames() {
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data", "com.android.hackslash.openehr1");
        files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);
        String filename = "";
        for (File file : files) {
            Log.d("Files", "FileName:" + file.getName());
            filename += file.getName() + "\n";
        }

        final String temp = filename;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                availfiles.setText(temp);
            }
        });
    }

    private void addViews(final String type) {
        for (final File file : files) {
            if (file.getName().contains(type)) {
                Button button = new Button(this);

                String buttonName = file.getName().replace("openEHR-EHR-", "")
                        .replace(".adl", "").replace(type + ".", "");
                button.setText(buttonName);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NavigationActivity.this, FormActivity.class);
                        intent.putExtra("file", file.getName());
                        intent.putExtra("type", type);
                        startActivity(intent);
                    }
                });
                rootLL.addView(button);
            }
        }
    }
}
