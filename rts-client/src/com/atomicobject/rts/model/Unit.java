package com.atomicobject.rts.model;
import com.atomicobject.rts.pathfinding.AGNode;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Unit {
    public static final int WORKER_COST = 100;
    public static final int SCOUT_COST = 130;
    public static final int TANK_COST = 150;
    public static final String TANK = "tank";
    public static final String WORKER = "worker";
    public static final String SCOUT = "scout";

	public Long resource;
	public String attackType;
	public Long health;
	public Long range;
	public Long attackDamage;
	public String type;
	public Long speed;
	public Long attackCooldown;
	public Boolean canAttack;
	public Long playerId;
	public Long x;
	public Long y;
	public Long id;
	public String status;
	public Long attackCooldownDuration;
	public List<AGNode> path;

	public Unit(JSONObject json) {
		resource = (Long) json.get("resource");
		attackType = (String) json.get("attack_type");
		health = (Long) json.get("health");
		range = (Long) json.get("range");
		attackDamage = (Long) json.get("attack_damage");
		type = (String) json.get("type");
		speed = (Long) json.get("speed");
		attackCooldown = (Long) json.get("attack_cooldown");
		canAttack = (Boolean) json.get("can_attack");
		playerId = (Long) json.get("player_id");
		x = (Long) json.get("x");
		y = (Long) json.get("y");
		id = (Long) json.get("id");
		status = (String) json.get("status");
		attackCooldownDuration = (Long) json.get("attack_cooldown_duration");

		path = new LinkedList<>();
	}

	public static boolean withinMelee(Long row1, Long col1, Long row2, Long col2) {
	    boolean aboveOrBelow = ((row2 - row1 == 1) || (row2 - row1 == -1)) && (col2 - col1 == 0);
	    boolean leftOrRight  = ((col2 - col1 == 1) || (col2 - col1 == -1)) && (row2 - row1 == 0);
        boolean onTopOf = onTopOf(row1, col1, row2, col2);

        return onTopOf || aboveOrBelow || leftOrRight;
	}

	public static boolean withinMelee(Unit unit1, Unit unit2) {
	    return withinMelee(unit1.x, unit1.y, unit2.x, unit2.y);
    }

	public static boolean onTopOf(Long row1, Long col1, Long row2, Long col2) {
        return (row1.equals(row2)) && (col1.equals(col2));
    }

    public static boolean onTopOf(Unit unit1, Unit unit2) {
	    return onTopOf(unit1.x, unit1.y, unit2.x, unit2.y);
    }

    public static boolean withinTankRange(Long row1, Long col1, Long row2, Long col2) {
        boolean within2rows = ((row2 - row1) >= 0) && ((row2 - row1) <= 2);
        boolean within2cols = ((col2 - col1) >= 0) && ((col2 - col1) <= 2);
        return within2rows && within2cols;
    }

    public static boolean withinTankRange(Unit unit1, Unit unit2) {
        return withinTankRange(unit1.x, unit1.y, unit2.x, unit2.y);
    }
}
