package com.atomicobject.rts;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Tile {
    boolean visible;
    int x;
    int y;
    boolean blocked;
    TileResource[] resources;
    Unit[] units;

    public Tile(JSONObject json) {
        visible = (boolean) json.get("visible");
        x = (int) json.get("x");
        y = (int) json.get("y");
        blocked = (boolean) json.get("blocked");
        JSONArray res = (JSONArray) json.get("resources");
        for (int i = 0; i < res.size(); i++) {
            resources[i] = new TileResource((JSONObject) res.get(i));
        }
        JSONArray jsonUnits = (JSONArray) json.get("units");
        for (int i = 0; i < jsonUnits.size(); i++) {
            units[i] = new Unit((JSONObject) jsonUnits.get(i));
        }
    }
}
