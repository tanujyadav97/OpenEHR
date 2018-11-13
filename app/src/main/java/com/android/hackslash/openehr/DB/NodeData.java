package com.android.hackslash.openehr.DB;

import java.util.ArrayList;

public class NodeData {
    public String timestamp;
    public String archetype_name;
    public String context_id;
    public ArrayList<Field> field_data;

    public NodeData() {
    }

    public NodeData(String timestamp, String archetype_name, String context_id, ArrayList<Field> filed_data) {
        this.timestamp = timestamp;
        this.archetype_name = archetype_name;
        this.context_id = context_id;
        this.field_data = filed_data;
    }
}
