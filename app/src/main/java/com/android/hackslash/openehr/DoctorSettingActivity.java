package com.android.hackslash.openehr;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.ChildrenNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DoctorSettingActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    Map<String, Boolean> depts = new HashMap<>();
    LinearLayout docrootLL;
    ArrayList<Switch> seldept = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_setting);

        findViewById(R.id.doc_submit).setClickable(false);
        docrootLL = findViewById(R.id.docrootLL);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        mRef.child("depts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childs : dataSnapshot.getChildren()) {
                    depts.put(childs.getKey(), false);
                }
                getDoctorSelections();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        findViewById(R.id.doc_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Switch rb : seldept) {
                    depts.put(rb.getText().toString(), rb.isChecked());
                }
                mRef.child("doctors").child(user.getUid()).child("adls").setValue(depts);
                Toast.makeText(getApplicationContext(), "Changes made successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DoctorSettingActivity.this, DoctorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void getDoctorSelections() {
        mRef.child("doctors").child(user.getUid()).child("adls").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childs : dataSnapshot.getChildren()) {
                    if (childs.getValue().toString().equals("true"))
                        depts.put(childs.getKey(), true);
                }
                makeFields();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeFields() {
        Log.d("hjkh", "ggg");
        for (String key : depts.keySet()) {
            Switch rb = new Switch(this);
            rb.setText(key);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            rb.setChecked(depts.get(key));
            seldept.add(rb);
            docrootLL.addView(rb);
        }
        findViewById(R.id.doc_submit).setClickable(true);
    }

}
