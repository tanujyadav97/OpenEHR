package com.android.hackslash.openehr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "Navigation Activity";
    private DownloadFiles downFile = new DownloadFiles();
    public File[] files;
    LinearLayout rootLL;
    ScrollView homeSV, rootSV;
    Button updateFiles;
    TextView availfiles;
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setContentView(R.layout.activity_navigation);

        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(NavigationActivity.this, signin_activity.class));
                }
            }
        };
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

        homeSV = findViewById(R.id.homeSV);
        rootLL = findViewById(R.id.rootLL);
        rootSV = findViewById(R.id.rootSV);
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
            getSupportActionBar().setTitle("MedEForm");
            rootSV.setVisibility(View.GONE);
            homeSV.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_action) {
            getSupportActionBar().setTitle("Action");
            homeSV.setVisibility(View.GONE);
            rootSV.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("ACTION");
        } else if (id == R.id.nav_evaluation) {
            getSupportActionBar().setTitle("Evaluation");
            homeSV.setVisibility(View.GONE);
            rootSV.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("EVALUATION");
        } else if (id == R.id.nav_observation) {
            getSupportActionBar().setTitle("Observation");
            homeSV.setVisibility(View.GONE);
            rootSV.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("OBSERVATION");
        } else if (id == R.id.nav_instruction) {
            getSupportActionBar().setTitle("Instruction");
            homeSV.setVisibility(View.GONE);
            rootSV.setVisibility(View.VISIBLE);
            rootLL.removeAllViews();
            addViews("INSTRUCTION");
        } else if (id == R.id.nav_settings) {
            getSupportActionBar().setTitle("Settings");
            homeSV.setVisibility(View.GONE);
            rootSV.setVisibility(View.GONE);
        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFileNames() {
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data", "com.android.hackslash.openehr");
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

                final String buttonName = file.getName().replace("openEHR-EHR-", "")
                        .replace(".adl", "").replace(type + ".", "");
                button.setText(buttonName);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NavigationActivity.this, FormActivity.class);
                        intent.putExtra("file", file.getName());
                        intent.putExtra("type", type);
                        intent.putExtra("title", buttonName);
                        startActivity(intent);
                    }
                });
                rootLL.addView(button);
            }
        }
    }

    private void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), signin_activity.class);
                        finish();
                        startActivity(intent);
                    }
                });
    }
}
