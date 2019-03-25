package com.android.hackslash.openehr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DoctorActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    ArrayList<String> depts = new ArrayList<>();
    LinearLayout docRootLL1;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        docRootLL1 = findViewById(R.id.docrootLL1);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        mRef.child("doctors").child(user.getUid()).child("adls").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childs : dataSnapshot.getChildren()) {
                    if (childs.getValue().toString().equals("true"))
                        depts.add(childs.getKey());
                }
                makeFields();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.doctor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_setting:
                startActivity(new Intent(DoctorActivity.this, DoctorSettingActivity.class));
                return true;
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeFields() {
        for (String dept : depts) {
            final LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setPadding(0, 10, 0, 0);

            TextView tv = new TextView(this);
            tv.setText(dept);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            ll.addView(tv);

            mRef.child("depts").child(dept).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childs : dataSnapshot.getChildren()) {
                        final String filename = childs.getKey().replace('+', '.');
                        String type;
                        if (filename.contains("ACTION"))
                            type = "ACTION";
                        else if (filename.contains("OBSERVATION"))
                            type = "OBSERVATION";
                        else if (filename.contains("EVALUATION"))
                            type = "EVALUATION";
                        else type = "INSTRUCTION";

                        final String type1 = type;
                        final String buttonName = filename.replace("openEHR-EHR-", "")
                                .replace(".adl", "").replace(type + ".", "");

                        Button button = new Button(getApplicationContext());

                        button.setText(buttonName);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DoctorActivity.this);
                                builder.setTitle("Patient Uid");

                                final EditText input = new EditText(getApplicationContext());
                                builder.setView(input);

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String m_Text = input.getText().toString();
                                        if (m_Text.equals("")) {
                                            dialog.cancel();
                                            Toast.makeText(getApplicationContext(), "Patient ID should not be empty.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Intent intent = new Intent(DoctorActivity.this, FormActivity.class);
                                            intent.putExtra("file", filename);
                                            intent.putExtra("type", type1);
                                            intent.putExtra("title", buttonName);
                                            intent.putExtra("uid", m_Text);
                                            startActivity(intent);
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            }
                        });
                        ll.addView(button);
                    }
                    docRootLL1.addView(ll);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), signin_activity.class);
                        final SharedPreferences.Editor editor = getSharedPreferences("ehrData", MODE_PRIVATE).edit();
                        editor.putBoolean("isDoctor", false);
                        editor.apply();
                        finish();
                        startActivity(intent);
                    }
                });
    }
}
