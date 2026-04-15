package com.remrin.client.sync;

import com.remrin.sync.ServerCommandGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory store for the server-side command preset received via the {@code eun_carpet} mod.
 * <p>
 * Updated when a {@code eun_carpet:cmd_gui_preset} payload arrives. Listeners (e.g.
 * {@link com.remrin.client.gui.CommandGUIScreen}) are notified so they can refresh the server tab.
 */
public final class ServerCommandStore {

	private static List<ServerCommandGroup> groups = Collections.emptyList();
	private static final List<Runnable> listeners = new ArrayList<>();

	private ServerCommandStore() {}

	/**
	 * Replaces the current group list and notifies all registered listeners.
	 */
	public static void update(List<ServerCommandGroup> newGroups) {
		groups = new ArrayList<>(newGroups);
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

	/**
	 * Clears all server commands (e.g. on disconnect).
	 */
	public static void clear() {
		groups = Collections.emptyList();
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

	public static List<ServerCommandGroup> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	public static boolean hasData() {
		return !groups.isEmpty();
	}

	/**
	 * Registers a listener that is called whenever the store is updated or cleared.
	 */
	public static void addListener(Runnable listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public static void removeListener(Runnable listener) {
		listeners.remove(listener);
	}
}
