package com.remrin.sync;

import java.util.List;

/**
 * A named group of server command entries, displayed as a category in the client's Server tab.
 * Defined in the common source set so eun_carpet can reference it as a compile dependency.
 */
public class ServerCommandGroup {
	private final String name;
	private final List<ServerCommandEntry> commands;

	public ServerCommandGroup(String name, List<ServerCommandEntry> commands) {
		this.name = name;
		this.commands = commands;
	}

	public String getName() { return name; }
	public List<ServerCommandEntry> getCommands() { return commands; }
}
