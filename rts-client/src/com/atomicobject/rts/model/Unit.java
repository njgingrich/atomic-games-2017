package com.atomicobject.rts.model;
import org.json.simple.JSONObject;

public class Unit {
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
	}
}
