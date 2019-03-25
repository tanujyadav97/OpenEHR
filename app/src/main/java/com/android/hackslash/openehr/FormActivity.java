package com.android.hackslash.openehr;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.hackslash.openehr.DB.Field;
import com.android.hackslash.openehr.DB.NodeData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FormActivity extends AppCompatActivity {
    public LinearLayout rootLL;
    ArrayList<Pair<String, Pair<String, View>>> views = new ArrayList<>();
    private DatabaseReference mDatabase;
    private FirebaseFirestore mFirestore;
    private FirebaseUser account;
    private String filename, type, title;
    private ArrayList<Field> fields = new ArrayList<>();
    private String patientUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirestore = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
//                    NodeData note = noteDataSnapshot.getValue(NodeData.class);
//                    System.out.println(note.archetype_name);
//                    System.out.println(note.timestamp);
//                    System.out.println(note.context_id);
//                    System.out.println(note.field_data.get(0).name);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        account = FirebaseAuth.getInstance().getCurrentUser();

        rootLL = findViewById(R.id.parentLL);

        Intent intent = getIntent();
        filename = intent.getStringExtra("file");
        type = intent.getStringExtra("type");
        title = intent.getStringExtra("title");
        patientUserid = intent.getStringExtra("uid");
        if (patientUserid.equals(""))
            patientUserid = account.getUid();

        getSupportActionBar().setTitle(title);

        makeForm(filename, type);
    }

    private void makeForm(String filename, String type) {
        Parser.getData(filename, type);
        dfs(-1, 0);
//        Parser.getData(filename,type,"Android/data/com.android.hackslash.openehr");

        Button submit = new Button(this);
        submit.setText("Submit");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fields.clear();
                readData();
            }
        });
        rootLL.addView(submit);
    }

    private void dfs(int root, int padding) {
        if (Parser.graph.containsKey(root)) {
            if (root == -1) {
                ArrayList<Integer> child = Parser.graph.get(root);
                for (int i = 0; i < child.size(); i++) {
                    dfs(child.get(i), padding);
                }
            } else {
                addCluster(true, root, padding);

                ArrayList<Integer> child = Parser.graph.get(root);
                for (int i = 0; i < child.size(); i++) {
                    dfs(child.get(i), padding + 40);
                }
            }
        } else {
            addItem(root, padding);
        }
    }

    private void addItem(int root, int padding) {
        ArrayList<String> data = Parser.childData.get(root);
        switch (data.get(0)) {
            case "DV_QUANTITY":
                addQuantity(true, root, data, padding);
                break;
            case "DV_TEXT":
                addText(true, root, padding);
                break;
            case "DV_COUNT":
                addCount(true, root, data, padding);
                break;
            case "DV_ORDINAL":
                addOrdinal(true, root, data, padding);
                break;
            case "DV_DATE_TIME":
                addDateTime(true, root, padding);
                break;
            case "DV_BOOLEAN":
                addBoolean(true, root, padding);
                break;
            case "DV_DURATION":
                addDuration(true, root, padding);
                break;
            case "DV_PARSABLE":
                addParsable(true, root, data, padding);
                break;
            case "DV_CODED_TEXT":
                addCodedText(true, root, data, padding);
                break;
            case "DV_CHOICE":
                addChoice(root, data, padding);
                break;
            default:
                System.out.println("no match");
        }
    }

    private void addChoice(int root, ArrayList<String> data, int padding) {
        addChoiceName(root, padding);
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).startsWith("DV_")) {
                ArrayList<String> subData = new ArrayList<>();
                subData.add(data.get(i));
                int j;
                for (j = i + 1; j < data.size() && !data.get(j).startsWith("DV_"); j++) {
                    subData.add(data.get(j));
                }

                switch (data.get(i)) {
                    case "DV_QUANTITY":
                        addQuantity(false, root, subData, padding + 40);
                        break;
                    case "DV_TEXT":
                        addText(false, root, padding + 40);
                        break;
                    case "DV_COUNT":
                        addCount(false, root, subData, padding + 40);
                        break;
                    case "DV_ORDINAL":
                        addOrdinal(false, root, subData, padding + 40);
                        break;
                    case "DV_DATE_TIME":
                        addDateTime(false, root, padding + 40);
                        break;
                    case "DV_BOOLEAN":
                        addBoolean(false, root, padding + 40);
                        break;
                    case "DV_DURATION":
                        addDuration(false, root, padding + 40);
                        break;
                    case "DV_PARSABLE":
                        addParsable(false, root, subData, padding + 40);
                        break;
                    case "DV_CODED_TEXT":
                        addCodedText(false, root, subData, padding + 40);
                        break;
                    default:
                        System.out.println("no match");
                }

                i = j - 1;
            }
        }
    }

    private void addQuantity(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(this);
        if (!data.get(1).equals("")) {
            int num = (int) Float.parseFloat(data.get(1));
            np.setMinValue(num);
        } else
            np.setMinValue(0);
        if (!data.get(2).equals("")) {
            int num = (int) Float.parseFloat(data.get(2));
            np.setMaxValue(num);
        } else
            np.setMaxValue(200);
        TextView unit = new TextView(this);
        unit.setText(data.get(3));
        ll1.addView(np);
        ll1.addView(unit);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("NumberPicker", new Pair(tv.getText(), np)));
        else
            views.add(new Pair("NumberPicker", new Pair(tv.getText() + " Quantity", np)));
    }

    private void addText(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        EditText et = new EditText(this);
        et.setLayoutParams(lp1);
        et.setPadding(0, 0, 0, 30);
        if (type)
            ll.addView(tv);
        ll.addView(et);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("EditText", new Pair(tv.getText(), et)));
        else
            views.add(new Pair("EditText", new Pair(tv.getText() + " text", et)));
    }

    private void addCount(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(this);
        if (data.size() > 1 && !data.get(1).equals("")) {
            int num = (int) Float.parseFloat(data.get(1));
            np.setMinValue(num);
        } else
            np.setMinValue(0);
        if (data.size() > 1 && !data.get(2).equals("")) {
            int num = (int) Float.parseFloat(data.get(2));
            np.setMaxValue(num);
        } else
            np.setMaxValue(100);
        ll1.addView(np);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("NumberPicker", new Pair(tv.getText(), np)));
        else
            views.add(new Pair("NumberPicker", new Pair(tv.getText() + " Count", np)));
    }

    private void addOrdinal(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("Spinner", new Pair(tv.getText(), sp)));
        else
            views.add(new Pair("Spinner", new Pair(tv.getText() + " Ordinal", sp)));
    }

    private void addDateTime(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        View vw = tv;
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        final LayoutInflater factory = getLayoutInflater();
        final View view = factory.inflate(R.layout.date_time, null);
        LinearLayout datell = view.findViewById(R.id.datelinear);
        DatePicker dp = view.findViewById(R.id.datep);
        TimePicker tp = view.findViewById(R.id.timep);
        datell.removeAllViews();
        ll1.addView(dp);
        ll1.addView(tp);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);

        if (type) {
            views.add(new Pair("DatePicker", new Pair(tv.getText() + " Date", dp)));
            views.add(new Pair("TimePicker", new Pair(tv.getText() + " Time", tp)));
        } else {
            views.add(new Pair("DatePicker", new Pair(tv.getText() + " Date Datetime", dp)));
            views.add(new Pair("TimePicker", new Pair(tv.getText() + " Time Datetime", tp)));
        }
    }

    private void addBoolean(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        ll1.setLayoutParams(lp1);
        Switch sw = new Switch(this);
        ll1.addView(sw);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("Switch", new Pair(tv.getText(), sw)));
        else
            views.add(new Pair("Switch", new Pair(tv.getText() + " Boolean", sw)));
    }

    private void addDuration(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(this);
        np.setMinValue(0);
        np.setMaxValue(100);
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("yr");
        spinnerArray.add("mth");
        spinnerArray.add("wk");
        spinnerArray.add("day");
        spinnerArray.add("hr");
        spinnerArray.add("min");
        spinnerArray.add("sec");

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        ll1.addView(np);
        ll1.addView(sp);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);

        if (type) {
            views.add(new Pair("NumberPicker", new Pair(tv.getText() + " Value", np)));
            views.add(new Pair("Spinner", new Pair(tv.getText() + " Unit", sp)));
        } else {
            views.add(new Pair("NumberPicker", new Pair(tv.getText() + " Value Duration", np)));
            views.add(new Pair("Spinner", new Pair(tv.getText() + " Unit Duration", sp)));
        }
    }

    private void addParsable(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("Spinner", new Pair(tv.getText(), sp)));
        else
            views.add(new Pair("Spinner", new Pair(tv.getText() + " Parsable", sp)));
    }

    private void addCodedText(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);

        if (type)
            views.add(new Pair("Spinner", new Pair(tv.getText(), sp)));
        else
            views.add(new Pair("Spinner", new Pair(tv.getText() + " Codedtext", sp)));
    }

    private void addCluster(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        if (type)
            ll.addView(tv);
        rootLL.addView(ll);
    }

    private void addChoiceName(int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root) + "\n(Use only one)");
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(getResources().getDimension(R.dimen.fieldname));
        ll.addView(tv);
        rootLL.addView(ll);
    }

    public void readData() {
        for (int i = 0; i < views.size(); i++) {
            switch (views.get(i).first) {
                case "NumberPicker":
                    readNumberPicker(views.get(i).second);
                    break;
                case "EditText":
                    readEditText(views.get(i).second);
                    break;
                case "Spinner":
                    readSpinner(views.get(i).second);
                    break;
                case "DatePicker":
                    readDatePicker(views.get(i).second);
                    break;
                case "TimePicker":
                    readTimePicker(views.get(i).second);
                    break;
                case "Switch":
                    readSwitch(views.get(i).second);
                    break;
                default:

            }
        }

//        addDatatoFirebaseRealtime();
        addDatatoFirestore();
    }

    private void readSwitch(Pair<String, View> data) {
        Switch sw = (Switch) data.second;
        Log.d("FormData :", data.first + " " + sw.isChecked());
        fields.add(new Field(data.first, sw.isChecked() + ""));
    }

    private void readTimePicker(Pair<String, View> data) {
        TimePicker tp = (TimePicker) data.second;
        Log.d("FormData :", data.first + " " + tp.getHour() + ":" + tp.getMinute());
        fields.add(new Field(data.first, tp.getHour() + ":" + tp.getMinute()));
    }

    private void readDatePicker(Pair<String, View> data) {
        DatePicker dp = (DatePicker) data.second;
        Log.d("FormData :", data.first + " " + dp.getDayOfMonth() + "-" + (dp.getMonth() + 1) + "-" + dp.getYear());
        fields.add(new Field(data.first, dp.getDayOfMonth() + "-" + (dp.getMonth() + 1) + "-" + dp.getYear()));
    }

    private void readSpinner(Pair<String, View> data) {
        Spinner sp = (Spinner) data.second;
        Log.d("FormData :", data.first + " " + sp.getSelectedItem().toString());
        fields.add(new Field(data.first, sp.getSelectedItem().toString()));
    }

    private void readEditText(Pair<String, View> data) {
        EditText et = (EditText) data.second;
        Log.d("FormData :", data.first + " " + et.getText());
        fields.add(new Field(data.first, et.getText().toString()));
    }

    private void readNumberPicker(Pair<String, View> data) {
        NumberPicker np = (NumberPicker) data.second;
        Log.d("FormData :", data.first + " " + np.getValue());
        fields.add(new Field(data.first, np.getValue() + ""));
    }

    private void addDatatoFirebaseRealtime() {
        String uid = mDatabase.push().getKey();
        String ts = ((Long) System.currentTimeMillis()).toString();
        NodeData mNode = new NodeData(ts, filename, "tanuj", fields);
        mDatabase.child(uid).setValue(mNode, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(getApplicationContext(), "Database Updated Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to update database.", Toast.LENGTH_SHORT).show();
                    Log.e("FormActivity", error.toString());
                }
            }
        });
    }

    private void addDatatoFirestore() {
        Map<String, String> fieldsmap = getMapfromList();
        String ts = ((Long) System.currentTimeMillis()).toString();

        mFirestore.collection("EHR").document(patientUserid).collection(filename)
                .document(ts).set(fieldsmap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Database Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Unable to update database.", Toast.LENGTH_SHORT).show();
                        Log.e("FormActivity", e.toString());
                    }
                });
    }

    private Map<String, String> getMapfromList() {
        Map<String, String> tmp = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            tmp.put(fields.get(i).name, fields.get(i).value);
        }
        return tmp;
    }
}
