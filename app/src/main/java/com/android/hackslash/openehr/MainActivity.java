package com.android.hackslash.openehr;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public LinearLayout rootLL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootLL = findViewById(R.id.parentLL);
        makeForm();
    }

    private void makeForm(){
        Parser.getData();
        dfs(-1,0);
    }

    private void dfs(int root,int padding){
        if(Parser.graph.containsKey(root)){
            if(root==-1){
                ArrayList<Integer> child = Parser.graph.get(root);
                for(int i=0;i<child.size();i++){
                    dfs(child.get(i),padding);
                }
            }else{
                addCluster(true, root, padding);

                ArrayList<Integer> child = Parser.graph.get(root);
                for(int i=0;i<child.size();i++){
                    dfs(child.get(i),padding+40);
                }
            }
        }else{
            addItem(root, padding);
        }
    }

    private void addItem(int root, int padding){
        ArrayList<String> data = Parser.childData.get(root);
        switch (data.get(0)){
            case "DV_QUANTITY":
                addQuantity(true,root, data, padding);
                break;
            case "DV_TEXT":
                addText(true,root, padding);
                break;
            case "DV_COUNT":
                addCount(true,root, data, padding);
                break;
            case "DV_ORDINAL":
                addOrdinal(true,root, data, padding);
                break;
            case "DV_DATE_TIME":
                addDateTime(true,root, padding);
                break;
            case "DV_BOOLEAN":
                addBoolean(true,root, padding);
                break;
            case "DV_DURATION":
                addDuration(true,root, padding);
                break;
            case "DV_PARSABLE":
                addParsable(true,root, data, padding);
                break;
            case "DV_CODED_TEXT":
                addCodedText(true,root, data, padding);
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
        for(int i=1;i<data.size();i++){
            if(data.get(i).startsWith("DV_")){
                ArrayList<String> subData = new ArrayList<>();
                subData.add(data.get(i));
                int j;
                for(j=i+1;j<data.size()&&!data.get(j).startsWith("DV_");j++){
                    subData.add(data.get(j));
                }

                switch (data.get(i)){
                    case "DV_QUANTITY":
                        addQuantity(false, root, subData, padding+40);
                        break;
                    case "DV_TEXT":
                        addText(false, root, padding+40);
                        break;
                    case "DV_COUNT":
                        addCount(false, root, subData, padding+40);
                        break;
                    case "DV_ORDINAL":
                        addOrdinal(false, root, subData, padding+40);
                        break;
                    case "DV_DATE_TIME":
                        addDateTime(false, root, padding+40);
                        break;
                    case "DV_BOOLEAN":
                        addBoolean(false, root, padding+40);
                        break;
                    case "DV_DURATION":
                        addDuration(false, root, padding+40);
                        break;
                    case "DV_PARSABLE":
                        addParsable(false, root, subData, padding+40);
                        break;
                    case "DV_CODED_TEXT":
                        addCodedText(false, root, subData, padding+40);
                        break;
                    default:
                        System.out.println("no match");
                }

                i=j-1;
            }
        }
    }

    private void addQuantity(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(this);
        if (!data.get(1).equals("")){
            int num = (int)Float.parseFloat(data.get(1));
            np.setMinValue(num);
        }
        else
            np.setMinValue(0);
        if (!data.get(2).equals("")){
            int num = (int)Float.parseFloat(data.get(2));
            np.setMaxValue(num);
        }
        else
            np.setMaxValue(200);
        TextView unit = new TextView(this);
        unit.setText(data.get(3));
        ll1.addView(np);
        ll1.addView(unit);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);
    }

    private void addText(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        tv.setLayoutParams(lp1);
        EditText et = new EditText(this);
        et.setLayoutParams(lp1);
        et.setPadding(0,0,0,30);
        if (type)
            ll.addView(tv);
        ll.addView(et);
        rootLL.addView(ll);
    }

    private void addCount(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(this);
        if (data.size()>1&&!data.get(1).equals("")){
            int num = (int)Float.parseFloat(data.get(1));
            np.setMinValue(num);
        }
        else
            np.setMinValue(0);
        if (data.size()>1&&!data.get(2).equals("")){
            int num = (int)Float.parseFloat(data.get(2));
            np.setMaxValue(num);
        }
        else
            np.setMaxValue(100);
        ll1.addView(np);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);
    }

    private void addOrdinal(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for(int i=1;i<data.size();i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);
    }

    private void addDateTime(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout ll1  = new LinearLayout(this);
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
    }

    private void addBoolean(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        ll1.setLayoutParams(lp1);
        Switch sw = new Switch(this);
        ll1.addView(sw);
        if (type)
            ll.addView(tv);
        ll.addView(ll1);
        rootLL.addView(ll);
    }

    private void addDuration(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
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
    }

    private void addParsable(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for(int i=1;i<data.size();i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);
    }

    private void addCodedText(boolean type, int root, ArrayList<String> data, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(this);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for(int i=1;i<data.size();i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sp.setAdapter(spinnerArrayAdapter);
        if (type)
            ll.addView(tv);
        ll.addView(sp);
        rootLL.addView(ll);
    }

    private void addCluster(boolean type, int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        if (type)
            ll.addView(tv);
        rootLL.addView(ll);
    }

    private void addChoiceName(int root, int padding) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding,10,0,0);
        TextView tv = new TextView(this);
        tv.setText(Parser.childNames.get(root) + "\n(Use only one)");
        tv.setTextColor(getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize( getResources().getDimension(R.dimen.fieldname));
        ll.addView(tv);
        rootLL.addView(ll);
    }
}
