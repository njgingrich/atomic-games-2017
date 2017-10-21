package com.atomicobject.rts;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Tile {
    Boolean visible;
    Long x;
    Long y;
    Boolean blocked;
    TileResource resource;
    Unit[] units;

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
