package com.atomicobject.rts.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Tile {
    public Boolean visible;
    public Long x;
    public Long y;
    public Boolean blocked;
    public TileResource resource;
    public Unit[] units;

    public Tile(JSONObject json) {
        visible = (Boolean) json.get("visible");
        x = (Long) json.get("x");
        y = (Long) json.get("y");
        blocked = (Boolean) json.get("blocked");
        if (json.get("resources") != null) {
            resource = new TileResource((JSONObject) json.get("resources"));
        }

        JSONArray jsonUnits = (JSONArray) json.get("units");
        if (units == null) jsonUnits = new JSONArray();
        for (int i = 0; i < jsonUnits.size(); i++) {
            units[i] = new Unit((JSONObject) jsonUnits.get(i));
        }
    }
}
