package com.atomicobject.rts;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public class Command {
    public static final String MOVE = "MOVE";
    public static final String GATHER = "GATHER";
    public static final String CREATE = "CREATE";
    public static final String SHOOT = "SHOOT";
    public static final String MELEE = "MELEE";
    public static final String IDENTIFY = "IDENTIFY";

    private String command;
    private Map<String, Object> args;

    public Command(String command, Map<String, Object> args) {
        this.command = command;
        this.args = args;
    }

    public static JSONArray create(List<Command> cmds) {
        JSONArray commands = new JSONArray();

        for (Command c : cmds) {
            JSONObject command = new JSONObject();
            command.put("command", c.command);
            for (String cmd : c.args.keySet()) {
                command.put(cmd, c.args.get(cmd));
            }
            commands.add(command);
        }
        return commands;
    }
}
