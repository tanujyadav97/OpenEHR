package com.hackslash.medeformlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Pair;
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

public class GenerateForm {
    private String path,filename,type;
    Activity activity;
    private LinearLayout rootLL;
    private Context context;
    ArrayList<Pair<String, Pair<String, View>>> views = new ArrayList<>();
    private ArrayList<Field> fields = new ArrayList<>();

    public GenerateForm(Activity mactivity, Context mcontext, String Path, String Filename, String Type, int LLID){
        path = Path;
        filename = Filename;
        type = Type;
        rootLL = mactivity.findViewById(LLID);
        context = mcontext;
        activity = mactivity;
        fields.clear();
    }

    public void makeForm() {
        Parser.getData(filename, type, path);
        dfs(-1, 0);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(Color.BLUE);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(Color.BLUE);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(context);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(context);
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
        TextView unit = new TextView(context);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        EditText et = new EditText(context);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(context);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(context);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(context);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        View vw = tv;
        LinearLayout ll1 = new LinearLayout(context);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        final LayoutInflater factory = activity.getLayoutInflater();
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(context);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        ll1.setLayoutParams(lp1);
        Switch sw = new Switch(context);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tv.setLayoutParams(lp1);
        LinearLayout ll1 = new LinearLayout(context);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(lp1);
        NumberPicker np = new NumberPicker(context);
        np.setMinValue(0);
        np.setMaxValue(100);
        Spinner sp = new Spinner(context);
        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("yr");
        spinnerArray.add("mth");
        spinnerArray.add("wk");
        spinnerArray.add("day");
        spinnerArray.add("hr");
        spinnerArray.add("min");
        spinnerArray.add("sec");

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(context);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        Spinner sp = new Spinner(context);
        ArrayList<String> spinnerArray = new ArrayList<>();
        for (int i = 1; i < data.size(); i++)
            spinnerArray.add(data.get(i));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
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
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root));
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
        if (type)
            ll.addView(tv);
        rootLL.addView(ll);
    }

    private void addChoiceName(int root, int padding) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(padding, 10, 0, 0);
        TextView tv = new TextView(context);
        tv.setText(Parser.childNames.get(root) + "\n(Use only one)");
        tv.setTextColor(context.getResources().getColor(R.color.fieldname));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(context.getResources().getDimension(R.dimen.fieldname));
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
}
