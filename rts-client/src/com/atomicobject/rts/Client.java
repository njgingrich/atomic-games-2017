package com.atomicobject.rts;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Client {
	
	BufferedReader input;
	OutputStreamWriter out;
	LinkedBlockingQueue<Map<String, Object>> updates;
	Map<Long, Unit> units;
	GameMap map = new GameMap();

	public Client(Socket socket) {
		updates = new LinkedBlockingQueue<Map<String, Object>>();
		units = new HashMap<Long, Unit>();
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
			addUnitUpdate(unitUpdates);
			addTileUpdate(tileUpdates);
		}
	}

	private void addUnitUpdate(Collection<JSONObject> unitUpdates) {
		unitUpdates.forEach((unitUpdate) -> {
			Long id = (Long) unitUpdate.get("id");
			String type = (String) unitUpdate.get("type");
			if (!type.equals("base")) {
				units.put(id, new Unit(unitUpdate));
			}
		});
	}

	private void addTileUpdate(Collection<JSONObject> tileUpdates) {
		tileUpdates.forEach((tileUpdate) -> {
			Tile t = new Tile(tileUpdate);
			map.put(t.x, t.y, t);
		});
	}

	private void respondWithCommands() throws IOException {
		if (units.size() == 0) return;
		
		JSONArray commands = buildCommandList();		
		sendCommandListToServer(commands);
	}

	@SuppressWarnings("unchecked")
	private JSONArray buildCommandList() {
		String[] directions = {"N","E","S","W"};
		String direction = directions[(int) Math.floor(Math.random() * 4)];

		List<Tile> resourceTiles = getResourceLocations();

		Long[] unitIds = units.keySet().toArray(new Long[units.size()]);
		Long unitId = unitIds[(int) Math.floor(Math.random() * unitIds.length)];

		Map<String, Object> args = new HashMap<>();
		args.put("dir", direction);
		args.put("unit", unitId);
		Command move = new Command(Command.MOVE, args);
		List<Command> commands = new ArrayList<>();
		commands.add(move);
		return Command.create(commands);
	}

	private List<Tile> getResourceLocations() {
		List<Tile> tiles = new ArrayList<>();
		System.out.println("Looking for resources...");
		for (Tile[] row : map.rows) {
			for (Tile tile : row) {
				if (tile != null && tile.resource != null) {
					System.out.println("Found resource at [" + tile.x + ", " + tile.y + "]");
					tiles.add(tile);
				}
			}
		}
		return tiles;
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
