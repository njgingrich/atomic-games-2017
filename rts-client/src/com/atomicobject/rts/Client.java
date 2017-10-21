package com.atomicobject.rts;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.atomicobject.rts.model.Tile;
import com.atomicobject.rts.model.Unit;
import com.atomicobject.rts.pathfinding.AGNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Client {
	
	BufferedReader input;
	OutputStreamWriter out;
	LinkedBlockingQueue<Map<String, Object>> updates;
	Map<Long, Unit> units;
	Set<Long> gathering;
	Long player;
	Long score;
	GameMap map = new GameMap();

	public Client(Socket socket) {
		updates = new LinkedBlockingQueue<Map<String, Object>>();
		units = new HashMap<Long, Unit>();
		gathering = new HashSet<>();
		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		System.out.println("Starting client threads ...");
		new Thread(() -> readUpdatesFromServer()).start();
		new Thread(() -> runClientLoop()).start();
	}
	
	public void readUpdatesFromServer() {
		String nextLine;
		try {
			while ((nextLine = input.readLine()) != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> update = (Map<String, Object>) JSONValue.parse(nextLine.trim());
				for (String s : update.keySet()) {
					System.out.println("UPDATE: " + s + ", " + update.get(s));
				}
				updates.add(update);
			}
		} catch (IOException e) {
			// exit thread
		}		
	}

	public void runClientLoop() {
		System.out.println("Starting client update/command processing ...");
		try {
			while (true) {
				processUpdateFromServer();
				respondWithCommands();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeStreams();
	}

	private void processUpdateFromServer() throws InterruptedException {
		Map<String, Object> update = updates.take();
		if (update != null) {
			System.out.println("Processing udpate: " + update);
			@SuppressWarnings("unchecked")
			Collection<JSONObject> unitUpdates = (Collection<JSONObject>) update.get("unit_updates");
			Collection<JSONObject> tileUpdates = (Collection<JSONObject>) update.get("tile_updates");
			player = (Long) update.get("player");
			addUnitUpdate(unitUpdates);
			addTileUpdate(tileUpdates);
		}
	}

	private void addUnitUpdate(Collection<JSONObject> unitUpdates) {
		unitUpdates.forEach((unitUpdate) -> {
			Long id = (Long) unitUpdate.get("id");
			String type = (String) unitUpdate.get("type");
			Unit u = new Unit(unitUpdate);
			if (!type.equals("base")) {
				units.put(id, u);
			} else {
				score = u.resource;
				System.out.println("Set score to: " + score);
			}
			if ((Long) unitUpdate.get("player_id") != player) {
				map.putEnemy(u.x, u.y, u);
			}
		});
	}

	private void addTileUpdate(Collection<JSONObject> tileUpdates) {
		tileUpdates.forEach((tileUpdate) -> {
			Tile t = new Tile(tileUpdate);
			Tile curr = map.getTile(t.x, t.y);
			if (curr == null) {
				map.putTile(t.x, t.y, t);
			} else {
				curr.updateTile(tileUpdate);
			}
		});
	}

	private void respondWithCommands() throws IOException {
		if (units.size() == 0) return;
		
		JSONArray commands = buildCommandList();		
		sendCommandListToServer(commands);
	}

	@SuppressWarnings("unchecked")
	private JSONArray buildCommandList() {
		List<Tile> resourceTiles = getResourceLocations();
		List<Unit> enemies = getEnemyLocations();

		/**
		 * Strategy:
		 * - If unit has resource, return to base
		 * - If possible, gather a resource
		 * - Otherwise, if an enemy unit is in attack range, attack it
		 * -
		 * - if resources > 700:
		 * 	- if workerCount < 5, create a new
		 * 	- if scoutCount < 2, create a new
		 * 	- if tankCount < 1, create a new
		 * -
		 */

		// From the visible resources, collect what you can
		List<Command> collections = collectResources(resourceTiles);
		// Move the units who have collected back to the base
		returnToBase();
		scout();
		List<Command> toCreate = createUnits();
		List<Command> moves = getNextMoveForUnits();
		// goToResources(resourceTiles);

		List<Command> commands = new ArrayList<>();
		// commands.addAll(toCreate);
		// commands.add(move);
		commands.addAll(collections);
		commands.addAll(moves);
		return Command.create(commands);
	}

	private void scout() {
		String[] directions = {"N","E","S","W"};
		Long[] unitIds = units.keySet().toArray(new Long[units.size()]);

		for (int i = 0; i < 4; i++) {
			Long id = unitIds[i];
			Unit u = units.get(id);
			Map<String, Object> args = new HashMap<>();
			String direction = directions[i];
			switch (direction) {
				case "N":
					u.path = map.pathfindingMap.findPath(u.x.intValue(), u.y.intValue(), 0, -2);
					break;
				case "S":
					u.path = map.pathfindingMap.findPath(u.x.intValue(), u.y.intValue(), 0, 2);
					break;
				case "E":
					u.path = map.pathfindingMap.findPath(u.x.intValue(), u.y.intValue(), 2, 0);
					break;
				case "W":
					u.path = map.pathfindingMap.findPath(u.x.intValue(), u.y.intValue(), -2, 0);
					break;
			}
		}
	}

	private List<Command> getNextMoveForUnits() {
		List<Command> commands = new ArrayList<>();
		units.forEach((id, unit) -> {
			if (unit.path != null && !unit.path.isEmpty()) {
				Map<String, Object> args = new HashMap<>();
				AGNode first = unit.path.remove(0);
				System.out.println("Moving from [" + unit.x + ", " + unit.y + "] to [" + first.tile.x + ", " + first.tile.y + "]");
				args.put("dir", GameMap.getDirection(unit.x, unit.y, first.tile.x, first.tile.y));
				args.put("unit", id);
				commands.add(new Command(Command.MOVE, args));
			}
		});
		return commands;
	}

	private List<Command> createUnits() {
		Long tempScore = score;
		List<Command> commands = new ArrayList<>();
		while (tempScore > 500) {
			if (tempScore - Unit.WORKER_COST > 500) {
				Map<String, Object> args = new HashMap<>();
				args.put("type", Unit.WORKER);
				commands.add(new Command(Command.CREATE, args));
				tempScore -= Unit.WORKER_COST;
				System.out.println("Creating new worker (new score=" + tempScore + ")");
				continue;
			}
			if (tempScore - Unit.TANK_COST > 400) {
				Map<String, Object> args = new HashMap<>();
				args.put("type", Unit.TANK);
				commands.add(new Command(Command.CREATE, args));
				tempScore -= Unit.TANK_COST;
				System.out.println("Creating new tank (new score=" + tempScore + ")");
				continue;
			}
			if (tempScore - Unit.SCOUT_COST > 500) {
				Map<String, Object> args = new HashMap<>();
				args.put("type", Unit.SCOUT);
				commands.add(new Command(Command.CREATE, args));
				tempScore -= Unit.SCOUT_COST;
				System.out.println("Creating new scout (new score=" + tempScore + ")");
				continue;
			}
			break;
		}
		return commands;
	}

	private void returnToBase() {
		gathering.forEach(id -> {
			Unit worker = units.get(id);
			List<AGNode> path = map.pathfindingMap.findPath(worker.x.intValue(), worker.y.intValue(),0,0);
			worker.path = path;
		});
	}

	private void goToResources(List<Tile> resources) {
		resources.forEach(resource -> {
			// get an available worker
			List<Unit> available = units.values().stream()
												 .filter(unit -> unit.type.equals("worker"))
												 .filter(unit -> !gathering.contains(unit.id))
												 .collect(Collectors.toList());

			for (Unit worker : available) {
				// calculate path from worker to resource
				List<AGNode> path = map.pathfindingMap.findPath(worker.x.intValue(),
																worker.y.intValue(),
																resource.x.intValue(),
																resource.y.intValue());
				// store path in worker
				worker.path = path;
				gathering.add(worker.id);
			}
		});
	}

	private List<Command> collectResources(List<Tile> resources) {
		List<Command> commands = new ArrayList<>();

		units.forEach((id, unit) -> {
			// Don't reassign units already gathering
			if (gathering.contains(id)) return;
			resources.forEach(resource -> {
				if (Unit.withinMelee(unit.x, unit.y, resource.x, resource.y)) {
					System.out.println("Dispatching unit " + id + " to collect resource at [" + resource.x + ", " + resource.y + "]");
					gathering.add(id);
					Map<String, Object> args = new HashMap<>();
					System.out.println("Direction for [" + unit.x + ", " + unit.y + "] to [" + resource.x + ", " + resource.y + "] is " + GameMap.getDirection(unit.x, unit.y, resource.x, resource.y));
					args.put("dir", GameMap.getDirection(unit.x, unit.y, resource.x, resource.y));
					args.put("unit", id);
					Command gather = new Command(Command.GATHER, args);
					commands.add(gather);
				}
			});
		});
		return commands;
	}

	private List<Tile> getResourceLocations() {
		List<Tile> tiles = new ArrayList<>();
		System.out.println("Looking for resources...");
		for (Tile[] row : map.tiles) {
			for (Tile tile : row) {
				if (tile != null && tile.resource != null) {
					System.out.println("Found resource at [" + tile.x + ", " + tile.y + "]");
					tiles.add(tile);
				}
			}
		}
		return tiles;
	}

	private List<Unit> getEnemyLocations() {
		List<Unit> enemies = new ArrayList<>();
		System.out.println("Looking for enemies...");
		for (Unit[] row : map.enemies) {
			for (Unit u : row) {
				if (u != null) {
					System.out.println("Found " + u.type + " at [" + u.x + ", " + u.y + "]");
					enemies.add(u);
				}
			}
		}
		return enemies;
	}

	@SuppressWarnings("unchecked")
	private void sendCommandListToServer(JSONArray commands) throws IOException {
		JSONObject container = new JSONObject();
		container.put("commands", commands);
		System.out.println("Sending commands: " + container.toJSONString());
		out.write(container.toJSONString());
		out.write("\n");
		out.flush();
	}

	private void closeStreams() {
		closeQuietly(input);
		closeQuietly(out);
	}

	private void closeQuietly(Closeable stream) {
		try {
			stream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
