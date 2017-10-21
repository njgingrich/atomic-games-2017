package com.atomicobject.rts.model;

import org.json.simple.JSONObject;

public class TileResource {
    public Long id;
    public String type;
    public Long total;
    public Long value;

    public TileResource(JSONObject json) {
        id = (Long) json.get("id");
        type = (String) json.get("type");
        total = (Long) json.get("total");
        value = (Long) json.get("value");
    }
}
