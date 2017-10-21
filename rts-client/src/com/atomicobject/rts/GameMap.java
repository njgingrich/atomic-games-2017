package com.atomicobject.rts;

import com.atomicobject.rts.model.Tile;
import com.atomicobject.rts.model.Unit;
import com.atomicobject.rts.pathfinding.AGFactory;
import com.atomicobject.rts.pathfinding.AGNode;
import com.atomicobject.rts.pathfinding.Map;
import com.atomicobject.rts.pathfinding.NodeFactory;


public class GameMap {
    private static int ROWS = 30;
    public Tile[][] tiles;
    public Unit[][] enemies;
    public Map<AGNode> pathfindingMap;
    public NodeFactory factory;
    
    public GameMap() {
        tiles = new Tile[ROWS * 2][ROWS * 2];
        enemies = new Unit[ROWS * 2][ROWS * 2];
        factory = new AGFactory();
        pathfindingMap = new Map<AGNode>(tiles, ROWS * 2, ROWS *2, factory);
    }

    public Tile[] tileRow(int row) {
        return tiles[row];
    }

    public Unit[] unitRow(int row) {
        return enemies[row];
    }

    public Tile getTile(int row, int col) {
        return tiles[ROWS + row][ROWS + col];
    }

    public void putTile(Long row, Long col, Tile t) {
        // Base is actually at [ROWS,ROWS]
        tiles[ROWS + row.intValue()][ROWS + col.intValue()] = t;
    }

    public Unit getEnemy(int row, int col) {
        return enemies[ROWS + row][ROWS + col];
    }

    public void putEnemy(Long row, Long col, Unit u) {
        enemies[ROWS + row.intValue()][ROWS + col.intValue()] = u;
    }

    public static String getDirection(Long rowFrom, Long colFrom, Long rowTo, Long colTo) {
        if ((rowFrom - rowTo == 1) && (colFrom - colTo == 0)) return "N";
        else if ((rowFrom - rowTo == -1) && (colFrom - colTo == 0)) return "S";
        else if ((rowFrom - rowTo == 0) && (colFrom - colTo == 1)) return "E";
        else if ((rowFrom - rowTo == 0) && (colFrom - colTo == -1)) return "W";
        else return "N"; // ew
    }
}
