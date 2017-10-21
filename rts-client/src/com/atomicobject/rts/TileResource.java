package com.atomicobject.rts;

import org.json.simple.JSONObject;

public class TileResource {
    int id;
    String type;
    int total;
    int value;

    public TileResource(JSONObject json) {
        id = (int) json.get("id");
        type = (String) json.get("type");
        total = (int) json.get("total");
        value = (int) json.get("value");
    }
}
