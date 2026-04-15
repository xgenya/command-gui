package com.remrin.sync;

import java.util.List;

/**
 * A single executable command entry in the server-side command preset.
 * Defined in the common source set so eun_carpet can reference it as a compile dependency.
 */
public class ServerCommandEntry {
	private final String name;
	private final List<String> commands;
	private final String description;

	public ServerCommandEntry(String name, List<String> commands, String description) {
		this.name = name;
		this.commands = commands;
		this.description = description == null ? "" : description;
	}

	public String getName() { return name; }
	public List<String> getCommands() { return commands; }
	public String getDescription() { return description; }
}
