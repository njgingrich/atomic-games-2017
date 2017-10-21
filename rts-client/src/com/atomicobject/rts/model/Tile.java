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

    public Tile(int x, int y, int rows) {
        visible = false;
        blocked = false;
        this.x = (long) (x - rows);
        this.y = (long) (y - rows);
    }

    public Tile(JSONObject o) {
        updateTile(o);
    }

    public void updateTile(JSONObject json) {
        visible = (Boolean) json.get("visible");
        x = (Long) json.get("x");
        y = (Long) json.get("y");
        if (json.get("resources") != null) {
            resource = new TileResource((JSONObject) json.get("resources"));
        }
        if (json.get("blocked")!= null) {
            blocked = (Boolean) json.get("blocked");
        }
        JSONArray jsonUnits = (JSONArray) json.get("units");
        if (units == null) jsonUnits = new JSONArray();
        for (int i = 0; i < jsonUnits.size(); i++) {
            units[i] = new Unit((JSONObject) jsonUnits.get(i));
        }
    }
}
