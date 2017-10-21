package com.atomicobject.rts;

import com.atomicobject.rts.model.Tile;

public class GameMap {
    private static int ROWS = 30;
    public Tile[][] rows;

    public GameMap() {
        rows = new Tile[ROWS * 2][ROWS * 2];
    }

    public Tile[] row(int row) {
        return rows[row];
    }

    public Tile get(int row, int col) {
        return rows[ROWS + row][ROWS + col];
    }

    public void put(Long row, Long col, Tile t) {
        // Base is actually at [ROWS,ROWS]
        rows[ROWS + row.intValue()][ROWS + col.intValue()] = t;
    }
}
