package com.android.hackslash.openehr.DB;

public class Field {
    public String name;
    public Object value;

    public Field() {
    }

    public Field(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
